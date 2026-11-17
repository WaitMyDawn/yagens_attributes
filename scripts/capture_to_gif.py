"""Encode native capture sequences with one stable palette per loop."""

import argparse
import json
import math
from collections import defaultdict
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


def encode(frames, durations, destination):
    samples = frames[::max(1, len(frames) // 12)]
    board = Image.new("RGB", (frames[0].width, frames[0].height * len(samples)))
    for index, frame in enumerate(samples):
        board.paste(frame, (0, index * frame.height))
    palette = board.quantize(colors=256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    indexed = [frame.quantize(palette=palette, dither=Image.Dither.NONE) for frame in frames]
    indexed[0].save(destination, save_all=True, append_images=indexed[1:], duration=durations,
                    loop=0, disposal=1, optimize=True)
    with Image.open(destination) as result:
        actual = []
        for index in range(result.n_frames):
            result.seek(index)
            actual.append(result.info.get("duration", 0))
        if result.info.get("loop") != 0 or not actual or sum(actual) != sum(durations):
            raise ValueError(f"Animation timing mismatch: {destination}")
        return {"file": destination.name, "frames": result.n_frames, "duration_ms": sum(actual),
                "loop": result.info.get("loop"), "bytes": destination.stat().st_size}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    root = args.input.resolve()
    manifest = json.loads((root / "capture.json").read_text(encoding="utf-8"))
    if not manifest.get("passed"):
        raise ValueError("Capture did not complete")
    groups = defaultdict(list)
    for item in manifest["frames"]:
        file = (root / item["file"]).resolve()
        if not file.is_relative_to(root):
            raise ValueError("Capture path escapes its root")
        groups[file.parent.relative_to(root).as_posix()].append((file, max(10, round(item["duration_ms"] / 10) * 10)))
    args.output.mkdir(parents=True, exist_ok=True)
    reports, sequences = [], []
    for scene, items in groups.items():
        frames = []
        for file, duration in items:
            with Image.open(file) as image:
                frames.append(image.convert("RGB"))
        durations = [duration for _, duration in items]
        reports.append(encode(frames, durations, args.output / (scene.replace("/", "-") + ".gif")))
        sequences.append((scene, frames, durations))
    columns = math.ceil(math.sqrt(len(sequences)))
    width, height = sequences[0][1][0].size
    tile_width, tile_height = width // 2, height // 2
    overview = []
    font = ImageFont.load_default(size=17)
    count = min(len(frames) for _, frames, _ in sequences)
    for index in range(count):
        board = Image.new("RGB", (columns * tile_width, math.ceil(len(sequences) / columns) * tile_height))
        draw = ImageDraw.Draw(board)
        for position, (scene, frames, _) in enumerate(sequences):
            left, top = position % columns * tile_width, position // columns * tile_height
            board.paste(frames[index].resize((tile_width, tile_height), Image.Resampling.LANCZOS), (left, top))
            draw.text((left + 10, top + 8), scene, fill="white", stroke_width=2, stroke_fill="black", font=font)
        overview.append(board)
    reports.append(encode(overview, [50] * count, args.output / "overview.gif"))
    (args.output / "report.json").write_text(json.dumps(reports, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(reports, indent=2))


if __name__ == "__main__":
    main()
