package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.api.WeakPointApi;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.data.WeakPointGeometry;

@Mixin(AbstractArrow.class)
public abstract class ArrowWeakPointMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;findHitEntity(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/EntityHitResult;"))
    private EntityHitResult yagens$captureTrace(AbstractArrow arrow, Vec3 start, Vec3 end, Operation<EntityHitResult> original,
                                                @Share("weakPointHit") LocalRef<EntityHitResult> traceHit,
                                                @Share("weakPointStart") LocalRef<Vec3> traceStart,
                                                @Share("weakPointEnd") LocalRef<Vec3> traceEnd) {
        var hit = original.call(arrow, start, end);
        // The endpoints already respect the native block collision. EntityHitResult alone can contain the target origin.
        if (hit != null && arrow.getOwner() instanceof ServerPlayer && WeakPointGeometry.receiver(hit.getEntity()) != null) {
            traceStart.set(start);
            traceEnd.set(end);
            traceHit.set(hit);
        } else {
            traceHit.set(null);
        }
        return hit;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    private ProjectileDeflection yagens$scopeImpact(AbstractArrow arrow, HitResult hit, Operation<ProjectileDeflection> original,
                                                     @Share("weakPointHit") LocalRef<EntityHitResult> traceHit,
                                                     @Share("weakPointStart") LocalRef<Vec3> traceStart,
                                                     @Share("weakPointEnd") LocalRef<Vec3> traceEnd) {
        var candidate = traceHit.get();
        if (candidate == null || candidate != hit || !ServerConfigs.ENABLE_COMBAT.get()) return original.call(arrow, hit);
        return WeakPointApi.withTrace(arrow, candidate.getEntity(), traceStart.get(), traceEnd.get(),
                () -> original.call(arrow, hit));
    }
}
