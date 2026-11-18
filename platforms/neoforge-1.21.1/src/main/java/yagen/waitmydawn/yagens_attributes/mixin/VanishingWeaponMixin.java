package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.setup.WeaponLifecycle;

@Mixin(Player.class)
public abstract class VanishingWeaponMixin {
    @WrapOperation(method = "destroyVanishingCursedItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;removeItemNoUpdate(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack yagens$retireVanishingWeapon(Inventory inventory, int slot, Operation<ItemStack> original) {
        ItemStack destroyed = original.call(inventory, slot);
        if (inventory.player.level() instanceof ServerLevel level) WeaponLifecycle.retire(level, destroyed);
        return destroyed;
    }
}
