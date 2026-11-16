"""Build review-only alpha-cutoff PNGs, byte-size reports and paired-background boards."""

import argparse
import json
import re
import shutil
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from optimize_png import optimize_png


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def font(size):
    paths = [Path("C:/Windows/Fonts/msyh.ttc"), Path("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc")]
    path = next((p for p in paths if p.is_file()), None)
    return ImageFont.truetype(str(path), size) if path else ImageFont.load_default(size=size)


def centered(draw, value, x, y, size=16, color="#dfe8e8"):
    draw.text((x, y), value, font=font(size), fill=color, anchor="mt")


def render_boards(report, work):
    thresholds = report["thresholds_percent"]
    columns = [None, *thresholds]
    width, row_height = 160 + len(columns) * 280, 226
    height = 112 + len(report["assets"]) * row_height
    board = Image.new("RGBA", (width, height), "#121d26")
    draw = ImageDraw.Draw(board)
    centered(draw, "低透明度清理对比 · 同一行均为 32×32", width // 2, 12, 25)
    centered(draw, "每格左侧浅背景 / 右侧深背景；放大图使用最近邻显示", width // 2, 48, 15, "#93aab3")
    for index, cutoff in enumerate(columns):
        centered(draw, "当前正式图" if cutoff is None else f"清理 α < {cutoff}%", 160 + index * 280 + 132, 78, 19, "#e8ce91")
    for row, asset in enumerate(report["assets"]):
        y = 108 + row * row_height
        draw.line((12, y - 5, width - 12, y - 5), fill="#344953")
        draw.text((16, y + 65), asset["name"], font=font(20), fill="#e8ce91")
        for column, cutoff in enumerate(columns):
            case = asset["baseline"] if cutoff is None else asset["variants"][str(cutoff)]
            image = Image.open(work / case["file"]).convert("RGBA")
            left = 160 + column * 280
            for offset, colors in [(0, ("#c1c8cd", "#e5e9eb")), (136, ("#16212b", "#253846"))]:
                for yy in range(0, 128, 8):
                    for xx in range(0, 128, 8):
                        draw.rectangle((left + offset + xx, y + yy, left + offset + xx + 7, y + yy + 7), fill=colors[(xx // 8 + yy // 8) % 2])
                board.alpha_composite(image.resize((128, 128), Image.Resampling.NEAREST), (left + offset, y))
            centered(draw, f"{case['bytes']} B", left + 132, y + 134, 18, "#e8ce91")
            detail = "当前无损压缩版本" if cutoff is None else f"清理 {case['removed_visible_pixels']} 个低透明像素"
            centered(draw, detail, left + 132, y + 159, 13, "#9db3bb")
            draw.text((left + 28, y + 189), "16 / 32 px", font=font(12), fill="#829ca8")
            board.alpha_composite(image.resize((16, 16), Image.Resampling.NEAREST), (left + 150, y + 190))
            board.alpha_composite(image, (left + 181, y + 181))
    board.convert("RGB").save(work / "comparison.png")
    for row, asset in enumerate(report["assets"]):
        top = 108 + row * row_height
        detail = Image.new("RGB", (width, row_height + 108), "#121d26")
        detail.paste(board.crop((0, 0, width, 108)), (0, 0))
        detail.paste(board.crop((0, top, width, top + row_height)), (0, 108))
        detail.save(work / (asset["id"] + "-comparison.png"))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--work", type=Path, required=True)
    parser.add_argument("--tools", type=Path)
    args = parser.parse_args()
    if args.tools:
        sys.path.insert(0, str(args.tools.resolve()))
    manifest = json.loads(args.manifest.read_text("utf-8"))
    if manifest.get("schema_version") != 1:
        raise ValueError("Unsupported comparison schema")
    thresholds = manifest["thresholds_percent"]
    if len(set(thresholds)) != len(thresholds) or any(type(value) is not int or not 0 < value <= 100 for value in thresholds):
        raise ValueError("Thresholds must be distinct integer percentages from 1 to 100")
    root = args.manifest.resolve().parent
    work = args.work.resolve(); work.mkdir(parents=True, exist_ok=True)
    if work == root or root.is_relative_to(work) or work.is_relative_to(root):
        raise ValueError("Comparison outputs must be separate from source inputs")
    report = {"schema_version": 1, "thresholds_percent": thresholds, "comparison_only": True,
              "policy": "Clear RGBA only where alpha*100 < cutoff_percent*255; preserve all other RGBA values.", "assets": []}
    ids = set()
    for asset in manifest["assets"]:
        identity = asset["id"]
        if not re.fullmatch(r"[A-Za-z0-9_-]+", identity) or identity.casefold() in ids:
            raise ValueError("Unsafe or duplicate comparison ID")
        ids.add(identity.casefold())
        source = (root / asset["source"]).resolve()
        if not source.is_relative_to(root):
            raise ValueError("Comparison input must stay inside its manifest root")
        with Image.open(source) as image:
            if image.size != (32, 32):
                raise ValueError("These review boards require the project's 32px texture contract")
        folder = work / "candidates" / identity; folder.mkdir(parents=True, exist_ok=True)
        original = folder / "original.png"; shutil.copy2(source, original)
        entry = {"id": identity, "name": asset["name"], "baseline": {"file": original.relative_to(work).as_posix(), "bytes": original.stat().st_size}, "variants": {}}
        for cutoff in thresholds:
            target = folder / f"alpha-{cutoff}.png"
            result = optimize_png(source, target, alpha_cutoff_percent=cutoff)
            write_json(work / "reports" / f"{identity}-{cutoff}.json", result)
            entry["variants"][str(cutoff)] = {"file": target.relative_to(work).as_posix(), "bytes": result["output_bytes"], **result["alpha_cleanup"]}
        report["assets"].append(entry)
    write_json(work / "report.json", report)
    render_boards(report, work)
    for asset in report["assets"]:
        print(json.dumps({"id": asset["id"], "baseline": asset["baseline"]["bytes"], "variants": {key: value["bytes"] for key, value in asset["variants"].items()}}, ensure_ascii=False))


if __name__ == "__main__":
    main()
