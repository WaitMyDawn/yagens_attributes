package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;
import yagen.waitmydawn.yagens_attributes.setup.WeaponLifecycle;

@Mixin(AbstractArrow.class)
public abstract class VirtualArrowMixin {
    @Inject(method = "tickDespawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V", shift = At.Shift.AFTER))
    private void yagens$retireExpiredTrident(CallbackInfo callback) {
        WeaponLifecycle.retireCarrier((AbstractArrow) (Object) this);
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void yagens$touch(Player player, CallbackInfo callback) {
        var arrow = (AbstractArrow) (Object) this;
        var shot = RangedShooting.shot(arrow);
        if (shot != null && shot.virtual()) {
            if (arrow.isNoPhysics() && arrow.getOwner() == player) arrow.discard();
            callback.cancel();
        }
    }
    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void yagens$pickup(Player player, CallbackInfoReturnable<Boolean> callback) {
        var shot = RangedShooting.shot((AbstractArrow) (Object) this);
        if (shot != null && shot.virtual()) callback.setReturnValue(false);
    }
    @Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
    private void yagens$returnItem(CallbackInfoReturnable<ItemStack> callback) {
        var arrow = (AbstractArrow) (Object) this;
        var shot = RangedShooting.shot(arrow);
        // Returning native arrows may bypass tryPickup or drop their carried weapon when the owner disappears.
        if (shot != null && shot.virtual()) callback.setReturnValue(ItemStack.EMPTY);
    }
}
