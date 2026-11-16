package yagen.waitmydawn.yagens_attributes.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.yagens_attributes.data.WorldState;

public final class KuvaItem extends MaterialItem {
    public KuvaItem(Properties properties) { super(properties, "item.yagens_attributes.kuva.description", "item.yagens_attributes.kuva.use", "item.yagens_attributes.kuva.deposit"); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int count = stack.getCount();
            try { WorldState.get(serverPlayer.server).creditKuva(player.getUUID(), count); }
            catch (IllegalArgumentException exception) { return InteractionResultHolder.fail(stack); }
            stack.shrink(count); player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.translatable("gui.yagens_attributes.kuva_deposited", count), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
