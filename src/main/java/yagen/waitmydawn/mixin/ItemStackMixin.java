package yagen.waitmydawn.mixin;

import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Overwrite
    public int getMaxStackSize() {
        Item item = ((ItemStack) (Object) this).getItem();
        if (item == Items.TOTEM_OF_UNDYING) {
            return 16;
        } else if (item == Items.MILK_BUCKET) {
            return 16;
        } else if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            return 64;
        } else if (item == Items.ENCHANTED_BOOK) {
            return 64;
        }
        return item.getDefaultMaxStackSize();
    }
//    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
//    private void onGetMaxStackSize(CallbackInfoReturnable<Integer> cir) {
//        if (((ItemStack)(Object)this).getItem() == Items.TOTEM_OF_UNDYING) {
//            LOGGER.info("Mixin applied to Totem of Undying: setting max stack size to 16");
//            cir.setReturnValue(16);
//        }
//    }
}