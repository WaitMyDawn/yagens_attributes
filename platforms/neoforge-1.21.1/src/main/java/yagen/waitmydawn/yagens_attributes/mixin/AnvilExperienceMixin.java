package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;
import yagen.waitmydawn.yagens_attributes.setup.ExperiencePayments;

@Mixin(AnvilMenu.class)
public abstract class AnvilExperienceMixin {
    @Shadow public abstract int getCost();
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void yagens$affordPoints(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(getCost() > 0 && ExperiencePayments.affordable(player, ExperiencePoints.total(getCost())));
    }
    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    private void yagens$payPoints(Player player, int delta, Operation<Void> original) {
        ExperiencePayments.pay(player, ExperiencePoints.total(-delta));
    }
    // The second instabuild check is only the 40-level rejection, not enchantment applicability.
    @ModifyExpressionValue(method = "createResult", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z", ordinal = 1))
    private boolean yagens$removePriceLimit(boolean creative) { return true; }
}
