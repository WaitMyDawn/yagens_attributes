package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.loot.*;
import yagen.waitmydawn.yagens_attributes.datagen.ModRewardJson;
import yagen.waitmydawn.yagens_attributes.loot.TableResultModifier;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;

final class RewardRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, LootParams params, JsonObject report) {
        var catalog = ModDefinitions.INSTANCE.catalog(); var observed = EnumSet.noneOf(ModInfo.Rarity.class); int melee = 0, ranged = 0;
        for (var template : BuiltinLoot.rewardTemplates().entrySet()) {
            var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(template.getKey())));
            for (int seed = 1; seed <= 128; seed++) {
                var result = generate(table, params, seed); require(result.size() == 1 && result.get(0).getCount() == 1, "Reward must emit one physical card"); var stack = result.get(0); var spec = template.getValue();
                if (spec.method() == ModReward.Method.VEILED_RIVEN) { if (stack.is(ModItems.UNKNOWN_MELEE_RIVEN.get())) melee++; else if (stack.is(ModItems.UNKNOWN_RANGED_RIVEN.get())) ranged++; else throw new AssertionError("Unrecognized veiled Riven"); }
                else {
                    var data = stack.get(Components.MOD_DATA.get()); require(data != null, "Reward lost Mod components"); var definition = catalog.definitions().get(data.type().toString()); require(definition != null && data.level() >= 0 && data.level() <= definition.maxLevel(), "Invalid reward rank or definition");
                    if (spec.method() == ModReward.Method.WEIGHTED) { require(ModReward.WEIGHTED_QUALITIES.contains(definition.rarity()), "Unexpected weighted quality"); observed.add(definition.rarity()); }
                    if (spec.method() == ModReward.Method.SPECIFIC) require(data.type().toString().equals(spec.mod()), "Specific method changed identity");
                    if (spec.method() == ModReward.Method.GALVANIZED) require(definition.rarity() == ModInfo.Rarity.GALVANIZED, "Non-Galvanized reward");
                }
            }
        }
        require(observed.equals(ModReward.WEIGHTED_QUALITIES), "Quality pool coverage missing: " + observed); require(melee > 0 && ranged > 0, "Veiled Riven pool selection missing");
        var spec = new ModReward(ModReward.Method.SPECIFIC, Map.of(), new yagen.waitmydawn.yagens_attributes.core.mods.UniversalNanocubeMod().id(), 100, 100);
        var full = LootTable.DIRECT_CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), ModRewardJson.table(spec)).getOrThrow();
        var card = generate(full, params, 1).get(0).get(Components.MOD_DATA.get()); require(card.level() == catalog.definitions().get(spec.mod()).maxLevel(), "100% must yield maximum rank");
        var key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("yagens_attributes:rewards/specific_mod"));
        var original = new ObjectArrayList<ItemStack>(); original.add(new ItemStack(Items.STONE, 7));
        var append = new TableResultModifier(new LootItemCondition[0], key, false).apply(original, context(params, 10)); require(append.stream().anyMatch(s -> s.is(Items.STONE) && s.getCount() == 7) && append.size() == 2, "Append lost original output");
        var replace = new TableResultModifier(new LootItemCondition[0], key, true).apply(original, context(params, 11)); require(replace.size() == 1 && !replace.get(0).is(Items.STONE), "Replacement retained old output"); require(original.stream().anyMatch(s -> s.is(Items.STONE)), "Replacement mutated the previous result object");
        var missing = new TableResultModifier(new LootItemCondition[0], ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("example:missing_reward_table")), true).apply(original, context(params, 12)); require(missing == original, "Missing replacement table erased source output");
        var gated = JsonParser.parseString("{\"pools\":[{\"rolls\":2,\"conditions\":[{\"condition\":\"minecraft:random_chance\",\"chance\":0.8}],\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"minecraft:stone\"}]}]}").getAsJsonObject();
        var perEntry = gated.deepCopy(); var p = perEntry.getAsJsonArray("pools").get(0).getAsJsonObject(); p.getAsJsonArray("entries").get(0).getAsJsonObject().add("conditions", p.remove("conditions"));
        var poolTable = LootTable.DIRECT_CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), gated).getOrThrow();
        var entryTable = LootTable.DIRECT_CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), perEntry).getOrThrow(); boolean one = false, zero = false, two = false;
        for (int seed = 1; seed <= 128; seed++) { int poolCount = generate(poolTable, params, seed).size(); require(poolCount == 0 || poolCount == 2, "Pool chance was evaluated per roll"); int entryCount = generate(entryTable, params, seed).size(); one |= entryCount == 1; zero |= poolCount == 0; two |= poolCount == 2; }
        require(one && zero && two, "Chance placement did not change behavior");
        report.addProperty("reward_method_checks", checks); report.addProperty("veiled_melee", melee); report.addProperty("veiled_ranged", ranged); return checks;
    }
    // Legacy random sources correlate on the first sample of adjacent seeds.
    private static LootContext context(LootParams params, long seed) { return new LootContext.Builder(params).withOptionalRandomSeed(seed * 0x9E3779B97F4A7C15L).create(Optional.empty()); }
    private static List<ItemStack> generate(LootTable table, LootParams params, long seed) { var result = new ArrayList<ItemStack>(); table.getRandomItemsRaw(context(params, seed), result::add); return result; }
    private static void require(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
}
