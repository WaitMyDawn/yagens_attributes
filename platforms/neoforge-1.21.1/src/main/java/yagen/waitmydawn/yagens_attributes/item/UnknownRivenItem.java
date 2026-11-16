package yagen.waitmydawn.yagens_attributes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.yagens_attributes.core.RivenRoll;
import yagen.waitmydawn.yagens_attributes.setup.RivenService;
import java.util.List;

public final class UnknownRivenItem extends Item {
    private final RivenRoll.Pool pool;
    public UnknownRivenItem(Properties properties, RivenRoll.Pool pool) { super(properties); this.pool = pool; }
    public RivenRoll.Pool pool() { return pool; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yagens_attributes.unknown_riven.reveal").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            try { RivenService.reveal(serverPlayer, hand, pool); }
            catch (IllegalArgumentException exception) {
                player.displayClientMessage(Component.translatable(exception.getMessage()), true);
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
