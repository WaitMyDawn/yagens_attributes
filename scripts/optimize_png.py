"""Process incoming PNGs with the project opacity policy and lossless encoding."""

import argparse
import io
import json
import struct
import sys
import tempfile
from pathlib import Path

from PIL import Image

ESSENTIAL = {b"IHDR", b"PLTE", b"tRNS", b"IDAT", b"IEND"}
COLOR_METADATA = {b"gAMA", b"cHRM", b"sRGB", b"iCCP", b"cICP", b"eXIf"}
DEFAULT_ALPHA_CUTOFF_PERCENT = 5


def chunks(data):
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("Expected a PNG file")
    result, offset = [], 8
    while offset < len(data):
        if offset + 12 > len(data):
            raise ValueError("Truncated PNG chunk")
        length = struct.unpack_from(">I", data, offset)[0]
        end = offset + 12 + length
        if end > len(data):
            raise ValueError("Truncated PNG chunk payload")
        result.append((data[offset + 4:offset + 8], data[offset:end]))
        offset = end
    return result


def pixels(data):
    with Image.open(io.BytesIO(data)) as image:
        if getattr(image, "n_frames", 1) != 1:
            raise ValueError("Animated PNGs require a frame-aware optimizer")
        return image.size, image.convert("RGBA").tobytes()


def cleanup_alpha(data, percent):
    if type(percent) is not int or not 0 <= percent <= 100:
        raise ValueError("Alpha cutoff must be an integer percentage from 0 to 100")
    size, original = pixels(data)
    result = bytearray(original)
    removed = normalized = 0
    for index in range(0, len(result), 4):
        alpha = result[index + 3]
        # Integer comparison preserves the strict boundary, including alpha 51 at exactly 20%.
        if alpha * 100 < percent * 255:
            if alpha > 0:
                removed += 1
            elif any(result[index:index + 3]):
                normalized += 1
            result[index:index + 4] = b"\0\0\0\0"
    expected = size, bytes(result)
    report = {"cutoff_percent": percent, "comparison": "strictly_less_than",
              "removed_visible_pixels": removed, "normalized_zero_alpha_pixels": normalized,
              "remaining_rgba_unchanged": True}
    if expected == (size, original):
        return data, expected, report
    buffer = io.BytesIO()
    Image.frombytes("RGBA", size, expected[1]).save(buffer, format="PNG", compress_level=9)
    encoded_chunks = chunks(buffer.getvalue())
    preserved = b"".join(raw for kind, raw in chunks(data) if kind in COLOR_METADATA)
    encoded = buffer.getvalue()[:8] + b"".join(raw + (preserved if kind == b"IHDR" else b"") for kind, raw in encoded_chunks)
    return encoded, expected, report


def optimize_png(source, destination, iterations=30, target_bytes=1024, alpha_cutoff_percent=DEFAULT_ALPHA_CUTOFF_PERCENT):
    from zopfli.png import optimize
    import zopfli

    original = source.read_bytes()
    original_chunks = chunks(original)
    header = next(raw for kind, raw in original_chunks if kind == b"IHDR")
    if header[16] > 8:
        raise ValueError("16-bit PNGs require a full-depth pixel comparison")
    original_pixels = pixels(original)
    working, expected, cleanup = (original, original_pixels, None) if alpha_cutoff_percent is None else cleanup_alpha(original, alpha_cutoff_percent)
    preserved = [kind.decode("ascii") for kind, _ in original_chunks if kind in COLOR_METADATA]
    encoded = optimize(working, lossy_transparent=False, lossy_8bit=False,
                       filter_strategies="01234mepb", keepchunks=preserved,
                       use_zopfli=True, num_iterations=iterations, num_iterations_large=iterations)
    stripped = working[:8] + b"".join(raw for kind, raw in chunks(working) if kind in ESSENTIAL | COLOR_METADATA)
    candidate = min((encoded, stripped, working), key=len)
    candidate_chunks = chunks(candidate)
    candidate_pixels = pixels(candidate)
    if candidate_pixels != expected:
        raise ValueError("Optimizer changed expected decoded pixels; output was not written")
    if [(kind, raw) for kind, raw in candidate_chunks if kind in COLOR_METADATA] != [
        (kind, raw) for kind, raw in original_chunks if kind in COLOR_METADATA
    ]:
        raise ValueError("Optimizer changed color/orientation metadata; output was not written")
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(dir=destination.parent, prefix=destination.name + ".", suffix=".tmp", delete=False) as output:
        temporary = Path(output.name)
        output.write(candidate)
    try:
        temporary.replace(destination)
    finally:
        temporary.unlink(missing_ok=True)
    return {
        "schema_version": 1,
        "source": source.as_posix(), "output": destination.as_posix(),
        "optimizer": "ZopfliPNG", "version": zopfli.__version__, "iterations": iterations,
        "input_bytes": len(original), "output_bytes": len(candidate),
        "saved_bytes": len(original) - len(candidate),
        "target_bytes": target_bytes, "target_met": len(candidate) <= target_bytes,
        "size": list(original_pixels[0]), "rgba_pixels_equal": candidate_pixels == original_pixels,
        "expected_pixels_equal": True, "alpha_cleanup": cleanup,
        "color_metadata_equal": True, "lossy_transparent": False, "lossy_8bit": False,
        "removed_chunks": [kind.decode("ascii") for kind, _ in original_chunks if kind not in {k for k, _ in candidate_chunks}],
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--tools", type=Path, help="Directory containing the optional zopfli installation")
    parser.add_argument("--report", type=Path)
    parser.add_argument("--iterations", type=int, default=30)
    parser.add_argument("--target-bytes", type=int, default=1024)
    parser.add_argument("--alpha-cutoff-percent", type=int, default=DEFAULT_ALPHA_CUTOFF_PERCENT,
                        help="Clear pixels below this opacity percentage (default: 5); use 0 to preserve source RGBA")
    args = parser.parse_args()
    if args.iterations < 1 or args.target_bytes < 1:
        parser.error("Iterations and target bytes must be positive")
    if args.alpha_cutoff_percent is not None and not 0 <= args.alpha_cutoff_percent <= 100:
        parser.error("Alpha cutoff must be between 0 and 100")
    if args.source.resolve() == args.output.resolve():
        parser.error("Preserve the source; choose a separate output path")
    if args.tools:
        sys.path.insert(0, str(args.tools.resolve()))
    report = optimize_png(args.source, args.output, args.iterations, args.target_bytes, args.alpha_cutoff_percent)
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
