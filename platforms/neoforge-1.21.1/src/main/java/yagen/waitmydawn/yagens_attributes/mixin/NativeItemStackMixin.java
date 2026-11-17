package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import yagen.waitmydawn.yagens_attributes.compat.NativeAttacks;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;

@Mixin(ItemStack.class)
public abstract class NativeItemStackMixin {
    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> yagens$use(Level level, Player player, InteractionHand hand, Operation<InteractionResultHolder<ItemStack>> original) {
        return NativeAttacks.run((ItemStack) (Object) this, player, () -> original.call(level, player, hand));
    }
    @WrapMethod(method = "useOn")
    private InteractionResult yagens$useOn(UseOnContext context, Operation<InteractionResult> original) {
        return NativeAttacks.run((ItemStack) (Object) this, context.getPlayer(), () -> original.call(context));
    }
    @WrapMethod(method = "releaseUsing")
    private void yagens$release(Level level, LivingEntity actor, int count, Operation<Void> original) {
        NativeAttacks.run((ItemStack) (Object) this, actor, () -> original.call(level, actor, count));
    }
    @WrapMethod(method = "onUseTick")
    private void yagens$tick(Level level, LivingEntity actor, int count, Operation<Void> original) {
        NativeAttacks.useTick((ItemStack) (Object) this, actor, count, duration -> original.call(level, actor, duration));
    }
    @WrapMethod(method = "hurtEnemy")
    private boolean yagens$afterHit(LivingEntity target, Player player, Operation<Boolean> original) {
        return player instanceof ServerPlayer serverPlayer
                ? NativeAttacks.run((ItemStack) (Object) this, serverPlayer, AttackKind.MELEE, () -> original.call(target, player))
                : original.call(target, player);
    }
}
