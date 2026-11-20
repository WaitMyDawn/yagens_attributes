package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Opt-in trace capture for projectiles that use ProjectileUtil's block-clipped collision query. */
public record ProjectileWeakPointTrace(EntityHitResult hit, Vec3 start, Vec3 end, ClipContext.Block clip, float margin) {
    private static final Map<EntityType<?>, ClipContext.Block> TYPES = new HashMap<>();

    public interface Carrier {
        void yagens$setWeakPointTrace(ProjectileWeakPointTrace trace);
    }

    public static void register(EntityType<? extends Projectile> type, ClipContext.Block clip) {
        TYPES.put(type, clip);
    }

    public static void capture(Entity source, EntityHitResult hit, Vec3 start, Vec3 end, float margin) {
        var clip = TYPES.get(source.getType());
        if (clip == null || !(source instanceof Projectile projectile)) return;
        var carrier = (Carrier) projectile;
        carrier.yagens$setWeakPointTrace(hit != null && projectile.getOwner() instanceof ServerPlayer
                && WeakPointGeometry.receiver(hit.getEntity()) != null ? new ProjectileWeakPointTrace(hit, start, end, clip, margin) : null);
    }

    public Vec3 contactEnd(Projectile projectile) {
        if (margin <= 0) return end;
        var bounds = hit.getEntity().getBoundingBox();
        if (bounds.contains(start) || bounds.contains(end) || bounds.clip(start, end).isPresent()) return end;

        // The native margin can deliver damage before the current step reaches the actual surface.
        var projected = end.add(end.subtract(start).normalize().scale(margin));
        var contact = bounds.clip(end, projected).orElse(null);
        if (contact == null) return end;
        var obstruction = projectile.level().clip(new ClipContext(start, contact, clip, ClipContext.Fluid.NONE, projectile));
        return obstruction.getType() == HitResult.Type.MISS ? contact : end;
    }
}
