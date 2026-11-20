package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
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
import yagen.waitmydawn.yagens_attributes.api.WeaponAttackApi;
import yagen.waitmydawn.yagens_attributes.api.WeakPointApi;
import yagen.waitmydawn.yagens_attributes.api.WeakPointHitEvent;
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
    private record Hit(int criticalTier, int statusDuration, List<StatusProcEvent.Application> applications, StatusContext context,
                       double toxinFraction, UUID meleeWeapon, int comboDuration, double comboChance,
                       Map<DamageType, Double> components, WeakPointGeometry.Match weakPoint) {}
    private static final Map<DamageContainer, Hit> INCOMING = new IdentityHashMap<>();
    private static final Map<DamageSource, Map<LivingEntity, Deque<Hit>>> PENDING = new IdentityHashMap<>();
    private static final TagKey<net.minecraft.world.damagesource.DamageType> AREA_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "area_damage"));
    private CombatEvents() {}

    static boolean isAreaDamage(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || source.is(AREA_DAMAGE);
    }

    @SubscribeEvent public static void reload(AddReloadListenerEvent event) { event.addListener(CombatData.WEAPONS); event.addListener(CombatData.MATERIALS); event.addListener(ShieldMaterials.INSTANCE); event.addListener(ModDefinitions.INSTANCE); }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) { ModCommands.register(event.getDispatcher()); }
    @SubscribeEvent public static void tags(net.neoforged.neoforge.event.TagsUpdatedEvent event) { CombatData.MATERIALS.invalidate(); }
    @SubscribeEvent public static void endTick(ServerTickEvent.Post event) { INCOMING.clear(); PENDING.clear(); }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void incoming(LivingIncomingDamageEvent event) {
        if (!ServerConfigs.ENABLE_COMBAT.get() || StatusDamage.isSecondary(event.getSource()) || !(event.getSource().getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        DamageSource source = event.getSource();
        var impact = WeaponAttackApi.impact(source, event.getEntity());
        var shot = impact == null ? RangedShooting.shot(source.getDirectEntity()) : impact.shot();
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
            boolean areaDamage = isAreaDamage(source);
            if (areaDamage) bonuses.replaceAll(bonus -> {
                if (!bonus.containsKey(Stats.DIRECT_DAMAGE)) return bonus;
                var copy = new LinkedHashMap<>(bonus); copy.remove(Stats.DIRECT_DAMAGE); return copy;
            });
            double flatDamage = MeleeEnchantments.take(event.getEntity(), source, event.getAmount());
            int hits = impact == null ? 1 : impact.hits();
            int portions = impact == null ? 1 : impact.portions();
            // Undo the move and pellet scaling of the incoming event so flat bonuses are scaled exactly once.
            double multiplier = impact == null ? 1 : impact.multiplier();
            double scale = (double) portions / hits / multiplier;
            double input = (event.getAmount() - flatDamage) * scale;
            double chance = areaDamage ? 0 : StatusEffects.criticalChance(event.getEntity());
            double extraCritical = StatusEffects.criticalDamage(event.getEntity());
            var hit = Combat.prepare(input, profile, bonuses, Map.of(), chance, extraCritical, flatDamage * scale);
            boolean hasShot = shot != null && shot.profile() != null;
            double share = (hasShot ? shot.scale() : 1) * multiplier / portions;
            var amounts = hit.components();
            double toxin = hit.componentTotal() <= 0 ? 0 : Math.min(1, Math.max(0, amounts.get(DamageType.TOXIN) / hit.componentTotal()));
            UUID weaponId = hasShot ? shot.weapon() : meleeWeapon != null ? meleeWeapon : rangedWeapon;
            var kind = hasShot ? shot.attackKind() : meleeWeapon != null ? AttackKind.MELEE : rangedWeapon != null ? AttackKind.RANGED : AttackKind.UNKNOWN;
            List<DamageType> forced = impact == null ? List.of() : impact.forcedProcs();
            var applications = new ArrayList<StatusProcEvent.Application>();
            var contexts = hits > 1 ? new HashMap<Integer, StatusContext>() : null;
            var random = (java.util.function.DoubleSupplier) player.getRandom()::nextDouble;
            double damage = 0;
            int criticalTier = 0;
            for (int i = 0; i < hits; i++) {
                var roll = hit.rollHit(random);
                damage += roll.damage() * share;
                criticalTier = Math.max(criticalTier, roll.criticalTier());
                if (roll.durationSeconds() <= 0 || (roll.procs().isEmpty() && forced.isEmpty())) continue;
                int tier = roll.criticalTier();
                var context = contexts == null ? null : contexts.get(tier);
                if (context == null) {
                    context = new StatusContext(Combat.statusBases(input, profile, bonuses, tier, extraCritical, share), tier, weaponId, kind);
                    if (contexts != null) contexts.put(tier, context);
                }
                List<DamageType> types = roll.procs();
                if (!forced.isEmpty()) {
                    types = new ArrayList<>(types);
                    types.addAll(forced);
                }
                applications.add(new StatusProcEvent.Application(types, context));
            }
            if (shot != null && shot.attackKind() == AttackKind.MELEE && shot.weapon() != null) {
                meleeWeapon = shot.weapon(); comboDuration = ComboService.duration(bonuses);
            }
            var weakPoint = areaDamage ? null : WeakPointApi.current(source, event.getEntity());
            var context = new StatusContext(Map.of(), criticalTier, weaponId, kind);
            INCOMING.put(event.getContainer(), new Hit(criticalTier, (int) Math.ceil(hit.statusDuration() * 20), applications, context, toxin, meleeWeapon, comboDuration,
                    Math.min(64, Stats.resolve(1, Stats.COMBO_COUNT_CHANCE, bonuses)), amounts, weakPoint));
            event.setAmount((float) Math.min(Float.MAX_VALUE, damage));
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
        int criticalTier = hit == null ? event.getSource() instanceof StatusDamage secondary ? secondary.criticalTier : 0 : hit.criticalTier();
        if (stack != null && stack.isEmpty()) targets.remove(event.getEntity());
        if (targets != null && targets.isEmpty()) PENDING.remove(event.getSource());
        float dealt = event.getNewDamage() + event.getReduction(Reduction.ABSORPTION);
        if (dealt <= 0) return;
        if (hit != null && hit.meleeWeapon != null) ComboService.hit(attacker, hit.meleeWeapon, hit.comboDuration, hit.comboChance);
        if (hit != null && hit.weakPoint() != null) {
            var point = hit.weakPoint();
            NeoForge.EVENT_BUS.post(new WeakPointHitEvent(attacker, event.getEntity(), event.getSource(), point.region(), point.location(),
                    dealt, hit.context().weapon(), hit.context().attackKind()));
        }
        var secondaryType = StatusDamage.damageType(event.getSource());
        int mask = secondaryType == null ? 0 : 1 << secondaryType.ordinal();
        if (hit != null && !hit.applications().isEmpty()) {
            var procs = new StatusProcEvent(attacker, event.getEntity(), hit.statusDuration(), hit.applications());
            for (DamageType type : procs.procs()) mask |= 1 << type.ordinal();
            NeoForge.EVENT_BUS.post(procs);
        }
        var packet = new DamagePayload(event.getEntity().getId(), event.getEntity().position().add(0, event.getEntity().getBbHeight() * 0.65, 0), attacker.getUUID(), dealt, criticalTier, mask);
        WorldState state = WorldState.get(attacker.server);
        int range = ServerConfigs.DAMAGE_VISIBILITY_RANGE.get();
        for (ServerPlayer viewer : ((net.minecraft.server.level.ServerLevel) event.getEntity().level()).players()) {
            boolean own = viewer == attacker;
            if (viewer.distanceToSqr(event.getEntity()) <= range * range && (own || (state.visibility(attacker.getUUID()).shareOwn() && state.visibility(viewer.getUUID()).seeOthers())))
                PacketDistributor.sendToPlayer(viewer, packet);
        }
    }
}
