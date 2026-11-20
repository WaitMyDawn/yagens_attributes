package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.data.ProjectileWeakPointTrace;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilWeakPointMixin {
    @WrapOperation(method = "getHitResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"))
    private static EntityHitResult yagens$captureTrace(Level level, Entity source, Vec3 start, Vec3 end, AABB candidates,
                                                       Predicate<Entity> filter, float margin, Operation<EntityHitResult> original) {
        var hit = original.call(level, source, start, end, candidates, filter, margin);
        if (!level.isClientSide()) ProjectileWeakPointTrace.capture(source, hit, start, end, margin);
        return hit;
    }
}
