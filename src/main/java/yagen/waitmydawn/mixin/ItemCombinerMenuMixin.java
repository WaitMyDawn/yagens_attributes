package yagen.waitmydawn.mixin;

import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.api.events.PlayerInteractionEvent;

@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {
    @Shadow
    protected ResultContainer resultSlots;

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void createNoMarkResult(CallbackInfo ci) {
        ItemStack stack = this.resultSlots.getItem(0);
        if (!stack.isEmpty())
            PlayerInteractionEvent.extendNewItemStackAttributes(stack);
    }
}
