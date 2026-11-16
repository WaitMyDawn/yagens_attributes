package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;

@Mixin(AnvilScreen.class)
public abstract class AnvilExperienceScreenMixin {
    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z"))
    private boolean yagens$removePriceLimit(boolean creative) { return true; }
    @WrapOperation(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent yagens$showPoints(String key, Object[] arguments, Operation<MutableComponent> original) {
        return Component.translatable("gui.yagens_attributes.experience_cost", ExperiencePoints.total(((Number) arguments[0]).intValue()));
    }
}
