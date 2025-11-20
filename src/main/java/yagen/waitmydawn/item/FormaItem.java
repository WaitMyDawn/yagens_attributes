package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.List;

public class FormaItem extends Item {

    public FormaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack formaStack = player.getItemInHand(hand);
            String type = formaStack.getOrDefault(ComponentRegistry.FORMA_TYPE, "");
            if (!type.isEmpty()) {
                FormaType newType = FormaType.values()[(FormaType.fromString(type).ordinal() + 1) % FormaType.values().length];
                formaStack.set(ComponentRegistry.FORMA_TYPE.get(), newType.getValue());
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        String type = stack.getOrDefault(ComponentRegistry.FORMA_TYPE, "");
        if (!type.isEmpty()) {
            tooltip.add(Component.translatable("item.yagens_attributes.forma.tooltip1", type)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.yagens_attributes.forma.tooltip2")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}