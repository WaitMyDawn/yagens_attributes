package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.WeaponLifecycle;

@Mixin(Entity.class)
public abstract class WeaponCarrierMixin {
    @Inject(method = {"kill", "onBelowWorld"}, at = @At("RETURN"))
    private void yagens$retireInvalidCarrier(CallbackInfo callback) {
        WeaponLifecycle.retireCarrier((Entity) (Object) this);
    }
}
