package yagen.waitmydawn.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    private static int consumeCount(ItemStack left, ItemStack right) {
        if (!left.is(Items.ENCHANTED_BOOK) || !right.is(Items.ENCHANTED_BOOK)) return 1;
        return Math.min(left.getCount(), right.getCount());
    }

    @ModifyArg(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 0), index = 1)
    private ItemStack shrinkLeft(ItemStack original) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int consume = consumeCount(left, right);
        if (left.is(Items.ENCHANTED_BOOK) && left.getCount() >= consume) {
            return left;
        }
        return original;
    }

    @ModifyArg(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3), index = 1)
    private ItemStack shrinkRight(ItemStack original) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int consume = consumeCount(left, right);
        if (right.is(Items.ENCHANTED_BOOK)) {
            right.shrink(consume);
            if (left.is(Items.ENCHANTED_BOOK)) {
                left.shrink(consume);
            }
            return right;
        }
        return original;
    }

    @ModifyArg(method = "createResult",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3), index = 1)
    private ItemStack setOutputCount(ItemStack stack) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int count = consumeCount(left, right);
        if (stack.is(Items.ENCHANTED_BOOK)) {
            stack.setCount(count);
        }
        return stack;
    }
}

