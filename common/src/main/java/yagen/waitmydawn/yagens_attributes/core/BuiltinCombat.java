package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/**
 * Shared authoring defaults for generated weapon profiles and defense tags.
 */
public final class BuiltinCombat {
    private BuiltinCombat() {
    }

    public record Template(WeaponProfile profile, boolean derived) {
    }

    public static Map<String, WeaponProfile> weapons() {
        var result = new LinkedHashMap<String, WeaponProfile>();
        result.put("minecraft:bow", new WeaponProfile(0.4D, 2D, 0.2D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.2D), 6D, ModInfo.Category.BOW, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D));
        result.put("minecraft:crossbow", new WeaponProfile(0.3D, 2D, 0.25D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.2D), 7D, ModInfo.Category.CROSSBOW, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D));
        result.put("minecraft:diamond_axe", new WeaponProfile(0.15D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 9D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:diamond_sword", new WeaponProfile(0.15D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 7D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:golden_axe", new WeaponProfile(0.1D, 2D, 0.2D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 7D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:golden_sword", new WeaponProfile(0.1D, 2D, 0.3D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 4D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:iron_axe", new WeaponProfile(0.12D, 2D, 0.12D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 9D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:iron_sword", new WeaponProfile(0.12D, 2D, 0.12D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 6D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:mace", new WeaponProfile(0.28D, 2D, 0.05D, 6D, Map.of(DamageType.IMPACT, 1D), 6D, ModInfo.Category.MACE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:netherite_axe", new WeaponProfile(0.18D, 2D, 0.18D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 10D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:netherite_sword", new WeaponProfile(0.18D, 2D, 0.18D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 8D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:stone_axe", new WeaponProfile(0.1D, 2D, 0.1D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 9D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:stone_sword", new WeaponProfile(0.1D, 2D, 0.1D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 5D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:trident", new WeaponProfile(0.25D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.1D), 9D, ModInfo.Category.TRIDENT, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D));
        result.put("minecraft:wooden_axe", new WeaponProfile(0.1D, 2D, 0.1D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 7D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.put("minecraft:wooden_sword", new WeaponProfile(0.1D, 2D, 0.1D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 4D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D));
        result.replaceAll((id, profile) -> profile.withDisposition(BuiltinRivens.dispositions().getOrDefault(id, 0D)));
        return Map.copyOf(result);
    }

    public static Map<String, Template> templates() {
        var result = new LinkedHashMap<String, Template>();
        result.put("sword", new Template(new WeaponProfile(0.15D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.45D, DamageType.SLASH, 0.45D), 1D, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D), true));
        result.put("axe", new Template(new WeaponProfile(0.15D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.1D, DamageType.SLASH, 0.7D), 1D, ModInfo.Category.AXE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D), true));
        result.put("trident", new Template(new WeaponProfile(0.25D, 2D, 0.15D, 6D, Map.of(DamageType.IMPACT, 0.2D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.1D), 1D, ModInfo.Category.TRIDENT, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D), true));
        result.put("bow", new Template(new WeaponProfile(0.4D, 2D, 0.2D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.2D), 6D, ModInfo.Category.BOW, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D), false));
        result.put("crossbow", new Template(new WeaponProfile(0.3D, 2D, 0.25D, 6D, Map.of(DamageType.IMPACT, 0.1D, DamageType.PUNCTURE, 0.7D, DamageType.SLASH, 0.2D), 7D, ModInfo.Category.CROSSBOW, Collections.nCopies(8, ModInfo.Polarity.NONE), 1.0D, 1.0D), false));
        result.put("mace", new Template(new WeaponProfile(0.28D, 2D, 0.05D, 6D, Map.of(DamageType.IMPACT, 1D), 1D, ModInfo.Category.MACE, Collections.nCopies(8, ModInfo.Polarity.NONE), 1D, 1D), true));
        return Map.copyOf(result);
    }

    public static Map<String, Template> weaponDefinitions() {
        var vanilla = weapons();
        var result = new LinkedHashMap<String, Template>();
        vanilla.forEach((id, profile) -> result.put(id, new Template(profile, false)));
        result.putAll(yagen.waitmydawn.yagens_attributes.core.compat.IronsSpellbooksWeapons.definitions(vanilla));
        result.putAll(yagen.waitmydawn.yagens_attributes.core.compat.CataclysmWeapons.definitions(vanilla));
        result.putAll(yagen.waitmydawn.yagens_attributes.core.compat.TwilightForestWeapons.definitions(vanilla));
        return Map.copyOf(result);
    }

    public static Map<String, List<String>> tags() {
        var result = new LinkedHashMap<String, List<String>>();
        // Elemental status damage uses material rules, without native fire/explosion immunities or specialized protection.
        result.put("minecraft:damage_type/bypasses_armor", List.of("yagens_attributes:slash_status"));
        result.put("minecraft:damage_type/no_knockback", List.of("yagens_attributes:impact_status", "yagens_attributes:puncture_status", "yagens_attributes:slash_status", "yagens_attributes:cold_status", "yagens_attributes:heat_status", "yagens_attributes:electricity_status", "yagens_attributes:toxin_status", "yagens_attributes:blast_status", "yagens_attributes:radiation_status", "yagens_attributes:corrosive_status", "yagens_attributes:viral_status", "yagens_attributes:gas_status", "yagens_attributes:magnetic_status"));
        result.put("yagens_attributes:entity_type/control_immune", List.of("minecraft:ender_dragon", "minecraft:wither"));
        result.put("yagens_attributes:entity_type/immune_to/blast", List.of());
        result.put("yagens_attributes:entity_type/immune_to/cold", List.of());
        result.put("yagens_attributes:entity_type/immune_to/corrosive", List.of());
        result.put("yagens_attributes:entity_type/immune_to/electricity", List.of());
        result.put("yagens_attributes:entity_type/immune_to/gas", List.of());
        result.put("yagens_attributes:entity_type/immune_to/heat", List.of());
        result.put("yagens_attributes:entity_type/immune_to/impact", List.of());
        result.put("yagens_attributes:entity_type/immune_to/magnetic", List.of());
        result.put("yagens_attributes:entity_type/immune_to/puncture", List.of());
        result.put("yagens_attributes:entity_type/immune_to/radiation", List.of());
        result.put("yagens_attributes:entity_type/immune_to/slash", List.of());
        result.put("yagens_attributes:entity_type/immune_to/toxin", List.of());
        result.put("yagens_attributes:entity_type/immune_to/viral", List.of());
        result.put("yagens_attributes:entity_type/status_immune", List.of());
        return Map.copyOf(result);
    }
}
