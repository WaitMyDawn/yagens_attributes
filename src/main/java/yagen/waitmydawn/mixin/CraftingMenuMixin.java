package yagen.waitmydawn.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.api.events.PlayerInteractionEvent;

import javax.annotation.Nullable;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @Inject(
            method = "slotChangedCraftingGrid",
            at = @At("RETURN")
    )
    private static void createNoMarkResult(
            AbstractContainerMenu menu,
            Level level,
            Player player,
            CraftingContainer craftSlots,
            ResultContainer resultSlots,
            @Nullable RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo ci
    ) {
        if (!level.isClientSide) {
            ItemStack resultStack = resultSlots.getItem(0);
            if (!resultStack.isEmpty()) {
                PlayerInteractionEvent.extendNewItemStackAttributes(resultStack);
            }
        }
    }
}