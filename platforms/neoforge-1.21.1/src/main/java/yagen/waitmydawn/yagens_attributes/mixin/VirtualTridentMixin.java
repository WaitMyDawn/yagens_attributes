package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

@Mixin(ThrownTrident.class)
public abstract class VirtualTridentMixin {
    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void yagens$pickup(Player player, CallbackInfoReturnable<Boolean> callback) {
        var trident = (ThrownTrident) (Object) this;
        var shot = RangedShooting.shot(trident);
        if (shot != null && shot.virtual()) {
            if (trident.isNoPhysics() && trident.getOwner() == player) trident.discard();
            callback.setReturnValue(false);
        }
    }
}
