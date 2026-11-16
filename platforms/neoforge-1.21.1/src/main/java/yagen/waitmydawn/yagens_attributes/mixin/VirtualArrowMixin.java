package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

@Mixin(AbstractArrow.class)
public abstract class VirtualArrowMixin {
    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void yagens$pickup(Player player, CallbackInfoReturnable<Boolean> callback) {
        var shot = RangedShooting.shot((AbstractArrow) (Object) this);
        if (shot != null && shot.virtual()) callback.setReturnValue(false);
    }
    @Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
    private void yagens$returnItem(CallbackInfoReturnable<ItemStack> callback) {
        var arrow = (AbstractArrow) (Object) this;
        var shot = RangedShooting.shot(arrow);
        // Loyalty may drop a trident when its owner dies; virtual copies must not produce that item either.
        if (arrow instanceof ThrownTrident && shot != null && shot.virtual()) callback.setReturnValue(ItemStack.EMPTY);
    }
}
