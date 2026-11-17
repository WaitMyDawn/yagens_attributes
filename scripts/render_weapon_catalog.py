"""Present registry metadata and native item renders exported by WeaponCatalogExport."""

import argparse
import base64
import html
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


MOD_NAMES = {"irons_spellbooks": "铁魔法", "cataclysm": "灾变", "twilightforest": "暮色森林"}
ROLES = {"standard": "近战", "ranged": "远程", "casting": "法杖 / 施法",
         "custom_melee": "特殊近战", "custom_ranged": "特殊远程", "throwable": "投掷",
         "defensive": "防御道具", "summoning": "召唤道具", "utility": "功能道具",
         "tool": "挖掘工具", "unclassified_attack_item": "待分类攻击物品"}
FAMILIES = {"SWORD": "剑", "AXE": "斧", "TRIDENT": "三叉戟", "BOW": "弓", "CROSSBOW": "弩", "MACE": "重锤"}


def wrap(draw, text, font, width):
    lines, current = [], ""
    for character in text:
        if current and draw.textlength(current + character, font=font) > width:
            lines.append(current)
            current = ""
        current += character
    return lines + ([current] if current else [])


def font(path, size):
    return ImageFont.truetype(str(path), size)


def sheet(mod, rows, source, output, font_path):
    columns, card_width, card_height, margin, header = 5, 244, 246, 16, 106
    width = margin * 2 + columns * card_width
    height = header + margin + ((len(rows) + columns - 1) // columns) * card_height
    image = Image.new("RGB", (width, height), "#0b141d")
    draw = ImageDraw.Draw(image)
    title_font, name_font, small_font = (font(font_path, size) for size in (30, 21, 14))
    title = f"{MOD_NAMES.get(mod, mod)} · {rows[0]['version']}"
    draw.text((margin + 10, 16), title, font=title_font, fill="#e4cf87")
    draw.text((margin + 10, 61), f"{len(rows)} 件武器与战斗道具 · 图标来自原生物品栏渲染", font=name_font, fill="#9eb0bf")
    for index, row in enumerate(rows):
        x = margin + index % columns * card_width
        y = header + index // columns * card_height
        draw.rounded_rectangle((x, y, x + card_width - 10, y + card_height - 10), 8, fill="#15202b", outline="#344454")
        icon = Image.open(source / row["icon"]).convert("RGB")
        icon.thumbnail((128, 128), Image.Resampling.NEAREST)
        image.paste(icon, (x + (card_width - 10 - icon.width) // 2, y + 5))
        text_y = y + 136
        for line in wrap(draw, row["name"], name_font, card_width - 24)[:2]:
            draw.text((x + 10, text_y), line, font=name_font, fill="#e5eaf0")
            text_y += 25
        field_name = row["field"].rsplit(".", 1)[-1]
        for line in wrap(draw, field_name, small_font, card_width - 24)[:2]:
            draw.text((x + 10, text_y + 2), line, font=small_font, fill="#a7b6c6")
            text_y += 17
        family = row["default_family"]
        badge = "默认 " + FAMILIES.get(family, family) if family else ROLES.get(row["role"], row["role"]) + " · 待配置"
        if row["max_stack_size"] != 1:
            badge = "可堆叠 · 暂不支持装配"
        draw.text((x + 10, y + card_height - 34), badge, font=small_font, fill="#7ed3b2" if family else "#e5b879")
    image.save(output / f"{mod}.png", optimize=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--font", type=Path, default=Path("C:/Windows/Fonts/msyh.ttc"))
    args = parser.parse_args()
    rows = json.loads((args.input / "weapons.json").read_text(encoding="utf-8"))
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output / "weapons.json").write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")
    parts = []
    summary = {}
    for mod in sorted({row["mod"] for row in rows}):
        items = [row for row in rows if row["mod"] == mod]
        weapons = [row for row in items if row["role"] != "tool"]
        summary[mod] = {"weapons_and_combat_items": len(weapons), "tools": len(items) - len(weapons)}
        sheet(mod, weapons, args.input, args.output, args.font)
    for row in rows:
        data = base64.b64encode((args.input / row["icon"]).read_bytes()).decode("ascii")
        search = html.escape(" ".join(str(value) for value in row.values()).lower(), quote=True)
        e = lambda key: html.escape(str(row[key]))
        family = FAMILIES.get(row["default_family"], "需显式配置")
        attributes = "<br>".join(html.escape(f"{v['attribute']}: {v['amount']:g} ({v['operation']})") for v in row["attributes"])
        parts.append(f'''<tr data-mod="{e('mod')}" data-role="{e('role')}" data-family="{e('default_family')}" data-search="{search}">
<td><img width="96" height="96" src="data:image/png;base64,{data}"></td><td><strong>{e('name')}</strong><code>{e('id')}</code><small>{html.escape(ROLES.get(row['role'], row['role']))}</small></td>
<td><code>{e('field')}</code><small>{e('class')}<br>继承 {e('superclass')}</small></td><td>{html.escape(family)}<small>最大堆叠 {row['max_stack_size']}{' · 当前不支持装配' if row['max_stack_size'] != 1 else ''}</small></td><td><small>{attributes or '无主手攻击属性'}</small></td></tr>''')
    options = ''.join(f'<option value="{html.escape(mod)}">{html.escape(MOD_NAMES.get(mod, mod))}</option>' for mod in summary)
    body = '''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>武器注册字段目录</title><style>
body{margin:24px;background:#0b141d;color:#e5eaf0;font:15px "Microsoft YaHei",sans-serif}h1{color:#e4cf87}p,small{color:#a7b6c6}small,code{display:block;margin-top:6px}code{font:12px Consolas,monospace;overflow-wrap:anywhere;color:#8acbe7}table{width:100%;border-collapse:collapse}td,th{text-align:left;padding:10px;border-bottom:1px solid #344454}td:first-child{width:96px}th{background:#1c2c3b;position:sticky;top:0}input,select{padding:10px;margin:0 8px 14px 0;background:#15202b;color:#e5eaf0;border:1px solid #536879;border-radius:5px}input{width:330px}img{image-rendering:pixelated}tr:hover{background:#15202b}</style>
<h1>武器、战斗道具与攻击工具 · 注册字段目录</h1>
<p>字段、注册名、实际类型与主手属性取自已加载模组；图标由 Minecraft 原生 GUI 渲染器导出。默认类别表示可匹配现有类默认值，不能证明特殊攻击、法术、射速或多重已接通。工具与防御/召唤道具单列供配置时选择。</p>
<input id="search" placeholder="搜索名称、注册名、字段或类"><select id="mod"><option value="">全部模组</option>''' + options + '''</select><select id="mode"><option value="combat">武器与战斗道具</option><option value="all">包含挖掘工具</option><option value="native">有默认类别</option><option value="explicit">需显式配置</option><option value="tool">仅挖掘工具</option></select><span id="count"></span>
<table><thead><tr><th>原生图标</th><th>名称 / 注册名</th><th>注册字段 / 实际类</th><th>默认类别</th><th>原生主手修饰值</th></tr></thead><tbody>''' + ''.join(parts) + '''</tbody></table><script>
const rows=[...document.querySelectorAll('tbody tr')],search=document.querySelector('#search'),mod=document.querySelector('#mod'),mode=document.querySelector('#mode');
function update(){let count=0;for(const row of rows){const d=row.dataset,m=mode.value;const visible=(!mod.value||d.mod===mod.value)&&d.search.includes(search.value.toLowerCase())&&(m==='all'||m==='combat'&&d.role!=='tool'||m==='native'&&!!d.family||m==='explicit'&&!d.family||m==='tool'&&d.role==='tool');row.hidden=!visible;if(visible)count++}document.querySelector('#count').textContent=count+' 项'}for(const e of [search,mod,mode])e.addEventListener('input',update);update();</script></html>'''
    (args.output / "catalog.html").write_text(body, encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    print(args.output / "catalog.html")


if __name__ == "__main__":
    main()
