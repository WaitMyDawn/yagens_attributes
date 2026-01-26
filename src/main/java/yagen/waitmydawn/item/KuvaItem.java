package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KuvaItem extends Item {
    public KuvaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yagens_attributes.kuva.tooltip1")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.yagens_attributes.kuva.tooltip3")
                .withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.OFF_HAND)
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        ItemStack kuvaStack = player.getItemInHand(hand);
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty())
            return InteractionResultHolder.pass(kuvaStack);
        int times = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
        if (times <= 0) {
            if (level.isClientSide())
                player.sendSystemMessage(Component.translatable("item.yagens_attributes.kuva.tooltip2"));
            return InteractionResultHolder.pass(kuvaStack);
        } else {
            if (!level.isClientSide()) {
                if (!player.isCreative())
                    kuvaStack.shrink(1);
                stack.set(DataComponents.REPAIR_COST, 0);
            }
            return InteractionResultHolder.sidedSuccess(kuvaStack, level.isClientSide());
        }
    }
}
