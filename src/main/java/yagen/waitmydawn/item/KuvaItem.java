package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.List;
import java.util.UUID;

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
        int decrement = 1;

        if (!level.isClientSide() && stack.getItem() == ItemRegistry.RING_OF_KING.get()) {
            if (stack.get(ComponentRegistry.OWNER.get()) != null) {
                ComponentRegistry.KuvaTime kuvaTime = stack.getOrDefault(ComponentRegistry.KUVA_TIME.get(),
                        ComponentRegistry.KuvaTime.EMPTY);
                long curTime = System.currentTimeMillis() / 1000;
                if (curTime - kuvaTime.startTime() > kuvaTime.continuity())
                    stack.set(ComponentRegistry.KUVA_TIME.get(),
                            new ComponentRegistry.KuvaTime(System.currentTimeMillis() / 1000,
                                    1800));
                else
                    stack.set(ComponentRegistry.KUVA_TIME.get(),
                            new ComponentRegistry.KuvaTime(kuvaTime.startTime(),
                                    kuvaTime.continuity() + 1800));
                kuvaStack.shrink(decrement--);
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.PLAYER_BURP,
                        SoundSource.PLAYERS
                );
            }
        }

        if (times <= 0) {
            if (level.isClientSide() && stack.getItem() != ItemRegistry.RING_OF_KING.get())
                player.sendSystemMessage(Component.translatable("item.yagens_attributes.kuva.tooltip2"));
            return InteractionResultHolder.pass(kuvaStack);
        } else {
            if (!level.isClientSide()) {
                if (!player.isCreative())
                    kuvaStack.shrink(decrement);
                stack.set(DataComponents.REPAIR_COST, 0);
            }
            return InteractionResultHolder.sidedSuccess(kuvaStack, level.isClientSide());
        }
    }
}
