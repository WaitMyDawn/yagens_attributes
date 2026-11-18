package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import yagen.waitmydawn.yagens_attributes.registries.Components;

@Mixin(AbstractContainerMenu.class)
public abstract class CreativeWeaponCloneMixin {
    @ModifyExpressionValue(method = "doClick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/ClickType;CLONE:Lnet/minecraft/world/inventory/ClickType;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/ClickType;THROW:Lnet/minecraft/world/inventory/ClickType;")))
    private ItemStack yagens$independentCopy(ItemStack copy) {
        return Components.clearWeaponState(copy);
    }
}
