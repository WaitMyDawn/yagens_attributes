package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.registries.Components;

import java.util.List;

@Mixin(Hotbar.class)
public abstract class CreativeHotbarWeaponMixin {
    @ModifyReturnValue(method = "load", at = @At("RETURN"))
    private List<ItemStack> yagens$independentTemplates(List<ItemStack> templates) {
        templates.forEach(Components::clearWeaponState);
        return templates;
    }
}
