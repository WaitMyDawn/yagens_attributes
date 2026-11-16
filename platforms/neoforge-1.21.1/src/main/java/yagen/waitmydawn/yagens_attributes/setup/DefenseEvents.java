package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

/** Resolves shield-only and health-only multipliers without amplifying shield overflow twice. */
@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class DefenseEvents {
    private record Pending(double before, boolean gated, int magnetic, double maximum) {}
    private static final Map<DamageSource, Map<LivingEntity, Deque<Pending>>> PENDING = new IdentityHashMap<>();
    private DefenseEvents() {}
    public static void absorptionChanging(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide || amount > 0 || target.getAbsorptionAmount() <= 0) return;
        var targets = PENDING.get(source); var stack = targets == null ? null : targets.get(target);
        if (stack != null && !stack.isEmpty() && stack.peek().gated) ShieldService.broken(target, stack.peek().before);
    }
    @SubscribeEvent public static void endTick(ServerTickEvent.Post event) { PENDING.clear(); }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void outgoing(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide || event.getAmount() <= 0 || StatusDamage.isSecondary(event.getSource())) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (StatusEffects.controlled(attacker) && event.getSource().getDirectEntity() == attacker) { event.setCanceled(true); return; }
            double factor = StatusRules.puncture(StatusEffects.count(attacker, DamageType.PUNCTURE));
            if (attacker.isAlliedTo(event.getEntity()) || StatusEffects.confusedAttack(attacker, event.getEntity())) factor *= StatusRules.radiation(StatusEffects.count(attacker, DamageType.RADIATION));
            event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() * factor));
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void before(LivingDamageEvent.Pre event) {
        var target = event.getEntity(); if (target.level().isClientSide || event.getNewDamage() <= 0) return;
        boolean bypass = event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        double fraction = StatusDamage.bypassesShields(event.getSource()) ? 1 : CombatEvents.toxinFraction(event.getSource(), target);
        double input = event.getNewDamage(), shield = target.getAbsorptionAmount();
        double normal = input * (1 - fraction), healthOnly = input * fraction;
        double nativeAfterArmor = input + event.getContainer().getReduction(Reduction.MOB_EFFECTS) + event.getContainer().getReduction(Reduction.ENCHANTMENTS);
        double unarmored = nativeAfterArmor <= 0 ? input : input + event.getContainer().getReduction(Reduction.ARMOR) * input / nativeAfterArmor;
        double shieldInput = unarmored * (1 - fraction);
        boolean protectedHealth = !bypass && ShieldService.protectedNow(target);
        if (protectedHealth) { normal = 0; shieldInput = 0; }
        int magnetic = StatusEffects.count(target, DamageType.MAGNETIC);
        boolean armed = !bypass && ShieldService.enabled(target) && ShieldService.state(target).armed;
        if (armed) ShieldService.state(target).observe(shield);
        var hit = ShieldRules.resolve(normal, shieldInput, healthOnly, shield, StatusRules.viral(magnetic),
                bypass ? 1 : StatusRules.viral(StatusEffects.count(target, DamageType.VIRAL)), armed, protectedHealth);
        var components = CombatEvents.components(event.getSource(), target);
        if (!components.isEmpty() && components.values().stream().mapToDouble(Double::doubleValue).sum() > 0)
            hit = LayeredDamage.resolve(input, unarmored, components,
                    yagen.waitmydawn.yagens_attributes.data.CombatData.MATERIALS.material(target),
                    yagen.waitmydawn.yagens_attributes.data.ShieldMaterials.INSTANCE.multipliers(), shield,
                    StatusRules.viral(magnetic), bypass ? 1 : StatusRules.viral(StatusEffects.count(target, DamageType.VIRAL)), armed, protectedHealth);
        var resolvedHit = hit;
        event.setNewDamage((float) Math.min(Float.MAX_VALUE, hit.shieldSpent() + hit.healthDamage()));
        event.getContainer().addModifier(Reduction.ABSORPTION, (container, reduction) -> Math.min(reduction, (float) resolvedHit.shieldSpent()));
        PENDING.computeIfAbsent(event.getSource(), key -> new IdentityHashMap<>()).computeIfAbsent(target, key -> new ArrayDeque<>())
                .push(new Pending(shield, hit.gated(), magnetic, ShieldService.enabled(target) ? ShieldService.natural(target) : target.getMaxAbsorption()));
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void after(LivingDamageEvent.Post event) {
        var targets = PENDING.get(event.getSource()); var stack = targets == null ? null : targets.get(event.getEntity());
        var pending = stack == null || stack.isEmpty() ? null : stack.pop();
        if (stack != null && stack.isEmpty()) targets.remove(event.getEntity());
        if (targets != null && targets.isEmpty()) PENDING.remove(event.getSource());
        double spent = event.getReduction(Reduction.ABSORPTION);
        if (event.getNewDamage() > 0 || spent > 0) ShieldService.damaged(event.getEntity());
        if (pending != null && pending.before > 0 && spent > 0 && event.getEntity().getAbsorptionAmount() <= 0) {
            if (pending.gated) ShieldService.broken(event.getEntity(), pending.before);
            if (pending.magnetic > 0) {
                UUID owner = StatusEffects.latestOwner(event.getEntity(), DamageType.MAGNETIC);
                double damage = .03 * pending.maximum * pending.magnetic;
                if (event.getEntity().isAlive()) StatusEffects.apply(event.getEntity(), DamageType.ELECTRICITY, owner, damage / .5, 120);
                StatusEffects.visual(event.getEntity(), DamageType.MAGNETIC, 1.2, true);
            }
        }
    }
}
