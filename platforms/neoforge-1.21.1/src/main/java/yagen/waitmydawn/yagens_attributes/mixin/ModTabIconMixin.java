package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.client.ClientMods;
import yagen.waitmydawn.yagens_attributes.registries.ModCreativeTabs;

@Mixin(CreativeModeTab.class)
public abstract class ModTabIconMixin {
    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void yagens$cycle(CallbackInfoReturnable<ItemStack> callback) {
        if ((Object) this == ModCreativeTabs.MODS.get()) {
            var icon = ClientMods.icon(); if (!icon.isEmpty()) callback.setReturnValue(icon);
        }
    }
}
