package yagen.waitmydawn.mixin;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40), require = 0)
    private int removeDisplayCap(int oldLimit) {
        return Integer.MAX_VALUE;
    }
}