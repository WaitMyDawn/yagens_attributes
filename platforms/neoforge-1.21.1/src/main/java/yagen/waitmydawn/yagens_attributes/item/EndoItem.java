package yagen.waitmydawn.yagens_attributes.item;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.yagens_attributes.data.WorldState;
import java.util.List;

public final class EndoItem extends Item {
    public EndoItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.yagens_attributes.endo.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int count = stack.getCount();
            try { WorldState.get(serverPlayer.server).creditEndo(player.getUUID(), count); }
            catch (IllegalArgumentException exception) {
                player.displayClientMessage(Component.translatable(exception.getMessage()), true);
                return InteractionResultHolder.fail(stack);
            }
            // Creative deposits consume the held items as well; accounting has no implicit item minting.
            stack.shrink(count); player.getInventory().setChanged();
            player.displayClientMessage(Component.translatable("gui.yagens_attributes.endo_deposited", count), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
