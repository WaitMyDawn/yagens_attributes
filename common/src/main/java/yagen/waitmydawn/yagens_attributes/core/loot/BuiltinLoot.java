package yagen.waitmydawn.yagens_attributes.core.loot;

import java.util.List;

public final class BuiltinLoot {
    public static java.util.Map<String, ModReward> rewardTemplates() {
        var weights = java.util.Map.of(yagen.waitmydawn.yagens_attributes.core.ModInfo.Rarity.COMMON, 1D, yagen.waitmydawn.yagens_attributes.core.ModInfo.Rarity.UNCOMMON, 1D,
                yagen.waitmydawn.yagens_attributes.core.ModInfo.Rarity.RARE, 1D, yagen.waitmydawn.yagens_attributes.core.ModInfo.Rarity.LEGENDARY, 1D);
        return java.util.Map.of(
                "yagens_attributes:rewards/quality_weighted_mod", new ModReward(ModReward.Method.WEIGHTED, weights, "", 0, 100),
                "yagens_attributes:rewards/specific_mod", new ModReward(ModReward.Method.SPECIFIC, java.util.Map.of(), new yagen.waitmydawn.yagens_attributes.core.mods.UniversalNanocubeMod().id(), 0, 100),
                "yagens_attributes:rewards/veiled_riven", new ModReward(ModReward.Method.VEILED_RIVEN, java.util.Map.of(), "", 0, 100),
                "yagens_attributes:rewards/galvanized_mod", new ModReward(ModReward.Method.GALVANIZED, java.util.Map.of(), "", 0, 100));
    }
    private BuiltinLoot() {}
    public static List<LootRule> additionalRules() {
        var result = new java.util.ArrayList<>(BuiltinMobLoot.rules()); result.addAll(BuiltinContainerLoot.rules()); return List.copyOf(result);
    }
    public static List<FixedLootRule> rules() {
        return List.of(new FixedLootRule("yagens_attributes:enderman_extra", "minecraft:enderman", "minecraft:entities/enderman", "yagens_attributes:forma", .10, 1));
    }
}
