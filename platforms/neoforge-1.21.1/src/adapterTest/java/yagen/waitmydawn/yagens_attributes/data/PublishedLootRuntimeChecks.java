package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yagen.waitmydawn.yagens_attributes.core.loot.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;

final class PublishedLootRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("6e6efb27-1ce5-4c07-b7ba-40123d16d790"), "[LootChecks]")); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        int sampled = 0;
        for (var rule : BuiltinLoot.additionalRules()) {
            var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(rule.sourceTable())));
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, player.position());
            if (!rule.entity().isEmpty()) {
                var entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(rule.entity())).create(level); require(entity instanceof LivingEntity, "Missing source entity: " + rule.entity());
                params.withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player)).withParameter(LootContextParams.ATTACKING_ENTITY, player).withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
            }
            LootParams context = params.create(rule.entity().isEmpty() ? LootContextParamSets.CHEST : LootContextParamSets.ENTITY);
            boolean emitted = false;
            for (long seed = 1; seed <= 256; seed++) {
                for (var stack : table.getRandomItems(context, seed * 0x9E3779B97F4A7C15L)) {
                    boolean matches = rule.pools().stream().flatMap(pool -> pool.entries().stream()).anyMatch(entry -> matches(entry.output(), stack));
                    emitted |= matches;
                    if (stack.has(Components.MOD_DATA.get())) { var data = stack.get(Components.MOD_DATA.get()); var definition = ModDefinitions.INSTANCE.catalog().definitions().get(data.type().toString()); require(definition != null && data.level() >= 0 && data.level() <= definition.maxLevel(), "Invalid physical reward card"); }
                }
                sampled++;
            }
            require(emitted, "Published source did not emit its additional rewards: " + rule.sourceTable());
        }
        var dragon = new LootDragon(level); dragon.setPos(player.position()); dragon.hurt(dragon.head, level.damageSources().playerAttack(player), 1_000_000F);
        require(dragon.lootCalls == 1, "Native dragon lethal damage did not invoke its entity loot table exactly once");
        var brute = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("minecraft:piglin_brute")).create(level);
        var sword = new ItemStack(Items.DIAMOND_SWORD); sword.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), 3); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        var bruteParams = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, brute).withParameter(LootContextParams.ORIGIN, player.position()).withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player)).withParameter(LootContextParams.ATTACKING_ENTITY, player).withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).create(LootContextParamSets.ENTITY);
        var bruteTable = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("minecraft:entities/piglin_brute"))); boolean increased = false;
        for (long seed = 1; seed <= 256; seed++) for (var stack : bruteTable.getRandomItems(bruteParams, seed * 0x9E3779B97F4A7C15L)) if (stack.is(ModItems.ENDO.get())) { require(stack.getCount() >= 2 && stack.getCount() <= 9, "Looting count function reordered or out of range"); increased |= stack.getCount() > 6; }
        require(increased, "Looting did not increase the configured base quantity"); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        report.addProperty("published_loot_sources", BuiltinLoot.additionalRules().size()); report.addProperty("published_loot_samples", sampled); report.addProperty("dragon_native_loot_calls", dragon.lootCalls); report.addProperty("published_loot_checks", checks); return checks;
    }
    private static boolean matches(LootRule.Output output, ItemStack stack) {
        if (output instanceof LootRule.Item item) return stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(item.id())));
        return stack.has(Components.MOD_DATA.get()) || stack.is(ModItems.UNKNOWN_MELEE_RIVEN.get()) || stack.is(ModItems.UNKNOWN_RANGED_RIVEN.get());
    }
    private static final class LootDragon extends EnderDragon {
        int lootCalls;
        LootDragon(ServerLevel level) { super(EntityType.ENDER_DRAGON, level); }
        @Override protected void dropFromLootTable(DamageSource source, boolean playerKill) { lootCalls++; super.dropFromLootTable(source, playerKill); }
    }
    private static void require(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
}
