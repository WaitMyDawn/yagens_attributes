"""Validate combat data and icon-font contracts without loading Minecraft."""

import json
import math
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "common/src/generated/resources"
STATIC_RESOURCES = ROOT / "common/src/main/resources"
TYPES = ("impact", "puncture", "slash", "cold", "heat", "electricity", "toxin",
         "blast", "radiation", "corrosive", "viral", "gas", "magnetic")


def require(condition, message):
    if not condition:
        raise ValueError(message)


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def read(path):
    return json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=unique_object)


def resource(relative):
    generated = RESOURCES / relative
    return generated if generated.is_file() else STATIC_RESOURCES / relative


def profile(data, path, derive=False):
    fields = {"critical_chance", "critical_damage", "status_chance", "status_duration", "damage_distribution", "base_damage", "category", "mod_slots"}
    require(data.keys() <= fields | {"fire_rate", "multishot", "riven_disposition"} and fields - ({"base_damage"} if derive else set()) <= data.keys(), f"Invalid weapon fields: {path}")
    for field in data.keys() - {"damage_distribution", "category", "mod_slots"}:
        value = data[field]
        require(type(value) in (int, float) and math.isfinite(value) and value >= 0, f"Invalid {field}: {path}")
    require(data["critical_chance"] <= 100 and data["critical_damage"] >= 1
            and data["status_chance"] <= 64 and data["status_duration"] <= 3600, f"Stat limit: {path}")
    disposition = data.get("riven_disposition", 0)
    require(disposition == 0 or .5 <= disposition <= 1.55, f"Invalid Riven disposition: {path}")
    for key in ("fire_rate", "multishot"):
        require(0 <= data.get(key, 1) <= 64, f"Ranged stat limit: {path}")
        if data['category'] in ('bow', 'crossbow', 'trident'):
            require(data.get(key) == 1.0, f"Expected explicit ranged baseline 1.0: {path}")
    distribution = data["damage_distribution"]
    require(distribution.keys() <= set(TYPES), f"Unknown damage type: {path}")
    require(all(type(x) in (int, float) and math.isfinite(x) and x >= 0 for x in distribution.values()), f"Invalid weights: {path}")
    require(math.isclose(sum(distribution.values()), 1), f"Default weights must sum to one: {path}")
    require(data.get('base_damage', 1) > 0 and data['category'] in {'weapon', 'melee', 'ranged', 'sword', 'axe', 'trident', 'mace', 'bow', 'crossbow'}, f"Invalid weapon baseline/category: {path}")
    require(1 <= len(data['mod_slots']) <= 64 and all(x in {'none', 'madurai', 'vazarin', 'naramon', 'zenurik', 'unairu', 'penjaga', 'umbra'} for x in data['mod_slots']), f"Invalid slot polarity: {path}")


def literal_ui_keys(content):
    # Concatenated fragments are not complete keys; their generated variants need separate coverage.
    keys = set(re.findall(r'"((?:gui|tooltip|key)\.yagens_attributes\.[a-z_]+)"(?!\s*\+)', content))
    keys.update("gui.yagens_attributes." + key for key in re.findall(r'text\("([a-z_]+)"(?!\s*\+)', content))
    return keys


def main():
    weapons = list((RESOURCES / "data").glob("*/weapon_stats/**/*.json"))
    require(bool(weapons), "No weapon definitions")
    for path in weapons:
        profile(read(path), path)
    defaults = list((RESOURCES / "data").glob("*/weapon_defaults/**/*.json"))
    families = set()
    for path in defaults:
        data = read(path)
        require(data.keys() == {"schema_version", "classes"} and data['schema_version'] == 1, f"Invalid class-default schema: {path}")
        for family, definition in data['classes'].items():
            require(family not in families, f"Duplicate family default: {family}")
            families.add(family)
            profile(definition, f"{path}:{family}", derive=True)
    require(families == {'sword', 'axe', 'trident', 'mace', 'bow', 'crossbow'}, "Incomplete/unknown default families")
    font = read(RESOURCES / "assets/yagens_attributes/font/damage_icons.json")
    require(len(font["providers"]) == len(TYPES), "Wrong icon count")
    for index, (provider, damage_type) in enumerate(zip(font["providers"], TYPES)):
        require(provider["type"] == "bitmap" and provider["chars"] == [chr(0xE000 + index)], "Invalid icon glyph")
        require(provider["file"] == f"yagens_attributes:status/{damage_type}_status.png", "Invalid icon mapping")
        namespace, texture = provider["file"].split(":", 1)
        path = resource(Path("assets") / namespace / "textures" / texture)
        png = path.read_bytes()
        require(png[:8] == b"\x89PNG\r\n\x1a\n", f"Invalid PNG: {path}")
        width, height = struct.unpack(">II", png[16:24])
        require(width == height == 32 and height >= provider["height"] and png[25] == 6, f"Expected 32x32 RGBA icon: {path}")
    languages = {name: read(RESOURCES / f"assets/yagens_attributes/lang/{name}.json") for name in ("en_us", "zh_cn")}
    endo = resource("assets/yagens_attributes/textures/item/endo.png")
    image = endo.read_bytes()
    require(image[:8] == b"\x89PNG\r\n\x1a\n" and struct.unpack(">II", image[16:24]) == (32, 32) and image[25] == 6, "Invalid 32x32 Endo texture")
    require(read(RESOURCES / "assets/yagens_attributes/models/item/endo.json") == {"parent": "minecraft:item/generated", "textures": {"layer0": "yagens_attributes:item/endo"}}, "Invalid Endo item model")
    for item in ("forma", "kuva"):
        png = resource(f"assets/yagens_attributes/textures/item/{item}.png").read_bytes()
        require(png[:8] == b"\x89PNG\r\n\x1a\n" and struct.unpack(">II", png[16:24]) == (32, 32), f"Invalid {item} item texture")
        require(read(RESOURCES / f"assets/yagens_attributes/models/item/{item}.json") == {"parent": "minecraft:item/generated", "textures": {"layer0": f"yagens_attributes:item/{item}"}}, f"Invalid {item} item model")
    require(languages["en_us"].keys() == languages["zh_cn"].keys(), "Language key sets differ")
    required = {"stat.yagens_attributes." + key for key in ("critical_chance", "critical_damage", "status_chance", "status_duration", "damage", "fire_rate", "multishot")}
    required.update("stat.yagens_attributes.damage." + name for name in TYPES)
    required.update("attribute.yagens_attributes." + name for name in ("max_shield", "shield_regen"))
    for name in TYPES:
        path = RESOURCES / f"data/yagens_attributes/damage_type/{name}_status.json"
        definition = read(path)
        require(definition == {"message_id": f"yagens_attributes.{name}", "scaling": "never", "exhaustion": 0.0, "effects": "hurt"}, f"Invalid status damage definition: {path}")
        immunity = read(RESOURCES / f"data/yagens_attributes/tags/entity_type/immune_to/{name}.json")
        require(immunity == {"replace": False, "values": []}, f"Invalid default immunity: {name}")
        required.update((f"death.attack.yagens_attributes.{name}", f"death.attack.yagens_attributes.{name}.player"))
    armor = read(RESOURCES / "data/minecraft/tags/damage_type/bypasses_armor.json")
    require(armor == {"replace": False, "values": ["yagens_attributes:slash_status"]}, "Only bleeding bypasses armor by default")
    for tag, names in {"no_knockback": TYPES, "is_fire": ["heat"], "is_explosion": ["blast"]}.items():
        definition = read(RESOURCES / f"data/minecraft/tags/damage_type/{tag}.json")
        require(definition == {"replace": False, "values": [f"yagens_attributes:{name}_status" for name in names]}, f"Invalid status compatibility tag: {tag}")
    required.update(("key.categories.yagens_attributes", "key.yagens_attributes.mod_space", "key.yagens_attributes.weapon_panel"))
    required.update(("item.yagens_attributes.endo", "item.yagens_attributes.endo.tooltip"))
    required.update('category.yagens_attributes.' + name for name in ('all', 'warframe', 'weapon', 'melee', 'ranged', 'sword', 'axe', 'trident', 'mace', 'bow', 'crossbow'))
    required.update('rarity.yagens_attributes.' + name for name in ('common', 'uncommon', 'rare', 'legendary', 'riven', 'galvanized'))
    required.update('order.yagens_attributes.' + name for name in ('quality', 'quantity', 'name'))
    required.update('polarity.yagens_attributes.' + name for name in ('none', 'madurai', 'vazarin', 'naramon', 'zenurik', 'unairu', 'penjaga', 'umbra'))
    client = ROOT / "platforms/neoforge-1.21.1/src/main/java/yagen/waitmydawn/yagens_attributes/client"
    for source in client.glob("*.java"):
        content = source.read_text(encoding="utf-8")
        required.update(literal_ui_keys(content))
    require(required <= languages["en_us"].keys(), f"Missing translations: {sorted(required - languages['en_us'].keys())}")
    for key, value in languages["en_us"].items():
        require(value.count("%s") == languages["zh_cn"][key].count("%s"), f"Translation argument mismatch: {key}")
    print(f"Validated {len(weapons)} weapon profiles, {len(defaults)} defaults file/{len(families)} families, {len(TYPES)} status damage types/32px icons, shield/status tags, Endo model/32px texture and {len(languages)} languages")


if __name__ == "__main__":
    main()
