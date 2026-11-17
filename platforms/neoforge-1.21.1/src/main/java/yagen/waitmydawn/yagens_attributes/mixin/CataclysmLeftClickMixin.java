package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import yagen.waitmydawn.yagens_attributes.compat.NativeAttacks;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;

@Pseudo
@Mixin(targets = {"com.github.L_Ender.cataclysm.items.Ancient_Spear", "com.github.L_Ender.cataclysm.items.Tidal_Claws"}, remap = false)
public abstract class CataclysmLeftClickMixin {
    @WrapMethod(method = "onLeftClick")
    private boolean yagens$attack(ItemStack stack, LivingEntity actor, Operation<Boolean> original) {
        return actor instanceof ServerPlayer player ? NativeAttacks.run(stack, player, AttackKind.MELEE, () -> original.call(stack, actor))
                : original.call(stack, actor);
    }
}
