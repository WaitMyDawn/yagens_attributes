package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.api.StatusProcEvent;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.StatusState;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;

import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class StatusEffects {
    private static final TagKey<EntityType<?>> CONTROL_IMMUNE = tag("control_immune");
    private static final TagKey<EntityType<?>> STATUS_IMMUNE = tag("status_immune");
    private static final Map<DamageType, TagKey<EntityType<?>>> IMMUNITIES = new EnumMap<>(DamageType.class);
    private static final Deque<Detonation> BLASTS = new ArrayDeque<>();

    static {
        for (var type : DamageType.values()) IMMUNITIES.put(type, tag("immune_to/" + type.key()));
    }

    private StatusEffects() {
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, id(path));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, path);
    }

    public static StatusState state(Entity entity) {
        return entity.getData(RangedAttachments.STATUS);
    }

    public static int count(LivingEntity target, DamageType type) {
        return target.hasData(RangedAttachments.STATUS) ? state(target).count(type) : 0;
    }

    public static boolean frozen(LivingEntity target) {
        return target.hasData(RangedAttachments.STATUS) && state(target).frozen > 0;
    }

    public static boolean controlled(LivingEntity target) {
        return !target.getType().is(CONTROL_IMMUNE) && target.hasData(RangedAttachments.STATUS) && (state(target).frozen > 0 || state(target).stunned());
    }

    public static boolean preventsRecharge(LivingEntity target) {
        return count(target, DamageType.MAGNETIC) > 0 || frozen(target);
    }

    public static double criticalChance(LivingEntity target) {
        return .05 * count(target, DamageType.PUNCTURE);
    }

    public static double criticalDamage(LivingEntity target) {
        return StatusRules.coldCritical(count(target, DamageType.COLD), frozen(target));
    }

    public static UUID latestOwner(LivingEntity target, DamageType type) {
        var layer = latestLayer(target, type);
        return layer == null ? target.getUUID() : layer.owner();
    }
    public static StatusState.Layer latestLayer(LivingEntity target, DamageType type) {
        if (!target.hasData(RangedAttachments.STATUS)) return null;
        var layers = state(target).layers;
        for (int i = layers.size() - 1; i >= 0; i--) if (layers.get(i).type() == type) return layers.get(i);
        return null;
    }

    public static boolean confusedAttack(LivingEntity attacker, LivingEntity victim) {
        return attacker.hasData(RangedAttachments.STATUS) && victim.getUUID().equals(state(attacker).confusedTarget);
    }

    @SubscribeEvent
    public static void proc(StatusProcEvent event) {
        if (event.durationTicks() <= 0) return;
        var immediate = new ArrayList<StatusState.Layer>();
        for (var type : event.procs()) {
            boolean applied = apply(event.target(), type, event.attacker().getUUID(), event.context().base(type), event.durationTicks(), false, event.context().criticalTier(), event.context().weapon(), event.context().attackKind());
            if (applied && (type == DamageType.ELECTRICITY || type == DamageType.GAS))
                immediate.add(new StatusState.Layer(type, event.attacker().getUUID(), event.context().base(type), 1, 20, 0, event.context().criticalTier(), event.context().weapon(), event.context().attackKind()));
        }
        pulses(event.target(), immediate);
    }

    public static void apply(LivingEntity target, DamageType type, UUID owner, double base, int weaponTicks) {
        tryApply(target, type, owner, base, weaponTicks, 0);
    }

    public static boolean tryApply(LivingEntity target, DamageType type, UUID owner, double base, int weaponTicks, int criticalTier) {
        return apply(target, type, owner, base, weaponTicks, true, criticalTier);
    }

    public static boolean tryApply(LivingEntity target, DamageType type, UUID owner, int weaponTicks, yagen.waitmydawn.yagens_attributes.api.StatusContext context) {
        return apply(target, type, owner, context.base(type), weaponTicks, true, context.criticalTier(), context.weapon(), context.attackKind());
    }

    public static boolean clear(LivingEntity target, DamageType type) {
        if (target.level().isClientSide || !target.hasData(RangedAttachments.STATUS)) return false;
        var value = state(target);
        boolean present = value.count(type) > 0 || switch (type) {
            case COLD -> value.frozen > 0;
            case HEAT -> value.heatStrip > 0 || value.heatPanic > 0;
            case IMPACT -> value.impactStun > 0;
            case ELECTRICITY -> value.electricStun > 0;
            case RADIATION -> value.confusedTarget != null;
            default -> false;
        };
        if (!present) return false;
        value.clear(type);
        if (type == DamageType.RADIATION) stopConfusion(target);
        update(target);
        return true;
    }

    public static boolean clearAll(LivingEntity target) {
        if (target.level().isClientSide || !target.hasData(RangedAttachments.STATUS)) return false;
        var value = state(target);
        if (value.empty() && value.confusedTarget == null) return false;
        for (var type : DamageType.values()) value.clear(type);
        stopConfusion(target);
        update(target);
        return true;
    }

    private static void stopConfusion(LivingEntity target) {
        if (target instanceof Mob mob)
            mob.targetSelector.getAvailableGoals().stream().filter(goal -> goal.getGoal() instanceof ConfusionGoal).forEach(goal -> goal.stop());
    }

    private static boolean apply(LivingEntity target, DamageType type, UUID owner, double base, int weaponTicks, boolean immediate, int criticalTier) {
        return apply(target, type, owner, base, weaponTicks, immediate, criticalTier, null, AttackKind.UNKNOWN);
    }
    private static boolean apply(LivingEntity target, DamageType type, UUID owner, double base, int weaponTicks, boolean immediate, int criticalTier, UUID weapon, AttackKind attackKind) {
        Objects.requireNonNull(target); Objects.requireNonNull(type); Objects.requireNonNull(owner);
        Objects.requireNonNull(attackKind).validateWeapon(weapon);
        if (!Double.isFinite(base) || base < 0 || base > Float.MAX_VALUE || weaponTicks < 1 || weaponTicks > 72000 || criticalTier < 0 || criticalTier > 100)
            throw new IllegalArgumentException("Invalid status proc context");
        if ((!target.isAlive() && type != DamageType.GAS && type != DamageType.BLAST) || target.level().isClientSide || target.getType().is(STATUS_IMMUNE) || target.getType().is(IMMUNITIES.get(type)))
            return false;
        var value = state(target);
        if (type == DamageType.COLD && value.frozen > 0) return false;
        int duration = StatusRules.duration(type, weaponTicks);
        int maximum = StatusRules.cap(type, target instanceof Player);
        if (type == DamageType.COLD && target.getType().is(CONTROL_IMMUNE)) maximum = 4;
        if (type == DamageType.HEAT) {
            for (int i = 0; i < value.layers.size(); i++) {
                var layer = value.layers.get(i);
                if (layer.type() == type)
                    value.layers.set(i, new StatusState.Layer(type, layer.owner(), layer.base(), duration, layer.pulse(), layer.age(), layer.criticalTier(), layer.weapon(), layer.attackKind()));
            }
        }
        if (value.count(type) >= maximum) {
            // Capped statuses replace their oldest layer; uncapped damage statuses retain the strongest bounded set.
            int replace = -1;
            for (int i = 0; i < value.layers.size(); i++)
                if (value.layers.get(i).type() == type
                        && (replace < 0 || value.layers.get(i).remaining() < value.layers.get(replace).remaining()))
                    replace = i;
            if (maximum == 1024 && value.layers.get(replace).base() > base) return false;
            value.layers.remove(replace);
        }
        int pulse = 20;
        if (type == DamageType.HEAT) for (var layer : value.layers)
            if (layer.type() == type) {
                pulse = layer.pulse();
                break;
            }
        value.layers.add(new StatusState.Layer(type, owner, Math.min(Float.MAX_VALUE, base), duration, pulse, 0, criticalTier, weapon, attackKind));
        if (!target.getType().is(CONTROL_IMMUNE)) {
            if (type == DamageType.IMPACT) {
                value.impactStun = Math.max(value.impactStun, Math.min(20, Math.max(1, weaponTicks / 6)));
                StatusEffectService.emit(target, type, 6, true, List.of());
            }
            if (type == DamageType.ELECTRICITY)
                value.electricStun = Math.max(value.electricStun, Math.min(60, Math.max(1, weaponTicks / 2)));
            if (type == DamageType.HEAT && target instanceof Mob && value.count(type) == 1) value.heatPanic = 80;
            if (type == DamageType.COLD && !(target instanceof Player) && value.count(type) == 10) {
                value.frozen = Math.max(1, weaponTicks / 2);
            }
        }
        if (type == DamageType.BLAST && value.count(type) == 10) detonate(target, true);
        update(target);
        if (type == DamageType.SLASH) StatusEffectService.emit(target, type, (weaponTicks + 1) / 2, true, List.of());
        if (type == DamageType.PUNCTURE) StatusEffectService.emit(target, type, 9, true, List.of());
        if (immediate && (type == DamageType.ELECTRICITY || type == DamageType.GAS))
            pulses(target, List.of(new StatusState.Layer(type, owner, base, duration, pulse, 0, criticalTier, weapon, attackKind)));
        return true;
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide || !entity.hasData(RangedAttachments.STATUS)) return;
        if (entity instanceof AreaEffectCloud cloud) {
            tickCloud(cloud);
            return;
        }
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) return;
        var value = state(target);
        if (value.empty()) {
            if (value.dirty) update(target);
            return;
        }
        if (value.impactStun > 0) value.impactStun--;
        if (value.electricStun > 0) value.electricStun--;
        if (value.heatPanic > 0) value.heatPanic--;
        if (value.frozen > 0 && --value.frozen == 0) {
            var cold = value.layers.stream().filter(layer -> layer.type() == DamageType.COLD).limit(3).toList();
            value.layers.removeIf(layer -> layer.type() == DamageType.COLD);
            value.layers.addAll(cold);
        }
        int gasStacks = value.count(DamageType.GAS);
        var due = value.advance(value.frozen > 0);
        var heat = value.layers.stream().filter(layer -> layer.type() == DamageType.HEAT).findFirst();
        if (heat.isPresent()) value.heatStrip = Math.max(value.heatStrip, Math.min(.5, (heat.get().age() / 10) * .125));
        else value.heatStrip = Math.max(0, value.heatStrip - .5 / 30);
        update(target);
        pulses(target, due, gasStacks);
    }

    private record Origin(UUID owner, UUID weapon, AttackKind attackKind) {}

    private record Pulse(DamageType type, UUID owner, UUID weapon, AttackKind attackKind) {
    }

    private static void pulses(LivingEntity target, List<StatusState.Layer> due) {
        pulses(target, due, count(target, DamageType.GAS));
    }

    private static void pulses(LivingEntity target, List<StatusState.Layer> due, int gasStacks) {
        var groups = new LinkedHashMap<Pulse, List<Double>>();
        var tiers = new HashMap<Pulse, Integer>();
        for (var layer : due) {
            var key = new Pulse(layer.type(), layer.owner(), layer.weapon(), layer.attackKind());
            groups.computeIfAbsent(key, unused -> new ArrayList<>()).add(layer.base() * StatusRules.coefficient(layer.type()));
            tiers.merge(key, layer.criticalTier(), Math::max);
        }
        List<LivingEntity> electric = due.stream().anyMatch(layer -> layer.type() == DamageType.ELECTRICITY) ? area(target, 3) : List.of();
        List<LivingEntity> gas = due.stream().anyMatch(layer -> layer.type() == DamageType.GAS) ? area(target, StatusRules.gasRadius(Math.max(1, gasStacks))) : List.of();
        var struck = new LinkedHashSet<LivingEntity>();
        boolean heatHit = false, toxinHit = false;
        for (var entry : groups.entrySet()) {
            var key = entry.getKey();
            if (key.type == DamageType.ELECTRICITY) {
                for (var victim : electric)
                    if (allowed(target, victim, key.owner) && StatusDamage.tryHurtBatch(victim, key.type, key.owner, entry.getValue(), tiers.get(key), key.weapon, key.attackKind))
                        struck.add(victim);
            } else if (key.type == DamageType.GAS) {
                for (var victim : gas)
                    if (allowed(target, victim, key.owner))
                        StatusDamage.hurtBatch(victim, key.type, key.owner, entry.getValue(), tiers.get(key), key.weapon, key.attackKind);
            } else if (key.type == DamageType.HEAT || key.type == DamageType.TOXIN) {
                if (StatusDamage.tryHurtBatch(target, key.type, key.owner, entry.getValue(), tiers.get(key), key.weapon, key.attackKind)) {
                    if (key.type == DamageType.HEAT) heatHit = true;
                    else toxinHit = true;
                }
            } else StatusDamage.hurtBatch(target, key.type, key.owner, entry.getValue(), tiers.get(key), key.weapon, key.attackKind);
        }
        if (heatHit) StatusEffectService.emit(target, DamageType.HEAT, 8, false, List.of());
        if (toxinHit) StatusEffectService.emit(target, DamageType.TOXIN, 16, false, List.of());
        if (due.stream().anyMatch(layer -> layer.type() == DamageType.ELECTRICITY)) StatusEffectService.emit(target, DamageType.ELECTRICITY, 10, false, struck);
        due.stream().filter(layer -> layer.type() == DamageType.SLASH).mapToInt(layer -> (layer.remaining() + layer.age() + 1) / 2).max()
                .ifPresent(lifetime -> StatusEffectService.emit(target, DamageType.SLASH, lifetime, false, List.of()));
        if (due.stream().anyMatch(layer -> layer.type() == DamageType.BLAST)) StatusEffectService.emit(target, DamageType.BLAST, 12, false, List.of());
    }

    private static List<LivingEntity> area(Entity source, double radius) {
        return source.level().getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(radius), entity -> entity.isAlive() && entity.distanceToSqr(source) <= radius * radius);
    }

    private static boolean allowed(Entity host, LivingEntity victim, UUID owner) {
        if (victim.getUUID().equals(owner)) return false;
        if (victim == host) return true;
        Entity attacker = StatusDamage.owner((ServerLevel) host.level(), owner);
        if (attacker != null && (victim.isAlliedTo(attacker) || attacker.isAlliedTo(victim))) return false;
        if (attacker instanceof Player player) {
            if (victim instanceof TamableAnimal tame && tame.getOwnerUUID() != null && !(tame.getOwner() instanceof Player))
                return false;
            Player other = victim instanceof Player direct ? direct : victim instanceof TamableAnimal tame && tame.getOwner() instanceof Player ownerPlayer ? ownerPlayer : null;
            if (other != null && (!((ServerLevel) host.level()).getServer().isPvpAllowed() || !player.canHarmPlayer(other)))
                return false;
        } else if (attacker == null && (victim instanceof Player || victim instanceof TamableAnimal tame && tame.getOwnerUUID() != null))
            return false;
        return true;
    }

    private static void update(LivingEntity target) {
        var value = state(target);
        value.dirty = false;
        int[] counts = new int[DamageType.values().length];
        for (var layer : value.layers) {
            counts[layer.type().ordinal()]++;
        }
        StatusMarkerSync.changed(target, value, counts);
        double corrosion = StatusRules.corrosive(counts[DamageType.CORROSIVE.ordinal()]);
        modifier(target, Attributes.ARMOR, "corrosion", -corrosion);
        modifier(target, Attributes.ARMOR_TOUGHNESS, "corrosion", -corrosion);
        modifier(target, Attributes.ARMOR, "heat_strip", -value.heatStrip);
        double slow = StatusRules.coldSlow(counts[DamageType.COLD.ordinal()], target instanceof Player);
        modifier(target, Attributes.MOVEMENT_SPEED, "cold_slow", -slow);
        modifier(target, Attributes.FLYING_SPEED, "cold_slow", -slow);
        modifier(target, Attributes.ATTACK_SPEED, "cold_slow", -slow);
    }

    private static void modifier(LivingEntity target, net.minecraft.core.Holder<Attribute> attribute, String key, double amount) {
        var instance = target.getAttribute(attribute);
        if (instance == null) return;
        var keyId = id("status/" + key);
        var old = instance.getModifier(keyId);
        if (old != null && old.amount() == amount) return;
        instance.removeModifier(keyId);
        if (amount != 0)
            instance.addTransientModifier(new AttributeModifier(keyId, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @SubscribeEvent
    public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                && (event.getTarget() instanceof LivingEntity || event.getTarget() instanceof AreaEffectCloud)) {
            StatusMarkerSync.tracking(player, event.getTarget());
        }
    }

    @SubscribeEvent
    public static void stoppedTracking(PlayerEvent.StopTracking event) {
        var target = event.getTarget();
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player && target.hasData(RangedAttachments.STATUS))
            PacketDistributor.sendToPlayer(player, new yagen.waitmydawn.yagens_attributes.network.StatusMarkersPayload(target.getId(), target.getUUID(), Map.of()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void died(LivingDeathEvent event) {
        var target = event.getEntity();
        if (target.level().isClientSide || !target.hasData(RangedAttachments.STATUS)) return;
        detonate(target, true);
        var gas = state(target).layers.stream().filter(layer -> layer.type() == DamageType.GAS).toList();
        if (!gas.isEmpty()) {
            var cloud = new AreaEffectCloud(target.level(), target.getX(), target.getY(), target.getZ());
            cloud.setRadius(0);
            cloud.setWaitTime(0);
            cloud.setDuration(gas.stream().mapToInt(StatusState.Layer::remaining).max().orElse(1));
            state(cloud).layers.addAll(gas);
            ((ServerLevel) target.level()).addFreshEntity(cloud);
        }
        state(target).layers.clear();
        state(target).heatStrip = 0;
        state(target).frozen = 0;
        state(target).impactStun = 0;
        state(target).electricStun = 0;
        state(target).heatPanic = 0;
        update(target);
    }

    private static void tickCloud(AreaEffectCloud cloud) {
        var value = state(cloud);
        int stacks = value.count(DamageType.GAS);
        var due = value.advance(false);
        var groups = new LinkedHashMap<Origin, List<Double>>();
        var tiers = new HashMap<Origin, Integer>();
        for (var layer : due) {
            groups.computeIfAbsent(new Origin(layer.owner(), layer.weapon(), layer.attackKind()), key -> new ArrayList<>()).add(layer.base() * .5);
            tiers.merge(new Origin(layer.owner(), layer.weapon(), layer.attackKind()), layer.criticalTier(), Math::max);
        }
        if (!groups.isEmpty()) {
            var victims = area(cloud, StatusRules.gasRadius(Math.max(1, stacks)));
            for (var entry : groups.entrySet())
                for (var victim : victims)
                    if (allowed(cloud, victim, entry.getKey().owner()))
                        StatusDamage.hurtBatch(victim, DamageType.GAS, entry.getKey().owner(), entry.getValue(), tiers.get(entry.getKey()), entry.getKey().weapon(), entry.getKey().attackKind());
        }
        if (value.layers.isEmpty()) cloud.discard();
        else if (stacks != value.count(DamageType.GAS) || value.markerCounts[DamageType.GAS.ordinal()] == 0) StatusMarkerSync.changed(cloud);
    }

    private record Detonation(ServerLevel level, Vec3 position, LivingEntity host, List<StatusState.Layer> layers,
                              boolean area) {
    }

    private static void detonate(LivingEntity target, boolean area) {
        var value = state(target);
        var layers = value.layers.stream().filter(layer -> layer.type() == DamageType.BLAST).toList();
        if (layers.isEmpty()) return;
        value.layers.removeIf(layer -> layer.type() == DamageType.BLAST);
        BLASTS.addLast(new Detonation((ServerLevel) target.level(), target.position(), target, layers, area));
    }

    @SubscribeEvent
    public static void endTick(ServerTickEvent.Post event) {
        int budget = 128;
        while (budget-- > 0 && !BLASTS.isEmpty()) {
            var blast = BLASTS.removeFirst();
            if (blast.level.getServer() != event.getServer()) continue;
            var grouped = new LinkedHashMap<Origin, List<Double>>();
            var tiers = new HashMap<Origin, Integer>();
            for (var layer : blast.layers) {
                grouped.computeIfAbsent(new Origin(layer.owner(), layer.weapon(), layer.attackKind()), key -> new ArrayList<>()).add(layer.base());
                tiers.merge(new Origin(layer.owner(), layer.weapon(), layer.attackKind()), layer.criticalTier(), Math::max);
            }
            var victims = blast.level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(blast.position, blast.position).inflate(5), entity -> entity.isAlive() && entity.position().distanceToSqr(blast.position) <= 25);
            for (var entry : grouped.entrySet()) {
                StatusDamage.hurtBatch(blast.host, DamageType.BLAST, entry.getKey().owner(), entry.getValue().stream().map(value -> value * .3).toList(), tiers.get(entry.getKey()), entry.getKey().weapon(), entry.getKey().attackKind());
                double areaDamage = entry.getValue().stream().mapToDouble(Double::doubleValue).sum() * 3;
                for (var victim : victims)
                    if (victim != blast.host && allowed(blast.host, victim, entry.getKey().owner()))
                        StatusDamage.hurtBatch(victim, DamageType.BLAST, entry.getKey().owner(), List.of(areaDamage), tiers.get(entry.getKey()), entry.getKey().weapon(), entry.getKey().attackKind());
            }
            StatusEffectService.emit(blast.host, DamageType.BLAST, 12, true, List.of());
        }
    }

    @SubscribeEvent
    public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        BLASTS.clear();
    }

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof LivingEntity living && living.hasData(RangedAttachments.STATUS))
            update(living);
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.targetSelector.getAvailableGoals().stream().noneMatch(goal -> goal.getGoal() instanceof ConfusionGoal))
            mob.targetSelector.addGoal(-10, new ConfusionGoal(mob));
    }

    private static final class ConfusionGoal extends Goal {
        private final Mob mob;
        private LivingEntity chosen;

        ConfusionGoal(Mob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (count(mob, DamageType.RADIATION) == 0 || mob.getType().is(CONTROL_IMMUNE)) return false;
            UUID owner = latestOwner(mob, DamageType.RADIATION);
            var choices = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(10), other -> other != mob && !(other instanceof Player) && other.isAlive() && mob.hasLineOfSight(other) && allowed(mob, other, owner));
            if (choices.isEmpty()) return false;
            chosen = choices.get(mob.getRandom().nextInt(choices.size()));
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return count(mob, DamageType.RADIATION) > 0 && chosen != null && chosen.isAlive() && mob.distanceToSqr(chosen) <= 256;
        }

        @Override
        public void start() {
            state(mob).confusedTarget = chosen.getUUID();
            mob.setTarget(chosen);
        }

        @Override
        public void tick() {
            mob.setTarget(chosen);
            mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, chosen);
        }

        @Override
        public void stop() {
            if (mob.getTarget() == chosen) mob.setTarget(null);
            if (mob.getBrain().isMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, chosen))
                mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
            chosen = null;
            state(mob).confusedTarget = null;
        }
    }
}
