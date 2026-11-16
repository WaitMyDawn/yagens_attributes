package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonParser;
import yagen.waitmydawn.yagens_attributes.core.loot.*;
import yagen.waitmydawn.yagens_attributes.datagen.FixedLootJson;
import java.nio.file.*;

public final class LootContracts {
    public static void run() throws Exception {
        int checks = 0;
        for (var entry : BuiltinLoot.rewardTemplates().entrySet()) {
            var spec = entry.getValue(); var encoded = yagen.waitmydawn.yagens_attributes.datagen.ModRewardJson.entry(spec);
            if (!yagen.waitmydawn.yagens_attributes.datagen.ModRewardJson.read(encoded).equals(spec)) throw new AssertionError("Reward method codec round trip"); checks++;
            String[] key = entry.getKey().split(":", 2); var generated = JsonParser.parseString(Files.readString(Path.of("../../common/src/generated/resources/data/" + key[0] + "/loot_table/" + key[1] + ".json")));
            if (!generated.equals(yagen.waitmydawn.yagens_attributes.datagen.ModRewardJson.table(spec))) throw new AssertionError("Reward template datagen differs"); checks++;
        }
        for (var rule : BuiltinLoot.rules()) {
            if (!FixedLootJson.read(FixedLootJson.describe(rule)).equals(rule)) throw new AssertionError("Loot descriptor round trip"); checks++;
            var root = Path.of("../../common/src/generated/resources/data"); String[] id = rule.id().split(":", 2), table = rule.rewardTable().split(":", 2);
            var generated = JsonParser.parseString(Files.readString(root.resolve(table[0] + "/loot_table/" + table[1] + ".json")));
            if (!generated.equals(FixedLootJson.table(rule))) throw new AssertionError("Generated reward differs"); checks++;
            var modifier = JsonParser.parseString(Files.readString(root.resolve(id[0] + "/loot_modifiers/" + id[1] + ".json")));
            if (!modifier.equals(FixedLootJson.modifier(rule))) throw new AssertionError("Generated modifier differs"); checks++;
            var index = JsonParser.parseString(Files.readString(root.resolve("neoforge/loot_modifiers/global_loot_modifiers.json"))).getAsJsonObject();
            if (index.get("replace").getAsBoolean() || !index.getAsJsonArray("entries").contains(new com.google.gson.JsonPrimitive(rule.id()))) throw new AssertionError("Missing append-only loot modifier"); checks++;
        }
        try (var stream = LootContracts.class.getResourceAsStream("/loot/confirmed-first-batch.json")) {
            if (stream == null) throw new AssertionError("Missing confirmed loot fixture");
            var expected = JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var originals = new java.util.HashMap<String, com.google.gson.JsonObject>();
            expected.getAsJsonArray("operations").forEach(op -> originals.put(op.getAsJsonObject().get("table_id").getAsString(), op.getAsJsonObject()));
            var root = Path.of("../../common/src/generated/resources/data"); var index = JsonParser.parseString(Files.readString(root.resolve("neoforge/loot_modifiers/global_loot_modifiers.json"))).getAsJsonObject();
            var ids = new java.util.HashSet<String>(); index.getAsJsonArray("entries").forEach(id -> { if (!ids.add(id.getAsString())) throw new AssertionError("Duplicate global modifier"); });
            if (index.get("replace").getAsBoolean() || ids.size() != BuiltinLoot.rules().size() + BuiltinLoot.additionalRules().size()) throw new AssertionError("Loot modifier index was not merged"); checks++;
            if (originals.size() != BuiltinLoot.additionalRules().size()) throw new AssertionError("Confirmed loot coverage differs"); checks++;
            for (var rule : BuiltinLoot.additionalRules()) {
                var operation = originals.remove(rule.id());
                if (operation == null || !operation.get("table").equals(yagen.waitmydawn.yagens_attributes.datagen.LootRuleJson.table(rule))) throw new AssertionError("Java loot defaults differ from confirmed draft: " + rule.id()); checks++;
                var target = operation.getAsJsonObject("target");
                if (!rule.sourceTable().equals(target.get("table").getAsString()) || !rule.entity().equals(target.get("kind").getAsString().equals("mob") ? target.get("id").getAsString() : "") || rule.replace()) throw new AssertionError("Loot binding differs: " + rule.id()); checks++;
                String[] id = rule.id().split(":", 2);
                var generated = JsonParser.parseString(Files.readString(root.resolve(id[0] + "/loot_table/" + id[1] + ".json")));
                if (!generated.equals(operation.get("table"))) throw new AssertionError("Generated table differs: " + rule.id()); checks++;
                var modifier = JsonParser.parseString(Files.readString(root.resolve(id[0] + "/loot_modifiers/" + id[1] + ".json")));
                if (!modifier.equals(yagen.waitmydawn.yagens_attributes.datagen.LootRuleJson.modifier(rule)) || !ids.contains(rule.id())) throw new AssertionError("Missing/mismatched loot binding: " + rule.id()); checks++;
            }
            if (!originals.isEmpty()) throw new AssertionError("Unwritten confirmed rules"); checks++;
        }
        System.out.println("Passed " + checks + " generated additional loot contracts");
    }
}
