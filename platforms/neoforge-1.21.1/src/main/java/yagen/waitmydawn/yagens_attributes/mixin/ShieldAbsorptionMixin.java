package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.ShieldService;
import yagen.waitmydawn.yagens_attributes.setup.DefenseEvents;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

@Mixin(LivingEntity.class)
public abstract class ShieldAbsorptionMixin {
    @Shadow protected java.util.Stack<DamageContainer> damageContainers;
    @Inject(method = "getMaxAbsorption", at = @At("RETURN"), cancellable = true)
    private void yagens$capacity(CallbackInfoReturnable<Float> callback) {
        var entity = (LivingEntity) (Object) this;
        if (ShieldService.enabled(entity)) callback.setReturnValue(Math.max(callback.getReturnValue(), (float) ShieldService.capacity(entity)));
    }
    @ModifyVariable(method = "setAbsorptionAmount", at = @At("HEAD"), argsOnly = true)
    private float yagens$limit(float amount) {
        var entity = (LivingEntity) (Object) this;
        return ShieldService.enabled(entity) ? (float) Math.min(amount, ShieldService.capacity(entity)) : amount;
    }
    @Inject(method = "setAbsorptionAmount", at = @At("HEAD"))
    private void yagens$commitGate(float amount, CallbackInfo callback) {
        if (damageContainers != null && !damageContainers.empty())
            DefenseEvents.absorptionChanging((LivingEntity) (Object) this, damageContainers.peek().getSource(), amount);
    }
}
