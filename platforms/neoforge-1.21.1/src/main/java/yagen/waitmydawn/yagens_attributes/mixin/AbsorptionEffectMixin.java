package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.ShieldService;

@Mixin(targets = "net.minecraft.world.effect.AbsorptionMobEffect")
public abstract class AbsorptionEffectMixin {
    @Inject(method = "onEffectStarted", at = @At("HEAD"), cancellable = true)
    private void yagens$overload(LivingEntity entity, int amplifier, CallbackInfo callback) {
        if (ShieldService.enabled(entity)) {
            if (!entity.level().isClientSide) ShieldService.add(entity, 4D * (1D + Math.max(0, amplifier)));
            callback.cancel();
        }
    }
}
