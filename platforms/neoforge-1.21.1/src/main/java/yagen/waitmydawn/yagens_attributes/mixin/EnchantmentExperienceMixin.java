package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;
import yagen.waitmydawn.yagens_attributes.setup.ExperiencePayments;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentExperienceMixin {
    @Shadow @Final public int[] costs;
    @WrapOperation(method = "lambda$clickMenuButton$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onEnchantmentPerformed(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void yagens$payPoints(Player player, ItemStack stack, int tier, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int option) {
        ExperiencePayments.pay(player, ExperiencePoints.enchanting(costs[option], tier));
        // Preserve seed renewal and player callbacks without a second, level-based debit.
        original.call(player, stack, 0);
    }
}
