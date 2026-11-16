package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import yagen.waitmydawn.yagens_attributes.core.ModIdentity;
import yagen.waitmydawn.yagens_attributes.core.loot.ModReward;
import yagen.waitmydawn.yagens_attributes.data.ModRewardCodec;

/** Shared by retained data providers and removable authoring adapters. */
public final class ModRewardJson {
    private ModRewardJson() {}
    public static JsonObject entry(ModReward reward) {
        var entry = new JsonObject(); entry.addProperty("type", ModIdentity.ID + ":mod_reward");
        var parameters = ModRewardCodec.CODEC.encodeStart(JsonOps.INSTANCE, reward).getOrThrow().getAsJsonObject();
        if (reward.method() != ModReward.Method.VEILED_RIVEN) { parameters.addProperty("min_rank_percent", reward.minPercent()); parameters.addProperty("max_rank_percent", reward.maxPercent()); }
        entry.add("reward", parameters); return entry;
    }
    public static ModReward read(JsonObject entry) { return ModRewardCodec.CODEC.parse(JsonOps.INSTANCE, entry.get("reward")).getOrThrow(); }
    public static JsonObject table(ModReward reward) {
        var table = new JsonObject(); table.addProperty("type", "minecraft:generic"); var pools = new JsonArray(); var pool = new JsonObject(); pool.addProperty("rolls", 1); var entries = new JsonArray(); entries.add(entry(reward)); pool.add("entries", entries); pools.add(pool); table.add("pools", pools); return table;
    }
}
