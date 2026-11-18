package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.api.WeaponLifecycleApi;
import yagen.waitmydawn.yagens_attributes.registries.Components;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class WeaponDurabilityMixin {
    @WrapOperation(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void yagens$retireBrokenWeapon(ItemStack stack, int amount, Operation<Void> original,
                                          int damage, ServerLevel level, LivingEntity user, Consumer<Item> onBreak) {
        var weapon = stack.get(Components.WEAPON_ID.get());
        original.call(stack, amount);
        if (stack.isEmpty()) WeaponLifecycleApi.retireDestroyedWeapon(level, weapon);
    }
}
