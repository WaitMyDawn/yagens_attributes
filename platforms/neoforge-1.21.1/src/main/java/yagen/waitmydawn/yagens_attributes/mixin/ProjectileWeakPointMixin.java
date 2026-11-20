package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yagen.waitmydawn.yagens_attributes.api.WeakPointApi;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.data.ProjectileWeakPointTrace;

@Mixin(Projectile.class)
public abstract class ProjectileWeakPointMixin implements ProjectileWeakPointTrace.Carrier {
    @Unique private ProjectileWeakPointTrace yagens$weakPointTrace;

    @Override
    public void yagens$setWeakPointTrace(ProjectileWeakPointTrace trace) {
        yagens$weakPointTrace = trace;
    }

    @WrapMethod(method = "hitTargetOrDeflectSelf")
    private ProjectileDeflection yagens$applyTrace(HitResult hit, Operation<ProjectileDeflection> original) {
        var trace = yagens$weakPointTrace;
        yagens$weakPointTrace = null;
        // Impact handlers may cancel or replace the collision before native damage runs.
        if (trace == null || trace.hit() != hit || !ServerConfigs.ENABLE_COMBAT.get()) return original.call(hit);
        var projectile = (Projectile) (Object) this;
        return WeakPointApi.withTrace(projectile, trace.hit().getEntity(), trace.start(), trace.contactEnd(projectile),
                () -> original.call(hit));
    }
}
