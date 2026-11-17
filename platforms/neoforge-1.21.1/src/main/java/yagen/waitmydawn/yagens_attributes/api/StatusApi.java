package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.StatusEffects;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Server-thread access to independent procs; vanilla effects and their immunity hooks are not consulted. */
public final class StatusApi {
    private StatusApi() {}

    /** Immutable layer data; damageBase already includes the originating hit's offensive scaling. */
    public record Layer(DamageType type, UUID owner, double damageBase, int remainingTicks,
                        int nextPulseTicks, int ageTicks, int criticalTier, UUID weapon, AttackKind attackKind) {
        public Layer(DamageType type, UUID owner, double damageBase, int remainingTicks, int nextPulseTicks, int ageTicks, int criticalTier) {
            this(type, owner, damageBase, remainingTicks, nextPulseTicks, ageTicks, criticalTier, null, AttackKind.UNKNOWN);
        }
        public Layer { Objects.requireNonNull(attackKind).validateWeapon(weapon); }
    }

    public static int count(LivingEntity target, DamageType type) {
        requireServerThread(target); Objects.requireNonNull(type);
        return StatusEffects.count(target, type);
    }
    /** Distinct independent status types, including full Cold freeze; potion effects are outside this API. */
    public static int distinctTypes(LivingEntity target) {
        requireServerThread(target);
        if (!target.hasData(RangedAttachments.STATUS)) return 0;
        var state = StatusEffects.state(target);
        int mask = state.frozen > 0 ? 1 << DamageType.COLD.ordinal() : 0;
        for (var layer : state.layers) mask |= 1 << layer.type().ordinal();
        return Integer.bitCount(mask);
    }

    public static boolean isFrozen(LivingEntity target) {
        requireServerThread(target);
        return StatusEffects.frozen(target);
    }

    public static boolean isControlled(LivingEntity target) {
        requireServerThread(target);
        return StatusEffects.controlled(target);
    }

    /** Maximum remaining layer lifetime; Cold layer clocks pause during a full freeze. */
    public static int durationTicks(LivingEntity target, DamageType type) {
        requireServerThread(target); Objects.requireNonNull(type);
        return target.hasData(RangedAttachments.STATUS) ? StatusEffects.state(target).duration(type) : 0;
    }

    /** Returns a detached snapshot; callers cannot mutate the entity attachment through this list. */
    public static List<Layer> layers(LivingEntity target) {
        requireServerThread(target);
        if (!target.hasData(RangedAttachments.STATUS)) return List.of();
        return StatusEffects.state(target).layers.stream().map(layer -> new Layer(layer.type(), layer.owner(),
                layer.base(), layer.remaining(), layer.pulse(), layer.age(), layer.criticalTier(), layer.weapon(), layer.attackKind())).toList();
    }

    public static boolean apply(LivingEntity target, DamageType type, UUID owner, double damageBase, int weaponDurationTicks) {
        return apply(target, type, owner, damageBase, weaponDurationTicks, 0);
    }

    /**
     * Applies one proc using the type's duration and stack rules. Immunity tags and stack limits still apply.
     * The duration input is the weapon baseline, 1..72000 ticks; damageBase is finite and nonnegative,
     * at most Float.MAX_VALUE. criticalTier is display attribution, 0..100, not another damage multiplier.
     * Electricity and Gas include their immediate pulse. False means no layer was accepted.
     */
    public static boolean apply(LivingEntity target, DamageType type, UUID owner, double damageBase, int weaponDurationTicks, int criticalTier) {
        requireServerThread(target);
        return StatusEffects.tryApply(target, type, owner, damageBase, weaponDurationTicks, criticalTier);
    }

    /** Applies a proc with explicit weapon attribution for delayed kills and secondary area damage. */
    public static boolean apply(LivingEntity target, DamageType type, UUID owner, StatusContext context, int weaponDurationTicks) {
        requireServerThread(target);
        return StatusEffects.tryApply(target, type, owner, weaponDurationTicks, Objects.requireNonNull(context));
    }

    /** Clears a type and its residual modifiers/control state, scheduling a head-marker update. */
    public static boolean clear(LivingEntity target, DamageType type) {
        requireServerThread(target); Objects.requireNonNull(type);
        return StatusEffects.clear(target, type);
    }

    public static boolean clearAll(LivingEntity target) {
        requireServerThread(target);
        return StatusEffects.clearAll(target);
    }

    private static void requireServerThread(LivingEntity target) {
        Objects.requireNonNull(target);
        if (!(target.level() instanceof ServerLevel level) || !level.getServer().isSameThread())
            throw new IllegalStateException("Status API requires the logical server thread");
    }
}
