package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.api.StatusProcEvent;
import yagen.waitmydawn.yagens_attributes.api.StatusContext;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import yagen.waitmydawn.yagens_attributes.command.ModCommands;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.network.DamagePayload;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class CombatEvents {
    private record Hit(Combat.Result result, StatusContext context, double toxinFraction, UUID meleeWeapon, int comboDuration, double comboChance, Map<DamageType, Double> components) {}
    private static final Map<DamageContainer, Hit> INCOMING = new IdentityHashMap<>();
    private static final Map<DamageSource, Map<LivingEntity, Deque<Hit>>> PENDING = new IdentityHashMap<>();
    private CombatEvents() {}

    @SubscribeEvent public static void reload(AddReloadListenerEvent event) { event.addListener(CombatData.WEAPONS); event.addListener(CombatData.MATERIALS); event.addListener(ShieldMaterials.INSTANCE); event.addListener(ModDefinitions.INSTANCE); }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) { ModCommands.register(event.getDispatcher()); }
    @SubscribeEvent public static void tags(net.neoforged.neoforge.event.TagsUpdatedEvent event) { CombatData.MATERIALS.invalidate(); }
    @SubscribeEvent public static void endTick(ServerTickEvent.Post event) { INCOMING.clear(); PENDING.clear(); }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void incoming(LivingIncomingDamageEvent event) {
        if (!ServerConfigs.ENABLE_COMBAT.get() || StatusDamage.isSecondary(event.getSource()) || !(event.getSource().getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        DamageSource source = event.getSource();
        var shot = RangedShooting.shot(source.getDirectEntity());
        if (shot == null) shot = yagen.waitmydawn.yagens_attributes.compat.NativeAttacks.source(source);
        WeaponProfile profile;
        var bonuses = new ArrayList<Map<String, Bonus>>();
        UUID meleeWeapon = null; int comboDuration = 0;
        ItemStack liveWeapon = null;
        UUID rangedWeapon = shot == null ? null : shot.weapon();
        if (shot != null && shot.profile() != null) { profile = shot.profile(); bonuses.addAll(shot.sources()); }
        else {
            ItemStack weapon;
            if (source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK)) weapon = player.getMainHandItem();
            else if (source.is(DamageTypeTags.IS_PROJECTILE)) weapon = source.getWeaponItem();
            else return;
            if (weapon == null || weapon.isEmpty()) return;
            liveWeapon = weapon;
            profile = CombatData.WEAPONS.weapon(weapon); if (profile == null) return;
            WorldState state = WorldState.get(player.server);
            UUID id = weapon.get(Components.WEAPON_ID.get());
            if (source.is(DamageTypeTags.IS_PROJECTILE)) rangedWeapon = id;
            if (id != null) bonuses.addAll(state.ledger.bonuses(id, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(), profile.category()));
            bonuses.add(state.bonuses(player.getUUID()));
            if (source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK) && ModInfo.Category.MELEE.contains(profile.category()) && WeaponEligibility.operable(weapon, profile)) {
                meleeWeapon = Components.identify(weapon);
            }
        }
        try {
            if (meleeWeapon != null) {
                comboDuration = ComboService.duration(bonuses);
                bonuses = new ArrayList<>(ComboService.sources(player, meleeWeapon, bonuses));
                bonuses = new ArrayList<>(ConditionalModService.sources(player, player.getMainHandItem(), bonuses, event.getEntity()));
            } else if (shot != null && shot.profile() != null) {
                bonuses.addAll(ConditionalModService.targetSources(shot.targetModifiers(), event.getEntity()));
            } else if (liveWeapon != null) {
                bonuses = new ArrayList<>(ConditionalModService.sources(player, liveWeapon, bonuses, event.getEntity()));
            }
            if (source.is(DamageTypeTags.IS_EXPLOSION)) bonuses.replaceAll(bonus -> {
                if (!bonus.containsKey(Stats.DIRECT_DAMAGE)) return bonus;
                var copy = new LinkedHashMap<>(bonus); copy.remove(Stats.DIRECT_DAMAGE); return copy;
            });
            double input = event.getAmount();
            Map<DamageType, Double> material = Map.of();
            double chance = source.is(DamageTypeTags.IS_EXPLOSION) ? 0 : StatusEffects.criticalChance(event.getEntity());
            double extraCritical = StatusEffects.criticalDamage(event.getEntity());
            Combat.Result result = Combat.calculate(input, profile, bonuses, material, player.getRandom()::nextDouble, chance, extraCritical);
            double share = shot != null && shot.profile() != null ? shot.scale() : 1;
            var amounts = Combat.components(input, profile, bonuses);
            double adjusted = 0;
            for (var entry : amounts.entrySet()) adjusted += entry.getValue() * material.getOrDefault(entry.getKey(), 1D);
            double toxin = adjusted <= 0 ? 0 : Math.min(1, Math.max(0, amounts.get(DamageType.TOXIN) * material.getOrDefault(DamageType.TOXIN, 1D) / adjusted));
            var bases = Combat.statusBases(input, profile, bonuses, result.criticalTier(), extraCritical, share);
            if (shot != null && shot.profile() != null) result = new Combat.Result(Math.min(Float.MAX_VALUE, result.damage() * shot.scale()), result.criticalTier(), result.procs(), result.durationSeconds());
            event.setAmount((float) result.damage());
            var context = shot != null && shot.profile() != null ? new StatusContext(bases, result.criticalTier(), shot.weapon(), shot.attackKind())
                    : meleeWeapon == null ? new StatusContext(bases, result.criticalTier(), rangedWeapon)
                    : new StatusContext(bases, result.criticalTier(), meleeWeapon, AttackKind.MELEE);
            if (shot != null && shot.attackKind() == AttackKind.MELEE && shot.weapon() != null) {
                meleeWeapon = shot.weapon(); comboDuration = ComboService.duration(bonuses);
            }
            INCOMING.put(event.getContainer(), new Hit(result, context, toxin, meleeWeapon, comboDuration,
                    Math.min(64, Stats.resolve(1, Stats.COMBO_COUNT_CHANCE, bonuses)), amounts));
        } catch (IllegalArgumentException exception) {
            if (shot != null && shot.profile() != null) event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() * shot.scale()));
            YagensAttributes.LOGGER.error("Rejected combat modifiers for player {}: {}", player.getUUID(), exception.getMessage());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void before(LivingDamageEvent.Pre event) {
        // Incoming may be blocked before damage processing. Bind only the exact surviving damage container.
        var hit = INCOMING.remove(event.getContainer());
        if (hit != null) PENDING.computeIfAbsent(event.getSource(), key -> new IdentityHashMap<>())
                .computeIfAbsent(event.getEntity(), key -> new ArrayDeque<>()).push(hit);
    }

    public static double toxinFraction(DamageSource source, LivingEntity target) {
        var targets = PENDING.get(source); var stack = targets == null ? null : targets.get(target);
        return stack == null || stack.isEmpty() ? 0 : stack.peek().toxinFraction;
    }
    public static Map<DamageType, Double> components(DamageSource source, LivingEntity target) {
        var typed = StatusDamage.damageType(source);
        if (typed != null) return Map.of(typed, 1D);
        var targets = PENDING.get(source); var stack = targets == null ? null : targets.get(target);
        return stack == null || stack.isEmpty() ? Map.of() : stack.peek().components;
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void after(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        var targets = PENDING.get(event.getSource());
        var stack = targets == null ? null : targets.get(event.getEntity());
        Hit hit = stack == null || stack.isEmpty() ? null : stack.pop();
        Combat.Result result = hit == null ? new Combat.Result(event.getNewDamage(), event.getSource() instanceof StatusDamage secondary ? secondary.criticalTier : 0, List.of(), 0) : hit.result;
        if (stack != null && stack.isEmpty()) targets.remove(event.getEntity());
        if (targets != null && targets.isEmpty()) PENDING.remove(event.getSource());
        float dealt = event.getNewDamage() + event.getReduction(Reduction.ABSORPTION);
        if (dealt <= 0) return;
        if (hit != null && hit.meleeWeapon != null) ComboService.hit(attacker, hit.meleeWeapon, hit.comboDuration, hit.comboChance);
        var secondaryType = StatusDamage.damageType(event.getSource());
        int mask = secondaryType == null ? 0 : 1 << secondaryType.ordinal();
        if (result.durationSeconds() > 0) {
            for (DamageType type : result.procs()) mask |= 1 << type.ordinal();
            if (!result.procs().isEmpty()) NeoForge.EVENT_BUS.post(new StatusProcEvent(attacker, event.getEntity(), result.procs(), (int) Math.ceil(result.durationSeconds() * 20), hit == null ? StatusContext.EMPTY : hit.context));
        }
        var packet = new DamagePayload(event.getEntity().getId(), event.getEntity().position().add(0, event.getEntity().getBbHeight() * 0.65, 0), attacker.getUUID(), dealt, result.criticalTier(), mask);
        WorldState state = WorldState.get(attacker.server);
        int range = ServerConfigs.DAMAGE_VISIBILITY_RANGE.get();
        for (ServerPlayer viewer : ((net.minecraft.server.level.ServerLevel) event.getEntity().level()).players()) {
            boolean own = viewer == attacker;
            if (viewer.distanceToSqr(event.getEntity()) <= range * range && (own || (state.visibility(attacker.getUUID()).shareOwn() && state.visibility(viewer.getUUID()).seeOthers())))
                PacketDistributor.sendToPlayer(viewer, packet);
        }
    }
}
