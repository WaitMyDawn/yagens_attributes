package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.Category.*;

public final class BuiltinRivens {
    private BuiltinRivens() {}
    public static Map<String, Double> dispositions() {
        return Map.ofEntries(
                Map.entry("minecraft:wooden_sword", 1.55), Map.entry("minecraft:wooden_axe", 1.55),
                Map.entry("minecraft:golden_sword", 1.55), Map.entry("minecraft:golden_axe", 1.55),
                Map.entry("minecraft:stone_sword", 1.5), Map.entry("minecraft:stone_axe", 1.5),
                Map.entry("minecraft:iron_sword", 1.45), Map.entry("minecraft:iron_axe", 1.45),
                Map.entry("minecraft:diamond_sword", 1.35), Map.entry("minecraft:diamond_axe", 1.35),
                Map.entry("minecraft:netherite_sword", 1.3), Map.entry("minecraft:netherite_axe", 1.3),
                Map.entry("minecraft:mace", 1.1), Map.entry("minecraft:trident", 1.2),
                Map.entry("minecraft:bow", 1.0), Map.entry("minecraft:crossbow", .95));
    }
    public static RivenRules rules() {
        var entries = new LinkedHashMap<String, RivenRules.Trait>();
        trait(entries, Stats.DAMAGE, 1.647, 1.65, true, true, Set.of(WEAPON), 0);
        trait(entries, Stats.CRITICAL_CHANCE, 1.8, 1.4999, true, true, Set.of(WEAPON), 0);
        trait(entries, Stats.CRITICAL_DAMAGE, .9, 1.19997, true, true, Set.of(WEAPON), 0);
        trait(entries, Stats.STATUS_CHANCE, .9, .9, true, true, Set.of(WEAPON), 0);
        trait(entries, Stats.STATUS_DURATION, .9999, .9999, true, true, Set.of(WEAPON), 0);
        trait(entries, Stats.ATTACK_SPEED, .549, 0, true, true, Set.of(MELEE), 0);
        trait(entries, Stats.COMBO_DURATION, 8.1, 0, true, true, Set.of(MELEE), 0);
        trait(entries, Stats.FIRE_RATE, .6003, .6003, true, true, Set.of(RANGED, TRIDENT), 0);
        trait(entries, Stats.MULTISHOT, .9, .9, true, true, Set.of(RANGED, TRIDENT), 0);
        for (var type : List.of(DamageType.HEAT, DamageType.COLD, DamageType.ELECTRICITY, DamageType.TOXIN))
            trait(entries, "damage." + type.key(), .9, .9, true, false, Set.of(WEAPON), 0);
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH))
            trait(entries, "damage." + type.key(), type == DamageType.PUNCTURE ? 1.19997 : 1.197, type == DamageType.PUNCTURE ? 1.19997 : 1.1997, true, true, Set.of(WEAPON), .25);
        return new RivenRules(10, 8, .9, 1.1, List.of(new RivenRules.Factors(2, 0, .99, 0),
                new RivenRules.Factors(3, 0, .75, 0), new RivenRules.Factors(2, 1, 1.2375, -.495),
                new RivenRules.Factors(3, 1, .9375, -.75)), entries, "yagens_attributes:item/mods/riven");
    }
    private static void trait(Map<String, RivenRules.Trait> entries, String stat, double melee, double ranged, boolean positive, boolean negative, Set<ModInfo.Category> scope, double physicalMinimum) {
        boolean elemental = List.of("damage.heat", "damage.cold", "damage.electricity", "damage.toxin").contains(stat);
        entries.put(stat, new RivenRules.Trait(stat, stat.equals(Stats.COMBO_DURATION) ? ModDefinition.Operation.ADDIN : ModDefinition.Operation.ADDITION,
                elemental ? ModDefinition.Basis.ATTACK_DAMAGE : ModDefinition.Basis.FLAT, scope, melee, ranged, positive, negative, physicalMinimum,
                "Full-rank reference before disposition, layout and independent 0.9-1.1 variation. Percentage values are fractions; combo duration is seconds. Scale by (rank + 1) / 9."));
    }
}
