package yagen.waitmydawn.yagens_attributes.core.loot;

import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

/** Shared presets retain explicit quality weights, rank bounds and source identities. */
public final class LootRewards {
    public static final LootRule.PlayerKill PLAYER_KILL = new LootRule.PlayerKill();
    private LootRewards() {}
    public static ModReward weighted(double common, double uncommon, double rare, double legendary, int minPercent, int maxPercent) {
        return new ModReward(ModReward.Method.WEIGHTED, Map.of(ModInfo.Rarity.COMMON, common, ModInfo.Rarity.UNCOMMON, uncommon, ModInfo.Rarity.RARE, rare, ModInfo.Rarity.LEGENDARY, legendary), "", minPercent, maxPercent);
    }
    public static ModReward specific(String id, int minPercent, int maxPercent) { return new ModReward(ModReward.Method.SPECIFIC, Map.of(), id, minPercent, maxPercent); }
    public static ModReward galvanized(int minPercent, int maxPercent) { return new ModReward(ModReward.Method.GALVANIZED, Map.of(), "", minPercent, maxPercent); }
    public static ModReward veiled() { return new ModReward(ModReward.Method.VEILED_RIVEN, Map.of(), "", 0, 100); }
    public static LootRule append(String source, String entity, LootRule.Pool... pools) { return append(source, entity, List.of(pools)); }
    public static LootRule append(String source, String entity, List<LootRule.Pool> pools) {
        return new LootRule(ModIdentity.ID + ":editor_tables/" + source.replace(':', '/') + "_append", source, entity, false, pools);
    }
}
