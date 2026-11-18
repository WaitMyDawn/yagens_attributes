package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.yagens_attributes.data.WeakPointDefinitions;
import yagen.waitmydawn.yagens_attributes.data.WeakPointGeometry;

import java.util.function.Supplier;

public final class WeakPointApi {
    private record Impact(Entity direct, LivingEntity target, WeakPointGeometry.Match weakPoint) {}

    private static final ThreadLocal<Impact> IMPACT = new ThreadLocal<>();

    private WeakPointApi() {}

    /**
     * Associates an authoritative, obstruction-clipped trace with the damage delivered inside the callback.
     * The direct entity must match DamageSource#getDirectEntity and the target must be its actual victim.
     */
    public static <T> T withTrace(Entity direct, LivingEntity target, Vec3 start, Vec3 end, Supplier<T> damage) {
        return withTrace(direct, (Entity) target, start, end, damage);
    }

    /** Accepts the actual collision entity when a native part forwards damage to its living parent. */
    public static <T> T withTrace(Entity direct, Entity hit, Vec3 start, Vec3 end, Supplier<T> damage) {
        var target = WeakPointGeometry.receiver(hit);
        if (target == null || !(target.level() instanceof ServerLevel level) || direct.level() != level
                || hit.level() != level || !level.getServer().isSameThread())
            throw new IllegalArgumentException("Weak point traces require entities in the same logical server level");
        if (!finite(start) || !finite(end)) throw new IllegalArgumentException("Weak point trace coordinates must be finite");
        var weakPoint = WeakPointGeometry.match(hit, target, start, end, WeakPointDefinitions.INSTANCE.regions(target.getType()));
        var previous = IMPACT.get();
        IMPACT.set(new Impact(direct, target, weakPoint));
        try {
            return damage.get();
        } finally {
            if (previous == null) IMPACT.remove();
            else IMPACT.set(previous);
        }
    }

    public static WeakPointGeometry.Match current(DamageSource source, LivingEntity target) {
        var impact = IMPACT.get();
        return impact != null && impact.direct() == source.getDirectEntity() && impact.target() == target
                ? impact.weakPoint() : null;
    }

    private static boolean finite(Vec3 point) {
        return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }
}
