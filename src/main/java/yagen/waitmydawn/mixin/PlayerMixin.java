package yagen.waitmydawn.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void giveInfinityArrow(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack curr = cir.getReturnValue();
        if (!curr.isEmpty()) return;
        if (!(weapon.getItem() instanceof BowItem)) return;
        Level level = ((Player) (Object) this).level();
        if (weapon.getEnchantmentLevel(
                level.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.INFINITY)) > 0) {
            cir.setReturnValue(new ItemStack(Items.ARROW));
        }
    }
}