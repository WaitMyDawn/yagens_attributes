package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import yagen.waitmydawn.yagens_attributes.core.loot.BuiltinLoot;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;

final class LootRuntimeChecks {
    static int run(ServerLevel level, JsonObject report) {
        var rule = BuiltinLoot.rules().get(0); var entity = new EnderMan(EntityType.ENDERMAN, level);
        var params = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic()).create(LootContextParamSets.ENTITY);
        var source = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(rule.sourceTable())));
        int hits = 0, checks = 0;
        for (int i = 1; i <= 4096; i++) {
            var loot = source.getRandomItems(params, i);
            long forma = loot.stream().filter(stack -> stack.is(ModItems.FORMA.get())).count();
            if (forma > 1 || loot.stream().filter(stack -> stack.is(ModItems.FORMA.get())).anyMatch(stack -> stack.getCount() != 1)) throw new AssertionError("Duplicate Forma reward");
            hits += (int) forma; checks++;
        }
        if (hits < 300 || hits > 520) throw new AssertionError("Unexpected Forma frequency: " + hits); checks++;
        var zombie = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("minecraft:entities/zombie")));
        if (zombie.getRandomItems(params, 47).stream().anyMatch(stack -> stack.is(ModItems.FORMA.get()))) throw new AssertionError("Forma appended to unrelated entity table"); checks++;
        var cow = new net.minecraft.world.entity.animal.Cow(EntityType.COW, level);
        var wrongEntity = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, cow).withParameter(LootContextParams.ORIGIN, cow.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic()).create(LootContextParamSets.ENTITY);
        for (int seed = 1; seed <= 64; seed++) if (source.getRandomItems(wrongEntity, seed).stream().anyMatch(stack -> stack.is(ModItems.FORMA.get()))) throw new AssertionError("Shared table bypassed source entity selection");
        checks++;
        if (rule.chance() != .1 || rule.count() != 1) throw new AssertionError("Unexpected authoring default"); checks++;
        checks += RewardRuntimeChecks.run(level, params, report);
        checks += PublishedLootRuntimeChecks.run(level, report);
        report.addProperty("enderman_loot_rolls", 4096); report.addProperty("enderman_forma_rewards", hits); report.addProperty("loot_checks", checks); return checks;
    }
}
