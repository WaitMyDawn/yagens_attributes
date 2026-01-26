package yagen.waitmydawn.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin {
    @Mutable
    @Shadow
    private DataComponentMap components;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void setMaxStackSize(Item.Properties pProperties, CallbackInfo pCallbackInfo) {
        Item item = (Item) (Object) this;
        int maxStack = item.getDefaultMaxStackSize();
        if (item instanceof BucketItem || item instanceof MilkBucketItem || item instanceof SolidBucketItem) {
            maxStack = 16;
        } else if (item instanceof PotionItem) {
            maxStack = 64;
        } else if (item instanceof EnchantedBookItem) {
            maxStack = 64;
        } else if (item instanceof BedItem) {
            maxStack = 64;
        }
        if (maxStack != item.getDefaultMaxStackSize())
            components = PatchedDataComponentMap.fromPatch(components, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, maxStack).build());

    }
}
