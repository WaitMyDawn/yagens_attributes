"""Verify generated Mod catalogs, physical models and standalone loot templates."""

from pathlib import Path
import struct
from verify_combat_resources import ROOT, RESOURCES, STATIC_RESOURCES, read, require


def texture_resource(texture):
    namespace, path = texture.split(":", 1)
    relative = Path("assets") / namespace / "textures" / (path + ".png")
    candidates = [root / relative for root in (STATIC_RESOURCES, RESOURCES) if (root / relative).is_file()]
    require(len(candidates) == 1, f"Expected one static or generated texture source for {texture}: {candidates}")
    return candidates[0]


def main():
    data = RESOURCES / "data/yagens_attributes"
    assets = RESOURCES / "assets/yagens_attributes"
    mods = {p.stem: read(p) for p in (data / "mods").glob("*.json")}
    qualities = {p.stem: read(p) for p in (data / "mod_rarities").glob("*.json")}
    polarities = {p.stem: read(p) for p in (data / "mod_polarities").glob("*.json")}
    require(mods and qualities and polarities, "Empty bundled Mod catalog")
    variants = 0
    rendered_textures = set()
    for name, mod in mods.items():
        require(mod["schema_version"] == 1 and mod["rarity"] in qualities and mod["polarity"] in polarities, f"Invalid metadata: {name}")
        require(type(mod["max_level"]) is int and 0 <= mod["max_level"] <= 60, f"Invalid rank: {name}")
        require(mod["entries"] and mod["applicability"], f"Missing effects/scope: {name}")
        for effect in mod["entries"]:
            require(isinstance(effect["description"], str) and effect["description"].isascii() and effect["description"].strip(), f"Expected English editing guidance: {name}")
            require(effect["operation"] == ("addin" if effect["stat"] in {"combo_duration", "melee_range"} else "addition"), f"Invalid bundled Mod operation: {name}")
        model = read(assets / f"models/item/mods/{name}.json")
        visual = mod.get("style", {}).get("visual", "none")
        require(visual in {"none", "orokin"}, f"Unsupported visual style: {name}")
        rendered = f"yagens_attributes:item/mods/effects/{name}" if visual == "orokin" else mod["texture"]
        rendered_textures.add(rendered)
        require(model == {"parent": "minecraft:item/generated", "textures": {"layer0": rendered}}, f"Invalid physical model: {name}")
        loot = read(data / f"loot_table/mods/{name}.json")
        entry = loot["pools"][0]["entries"][0]
        require(entry["name"] == "yagens_attributes:mod" and entry["functions"] == [{"function": "minecraft:set_components", "components": {"yagens_attributes:mod_data": {"type": f"yagens_attributes:{name}", "level": 0}}}], f"Invalid loot component: {name}")
        variants += mod["max_level"] + 1
    textures = {mod["texture"] for mod in mods.values()} | rendered_textures | {"yagens_attributes:item/mod"}
    riven_rules = read(RESOURCES / "data/yagens_attributes/riven_rules/default.json")
    require(riven_rules["baseCapacity"] == 10 and riven_rules["maxLevel"] == 8, "Invalid Riven capacity/rank")
    textures.add(riven_rules["texture"])
    atlas = read(RESOURCES / "assets/minecraft/atlases/blocks.json")
    require(atlas.keys() == {"sources"}, "Mod atlas must append sources without replacing vanilla directories")
    require(all(source.keys() == {"type", "resource"} and source['type'] == 'minecraft:single' for source in atlas['sources']), "Expected explicit Mod texture atlas entries")
    atlas_textures = [source['resource'] for source in atlas['sources']]
    require(len(atlas_textures) == len(set(atlas_textures)) and set(atlas_textures) == textures, "Item atlas must include every declared Mod texture exactly once")
    images = [texture_resource(texture) for texture in sorted(textures)]
    for path in images:
        png = path.read_bytes()
        width, height = struct.unpack(">II", png[16:24])
        animation = Path(str(path) + ".mcmeta")
        valid_size = (width, height) == (32, 32)
        if animation.is_file():
            metadata = read(animation)["animation"]
            valid_size = width == 32 and height >= 32 and height % 32 == 0
            require(type(metadata.get("frametime", 1)) is int and metadata.get("frametime", 1) > 0, f"Invalid frame time: {path}")
        require(len(png) >= 33 and png[:8] == b"\x89PNG\r\n\x1a\n" and png[12:16] == b"IHDR"
                and valid_size and png[24:26] == b"\x08\x06",
                f"Expected 32px 8-bit RGBA sprite or animation strip: {path}")
    font = read(assets / "font/polarity_icons.json")
    require(len(font["providers"]) == len(polarities), "Expected a glyph provider for each polarity")
    for provider in font["providers"]:
        glyph = assets / ("textures/" + provider["file"].split(":", 1)[1])
        require(struct.unpack(">II", glyph.read_bytes()[16:24]) == (32, 32), f"Expected 32px polarity glyph: {glyph}")
        name = glyph.stem
        require(provider["chars"] == [polarities[name]["symbol"]], f"Polarity symbol/font mismatch: {name}")
    require(read(RESOURCES / "assets/minecraft/font/default.json")["providers"] == [{"type": "reference", "id": "yagens_attributes:polarity_icons"}, {"type": "reference", "id": "yagens_attributes:mod_symbols"}], "Default font must reference polarity and Mod symbol fonts")
    for resource_root in (RESOURCES, ROOT / "common/src/main/resources"):
        for recipe in (resource_root / "data").glob("*/recipe/**/*.json"):
            require("yagens_attributes:mod" not in recipe.read_text(encoding="utf-8"), f"Mod crafting recipe is not supported: {recipe}")
    static_count = sum(path.is_relative_to(STATIC_RESOURCES) for path in images)
    print(f"Validated {len(mods)} Mod definitions / {variants} levels, {len(qualities)} rarities, {len(polarities)} polarities, {len(images)} textures ({static_count} static), models and standalone loot tables")


if __name__ == "__main__":
    main()
