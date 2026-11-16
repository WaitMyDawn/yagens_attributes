package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.api.StatusApi;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ConditionalModService {
    private ConditionalModService() {}
    private static long now(ServerPlayer player) { return player.server.overworld().getGameTime(); }
    private static List<KillBuffState.Key> modifiers(ServerPlayer player, ItemStack weapon) {
        UUID id = weapon.get(Components.WEAPON_ID.get());
        if (id == null) return List.of();
        String item = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        var result = new ArrayList<KillBuffState.Key>();
        var catalog = ModDefinitions.INSTANCE.catalog();
        for (var entry : WorldState.get(player.server).ledger.installed(id)) {
            if (!entry.info().defined() || !entry.info().compatibleItem(item)) continue;
            try {
                var conditional = catalog.card(entry.info(), entry.info().level()).info().details().conditional();
                for (int i = 0; i < conditional.size(); i++) result.add(new KillBuffState.Key(id, entry.id(), i, conditional.get(i)));
            } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
    private static List<KillBuffState.Key> rangedModifiers(ServerPlayer player, UUID weapon) {
        if (weapon == null) return List.of();
        var result = new ArrayList<KillBuffState.Key>();
        for (var entry : WorldState.get(player.server).ledger.installed(weapon)) if (entry.info().defined()) {
            try {
                var modifiers = ModDefinitions.INSTANCE.catalog().card(entry.info(), entry.info().level()).info().details().conditional();
                for (int i = 0; i < modifiers.size(); i++) if (modifiers.get(i).condition().trigger() == ConditionalModifier.Trigger.RANGED_KILLS)
                    result.add(new KillBuffState.Key(weapon, entry.id(), i, modifiers.get(i)));
            } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
    public static List<Map<String, Bonus>> sources(ServerPlayer player, ItemStack weapon, List<Map<String, Bonus>> base, LivingEntity target) {
        if (!ServerConfigs.ENABLE_COMBAT.get()) return base;
        var keys = modifiers(player, weapon);
        if (keys.isEmpty()) return base;
        var result = new ArrayList<>(base);
        int types = -1; long now = now(player);
        for (var key : keys) {
            var modifier = key.modifier();
            int count = 0;
            if (modifier.condition().trigger() == ConditionalModifier.Trigger.TARGET_STATUS_TYPES && target != null) {
                if (types < 0) types = StatusApi.distinctTypes(target);
                count = types;
            } else if ((modifier.condition().trigger() == ConditionalModifier.Trigger.MELEE_KILLS || modifier.condition().trigger() == ConditionalModifier.Trigger.RANGED_KILLS) && player.hasData(RangedAttachments.KILL_BUFFS)) {
                count = player.getData(RangedAttachments.KILL_BUFFS).counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).count(now);
            }
            if (count > 0) result.add(modifier.source(count));
        }
        return List.copyOf(result);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer dead && dead.hasData(RangedAttachments.KILL_BUFFS)) dead.getData(RangedAttachments.KILL_BUFFS).counters.clear();
        var source = event.getSource();
        if (!ServerConfigs.ENABLE_COMBAT.get() || !(source.getEntity() instanceof ServerPlayer player) || player == event.getEntity() || !player.isAlive()) return;
        boolean melee = !StatusDamage.isSecondary(source) && source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK);
        UUID ranged = null;
        var shot = RangedShooting.shot(source.getDirectEntity());
        if (shot != null) ranged = shot.weapon();
        else if (source instanceof StatusDamage status) ranged = status.weapon;
        else if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) && source.getWeaponItem() != null) ranged = source.getWeaponItem().get(Components.WEAPON_ID.get());
        var keys = melee ? modifiers(player, player.getMainHandItem()) : rangedModifiers(player, ranged);
        for (var key : keys) if (key.modifier().condition().trigger() == (melee ? ConditionalModifier.Trigger.MELEE_KILLS : ConditionalModifier.Trigger.RANGED_KILLS)) {
            var counters = player.getData(RangedAttachments.KILL_BUFFS).counters;
            long now = now(player);
            counters.entrySet().removeIf(entry -> entry.getValue().count(now) == 0);
            if (!counters.containsKey(key) && counters.size() >= 256) continue;
            counters.put(key, counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).hit(now, key.modifier().condition()));
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.hasData(RangedAttachments.KILL_BUFFS)) {
            long now = now(player);
            var ledger = WorldState.get(player.server).ledger;
            player.getData(RangedAttachments.KILL_BUFFS).counters.entrySet().removeIf(entry -> {
                if (!ServerConfigs.ENABLE_COMBAT.get() || !player.isAlive() || entry.getValue().count(now) == 0) return true;
                try {
                    var installed = ledger.entry(entry.getKey().mod());
                    return !entry.getKey().weapon().equals(installed.weapon()) || installed.info().details() == null
                            || !installed.info().details().conditional().contains(entry.getKey().modifier());
                } catch (IllegalArgumentException ignored) { return true; }
            });
        }
    }
}
