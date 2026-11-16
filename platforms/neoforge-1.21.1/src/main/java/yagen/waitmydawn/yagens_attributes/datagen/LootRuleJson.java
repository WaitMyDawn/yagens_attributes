package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.loot.LootRule;
import java.util.*;

/** Ordered conditions/functions retain their native scope and evaluation order. */
public final class LootRuleJson {
    private LootRuleJson() {}
    public static JsonObject table(LootRule rule) {
        var result = new JsonObject(); result.addProperty("type", "minecraft:generic"); var pools = new JsonArray();
        for (var pool : rule.pools()) {
            var json = new JsonObject(); json.add("rolls", range(pool.rolls())); var entries = new JsonArray();
            pool.entries().forEach(entry -> entries.add(entry(entry))); json.add("entries", entries); conditions(json, pool.conditions()); pools.add(json);
        }
        result.add("pools", pools); return result;
    }
    public static JsonObject entry(LootRule.Entry entry) {
        JsonObject json;
        if (entry.output() instanceof LootRule.Item item) { json = new JsonObject(); json.addProperty("type", "minecraft:item"); json.addProperty("name", item.id()); }
        else if (entry.output() instanceof LootRule.Mod mod) json = ModRewardJson.entry(mod.reward());
        else throw new IllegalArgumentException("Unsupported loot output");
        if (entry.weight() != null) json.addProperty("weight", entry.weight()); conditions(json, entry.conditions());
        if (!entry.functions().isEmpty()) { var functions = new JsonArray(); entry.functions().forEach(function -> functions.add(function(function))); json.add("functions", functions); }
        return json;
    }
    private static JsonElement range(LootRule.Range range) {
        if (range.min() == range.max()) return new JsonPrimitive(range.min());
        var json = new JsonObject(); json.addProperty("type", "minecraft:uniform"); json.addProperty("min", range.min()); json.addProperty("max", range.max()); return json;
    }
    private static void conditions(JsonObject owner, List<LootRule.Condition> conditions) {
        if (!conditions.isEmpty()) { var array = new JsonArray(); conditions.forEach(condition -> array.add(condition(condition))); owner.add("conditions", array); }
    }
    private static JsonObject condition(LootRule.Condition condition) {
        var json = new JsonObject();
        if (condition instanceof LootRule.PlayerKill) json.addProperty("condition", "minecraft:killed_by_player");
        else if (condition instanceof LootRule.Chance chance) { json.addProperty("condition", "minecraft:random_chance"); json.addProperty("chance", chance.value()); }
        else if (condition instanceof LootRule.EnchantedChance chance) {
            json.addProperty("condition", "minecraft:random_chance_with_enchanted_bonus"); json.addProperty("enchantment", chance.enchantment()); json.addProperty("unenchanted_chance", chance.unenchanted());
            var value = new JsonObject(); value.addProperty("type", "minecraft:linear"); value.addProperty("base", chance.firstLevel()); value.addProperty("per_level_above_first", chance.perFurtherLevel()); json.add("enchanted_chance", value);
        } else throw new IllegalArgumentException("Unsupported loot condition");
        return json;
    }
    private static JsonObject function(LootRule.Function function) {
        var json = new JsonObject();
        if (function instanceof LootRule.Count count) { json.addProperty("function", "minecraft:set_count"); json.add("count", range(count.range())); }
        else if (function instanceof LootRule.EnchantedCount count) { json.addProperty("function", "minecraft:enchanted_count_increase"); json.addProperty("enchantment", count.enchantment()); json.add("count", range(count.range())); json.addProperty("limit", count.limit()); }
        else throw new IllegalArgumentException("Unsupported loot function");
        return json;
    }
    public static JsonObject modifier(LootRule rule) {
        var json = new JsonObject(); json.addProperty("type", "yagens_attributes:table_result"); json.addProperty("table", rule.id()); json.addProperty("replace", rule.replace());
        var conditions = new JsonArray(); var table = new JsonObject(); table.addProperty("condition", "neoforge:loot_table_id"); table.addProperty("loot_table_id", rule.sourceTable()); conditions.add(table);
        if (!rule.entity().isEmpty()) { var entity = new JsonObject(); entity.addProperty("condition", "minecraft:entity_properties"); entity.addProperty("entity", "this"); var predicate = new JsonObject(); predicate.addProperty("type", rule.entity()); entity.add("predicate", predicate); conditions.add(entity); }
        json.add("conditions", conditions); return json;
    }
}
