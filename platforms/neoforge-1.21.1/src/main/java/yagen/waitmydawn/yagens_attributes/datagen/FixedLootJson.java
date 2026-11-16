package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.loot.FixedLootRule;

/** The same compiler drives data generation and development previews. */
public final class FixedLootJson {
    private FixedLootJson() {}
    public static JsonObject describe(FixedLootRule rule) {
        var json = new JsonObject();
        json.addProperty("id", rule.id()); json.addProperty("entity", rule.entity()); json.addProperty("source_table", rule.sourceTable());
        json.addProperty("item", rule.item()); json.addProperty("chance", rule.chance()); json.addProperty("count", rule.count()); return json;
    }
    public static FixedLootRule read(JsonObject json) {
        if (!json.keySet().equals(java.util.Set.of("id", "entity", "source_table", "item", "chance", "count"))) throw new IllegalArgumentException("Invalid fixed loot fields");
        double count = json.get("count").getAsDouble();
        if (count != (int) count) throw new IllegalArgumentException("Loot count must be an integer");
        return new FixedLootRule(json.get("id").getAsString(), json.get("entity").getAsString(), json.get("source_table").getAsString(), json.get("item").getAsString(), json.get("chance").getAsDouble(), (int) count);
    }
    public static JsonObject table(FixedLootRule rule) {
        var table = new JsonObject(); table.addProperty("type", "minecraft:generic");
        var pool = new JsonObject(); pool.addProperty("rolls", 1);
        var chance = new JsonObject(); chance.addProperty("condition", "minecraft:random_chance"); chance.addProperty("chance", rule.chance());
        var conditions = new JsonArray(); conditions.add(chance); pool.add("conditions", conditions);
        var entry = new JsonObject(); entry.addProperty("type", "minecraft:item"); entry.addProperty("name", rule.item());
        var count = new JsonObject(); count.addProperty("function", "minecraft:set_count"); count.addProperty("count", rule.count());
        var functions = new JsonArray(); functions.add(count); entry.add("functions", functions);
        var entries = new JsonArray(); entries.add(entry); pool.add("entries", entries);
        var pools = new JsonArray(); pools.add(pool); table.add("pools", pools); return table;
    }
    public static JsonObject modifier(FixedLootRule rule) {
        var modifier = new JsonObject(); modifier.addProperty("type", "neoforge:add_table"); modifier.addProperty("table", rule.rewardTable());
        var condition = new JsonObject(); condition.addProperty("condition", "neoforge:loot_table_id"); condition.addProperty("loot_table_id", rule.sourceTable());
        var entity = new JsonObject(); entity.addProperty("condition", "minecraft:entity_properties"); entity.addProperty("entity", "this");
        var predicate = new JsonObject(); predicate.addProperty("type", rule.entity()); entity.add("predicate", predicate);
        var conditions = new JsonArray(); conditions.add(condition); conditions.add(entity); modifier.add("conditions", conditions); return modifier;
    }
}
