package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.api.StatusApi;
import yagen.waitmydawn.yagens_attributes.api.WeakPointApi;
import yagen.waitmydawn.yagens_attributes.api.WeakPointHitEvent;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;
import java.util.function.Predicate;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ConditionalModService {
    private ConditionalModService() {}
    private static long now(ServerPlayer player) { return player.server.overworld().getGameTime(); }
    private static List<KillBuffState.Key> modifiers(ServerPlayer player, ItemStack weapon) {
        UUID id = weapon.get(Components.WEAPON_ID.get());
        if (id == null) return List.of();
        String item = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        var profile = CombatData.WEAPONS.weapon(weapon);
        if (profile == null) return List.of();
        var result = new ArrayList<KillBuffState.Key>();
        var catalog = ModDefinitions.INSTANCE.catalog();
        for (var entry : WorldState.get(player.server).ledger.installed(id)) {
            if (!entry.info().defined() || !entry.info().compatibleItem(item) || !entry.info().compatible(profile.category())) continue;
            try {
                var conditional = catalog.card(entry.info(), entry.info().level()).info().details().conditional();
                for (int i = 0; i < conditional.size(); i++) result.add(new KillBuffState.Key(id, entry.id(), i, conditional.get(i)));
            } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
    private static List<KillBuffState.Key> weaponModifiers(ServerPlayer player, UUID weapon) {
        if (weapon == null) return List.of();
        var result = new ArrayList<KillBuffState.Key>();
        for (var entry : WorldState.get(player.server).ledger.installed(weapon)) if (entry.info().defined()) {
            try {
                var modifiers = ModDefinitions.INSTANCE.catalog().card(entry.info(), entry.info().level()).info().details().conditional();
                for (int i = 0; i < modifiers.size(); i++) if (modifiers.get(i).condition().trigger().timed())
                    result.add(new KillBuffState.Key(weapon, entry.id(), i, modifiers.get(i)));
            } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
    public static List<Map<String, Bonus>> sources(ServerPlayer player, ItemStack weapon, List<Map<String, Bonus>> base, LivingEntity target) {
        return sources(player, weapon, base, target, false);
    }
    static List<Map<String, Bonus>> sources(ServerPlayer player, ItemStack weapon, List<Map<String, Bonus>> base,
                                            LivingEntity target, boolean includeRangedLaunchBuffs) {
        if (!ServerConfigs.ENABLE_COMBAT.get()) return base;
        var keys = modifiers(player, weapon);
        if (keys.isEmpty()) return base;
        var result = new ArrayList<>(base);
        int types = -1; long now = now(player);
        for (var key : keys) {
            var modifier = key.modifier();
            var trigger = modifier.condition().trigger();
            if (modifier.condition().needsTarget() && target == null) continue;
            if (trigger.requiresRangedLaunch() && !includeRangedLaunchBuffs) continue;
            int count = 0;
            if (trigger == ConditionalModifier.Trigger.TARGET_STATUS_TYPES && target != null) {
                if (types < 0) types = StatusApi.distinctTypes(target);
                count = types;
            } else if (trigger.timed() && player.hasData(RangedAttachments.KILL_BUFFS)) {
                count = player.getData(RangedAttachments.KILL_BUFFS).counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).count(now, modifier.condition());
            }
            if (modifier.condition().perTargetStatus() && types < 0) types = StatusApi.distinctTypes(target);
            if (count > 0) result.add(modifier.source(count, Math.max(0, types)));
        }
        return List.copyOf(result);
    }
    /** Freeze the relevant buff stacks at launch; target statuses are evaluated separately on impact. */
    public static List<ConditionalModifier> targetModifiers(ServerPlayer player, ItemStack weapon) {
        return targetModifiers(player, weapon, AttackKind.RANGED);
    }

    static List<ConditionalModifier> targetModifiers(ServerPlayer player, ItemStack weapon, AttackKind kind) {
        if (!ServerConfigs.ENABLE_COMBAT.get()) return List.of();
        var result = new ArrayList<ConditionalModifier>();
        long now = now(player);
        for (var key : modifiers(player, weapon)) {
            var modifier = key.modifier();
            if (!modifier.condition().needsTarget()
                    || modifier.condition().trigger().requiresRangedLaunch() && kind != AttackKind.RANGED) continue;
            int count = modifier.condition().onKill() ? player.hasData(RangedAttachments.KILL_BUFFS)
                    ? player.getData(RangedAttachments.KILL_BUFFS).counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).count(now, modifier.condition()) : 0 : 1;
            if (count > 0) result.add(new ConditionalModifier(modifier.stat(), modifier.addition() * count, ConditionalModifier.Condition.STATUS_TYPES));
        }
        return List.copyOf(result);
    }
    public static List<Map<String, Bonus>> targetSources(List<ConditionalModifier> modifiers, LivingEntity target) {
        if (!ServerConfigs.ENABLE_COMBAT.get() || modifiers.isEmpty()) return List.of();
        int types = StatusApi.distinctTypes(target);
        if (types == 0) return List.of();
        return modifiers.stream().map(modifier -> modifier.source(types)).toList();
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer dead && dead.hasData(RangedAttachments.KILL_BUFFS)) dead.getData(RangedAttachments.KILL_BUFFS).counters.clear();
        var source = event.getSource();
        if (!ServerConfigs.ENABLE_COMBAT.get() || !(source.getEntity() instanceof ServerPlayer player) || player == event.getEntity() || !player.isAlive()) return;
        boolean melee = !StatusDamage.isSecondary(source) && source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK);
        UUID weapon = null;
        AttackKind kind = melee ? AttackKind.MELEE : AttackKind.UNKNOWN;
        var shot = RangedShooting.shot(source.getDirectEntity());
        if (shot == null) shot = yagen.waitmydawn.yagens_attributes.compat.NativeAttacks.source(source);
        if (shot != null) melee = false;
        if (source instanceof StatusDamage status) { weapon = status.weapon; kind = status.attackKind; }
        else if (shot != null) { weapon = shot.weapon(); kind = shot.attackKind(); }
        else if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getWeaponItem() != null) {
            weapon = source.getWeaponItem().get(Components.WEAPON_ID.get()); kind = AttackKind.RANGED;
        }
        var keys = melee ? modifiers(player, player.getMainHandItem()) : weaponModifiers(player, weapon);
        AttackKind creditedKind = kind;
        boolean weakPointKill = kind == AttackKind.RANGED && !keys.isEmpty()
                && !StatusDamage.isSecondary(source) && !CombatEvents.isAreaDamage(source)
                && WeakPointApi.current(source, event.getEntity()) != null;
        award(player, keys, trigger -> creditedKind.credits(trigger)
                || weakPointKill && trigger == ConditionalModifier.Trigger.RANGED_WEAK_POINT_KILLS);
    }

    @SubscribeEvent public static void weakPointHit(WeakPointHitEvent event) {
        var player = event.attacker();
        if (!ServerConfigs.ENABLE_COMBAT.get() || event.attackKind() != AttackKind.RANGED || event.damage() <= 0
                || player == event.target() || !player.isAlive()) return;
        award(player, weaponModifiers(player, event.weapon()), trigger -> trigger == ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS);
    }

    private static void award(ServerPlayer player, List<KillBuffState.Key> keys, Predicate<ConditionalModifier.Trigger> qualifies) {
        long now = now(player);
        for (var key : keys) if (qualifies.test(key.modifier().condition().trigger())) {
            var counters = player.getData(RangedAttachments.KILL_BUFFS).counters;
            counters.entrySet().removeIf(entry -> entry.getValue().count(now, entry.getKey().modifier().condition()) == 0);
            if (!counters.containsKey(key) && counters.size() >= 256) continue;
            counters.put(key, counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).hit(now, key.modifier().condition()));
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.hasData(RangedAttachments.KILL_BUFFS)) {
            long now = now(player);
            var ledger = WorldState.get(player.server).ledger;
            player.getData(RangedAttachments.KILL_BUFFS).counters.entrySet().removeIf(entry -> {
                if (!ServerConfigs.ENABLE_COMBAT.get() || !player.isAlive() || entry.getValue().count(now, entry.getKey().modifier().condition()) == 0) return true;
                try {
                    var installed = ledger.entry(entry.getKey().mod());
                    return !entry.getKey().weapon().equals(installed.weapon()) || installed.info().details() == null
                            || !installed.info().details().conditional().contains(entry.getKey().modifier());
                } catch (IllegalArgumentException ignored) { return true; }
            });
        }
        if (event.getEntity() instanceof ServerPlayer player) synchronize(player);
    }
    private static void synchronize(ServerPlayer player) {
        var state = player.getData(RangedAttachments.KILL_BUFFS);
        var held = player.getMainHandItem(); var profile = CombatData.WEAPONS.weapon(held);
        UUID weapon = player.isAlive() && ServerConfigs.ENABLE_COMBAT.get() && WeaponEligibility.operable(held, profile)
                ? held.get(Components.WEAPON_ID.get()) : null;
        var buffs = new ArrayList<WeaponBuff>(); long now = now(player);
        if (weapon != null && !state.counters.isEmpty()) {
            var ledger = WorldState.get(player.server).ledger;
            for (var key : modifiers(player, held)) {
                var rule = key.modifier().condition();
                if (!rule.trigger().timed()) continue;
                var deadlines = state.counters.getOrDefault(key, ConditionalModifier.Counter.EMPTY).expirations(now, rule);
                if (deadlines.isEmpty()) continue;
                var info = ledger.entry(key.mod()).info();
                buffs.add(new WeaponBuff(key.mod(), key.index(), info.details().type(), info.level(), key.modifier().stat(), key.modifier().addition(),
                        rule.perTargetStatus(), rule.durationTicks(), rule.maxStacks(), rule.decay(), deadlines));
            }
        }
        if (Objects.equals(weapon, state.displayedWeapon) && buffs.equals(state.displayed)) return;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new yagen.waitmydawn.yagens_attributes.network.WeaponBuffsPayload(weapon, now, buffs));
        state.displayedWeapon = weapon; state.displayed = List.copyOf(buffs);
    }
}
