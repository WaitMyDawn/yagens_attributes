package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.gson.*;
import net.minecraft.data.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yagen.waitmydawn.yagens_attributes.core.loot.BuiltinLoot;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class LootDataProvider implements DataProvider {
    private final Path output;
    public LootDataProvider(PackOutput output) { this.output = output.getOutputFolder(); }
    public static void gather(GatherDataEvent event) { event.getGenerator().addProvider(event.includeServer(), new LootDataProvider(event.getGenerator().getPackOutput())); }
    @Override public String getName() { return "Yagen additional loot"; }
    @Override public CompletableFuture<?> run(CachedOutput cache) {
        var tasks = new ArrayList<CompletableFuture<?>>(); var entries = new JsonArray();
        BuiltinLoot.rewardTemplates().forEach((id, spec) -> {
            String[] key = id.split(":", 2); tasks.add(DataProvider.saveStable(cache, ModRewardJson.table(spec), output.resolve("data/" + key[0] + "/loot_table/" + key[1] + ".json")));
        });
        for (var rule : BuiltinLoot.rules()) {
            var item = ResourceLocation.parse(rule.item());
            if (!BuiltInRegistries.ITEM.containsKey(item) || BuiltInRegistries.ITEM.get(item) == Items.AIR
                    || !BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(rule.entity())))
                throw new IllegalArgumentException("Unknown loot item or source entity: " + rule.id());
            String[] id = rule.id().split(":", 2), table = rule.rewardTable().split(":", 2);
            tasks.add(DataProvider.saveStable(cache, FixedLootJson.table(rule), output.resolve("data/" + table[0] + "/loot_table/" + table[1] + ".json")));
            tasks.add(DataProvider.saveStable(cache, FixedLootJson.modifier(rule), output.resolve("data/" + id[0] + "/loot_modifiers/" + id[1] + ".json")));
            entries.add(rule.id());
        }
        var ids = new HashSet<String>(); entries.forEach(id -> ids.add(id.getAsString()));
        for (var rule : BuiltinLoot.additionalRules()) {
            if (!ids.add(rule.id())) throw new IllegalArgumentException("Duplicate loot output: " + rule.id());
            if (!rule.entity().isEmpty() && !BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(rule.entity()))) throw new IllegalArgumentException("Unknown source entity: " + rule.entity());
            for (var pool : rule.pools()) for (var entry : pool.entries()) if (entry.output() instanceof yagen.waitmydawn.yagens_attributes.core.loot.LootRule.Item item) {
                var id = ResourceLocation.parse(item.id()); if (!BuiltInRegistries.ITEM.containsKey(id) || BuiltInRegistries.ITEM.get(id) == Items.AIR) throw new IllegalArgumentException("Unknown loot item: " + item.id());
            }
            String[] id = rule.id().split(":", 2);
            tasks.add(DataProvider.saveStable(cache, LootRuleJson.table(rule), output.resolve("data/" + id[0] + "/loot_table/" + id[1] + ".json")));
            tasks.add(DataProvider.saveStable(cache, LootRuleJson.modifier(rule), output.resolve("data/" + id[0] + "/loot_modifiers/" + id[1] + ".json")));
            entries.add(rule.id());
        }
        var index = new JsonObject(); index.addProperty("replace", false); index.add("entries", entries);
        tasks.add(DataProvider.saveStable(cache, index, output.resolve("data/neoforge/loot_modifiers/global_loot_modifiers.json")));
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }
}
