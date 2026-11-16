package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentExperienceScreenMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent yagens$showPoints(String key, Object[] arguments, Operation<MutableComponent> original) {
        if (key.equals("container.enchant.level.many")) {
            int tier = ((Number) arguments[0]).intValue();
            var menu = ((EnchantmentScreen) (Object) this).getMenu();
            return Component.translatable("gui.yagens_attributes.experience_cost", ExperiencePoints.enchanting(menu.costs[tier - 1], tier));
        }
        return original.call(key, arguments);
    }
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent yagens$showSingleTierPoints(String key, Operation<MutableComponent> original) {
        if (key.equals("container.enchant.level.one")) {
            var menu = ((EnchantmentScreen) (Object) this).getMenu();
            return Component.translatable("gui.yagens_attributes.experience_cost", ExperiencePoints.enchanting(menu.costs[0], 1));
        }
        return original.call(key);
    }
}
