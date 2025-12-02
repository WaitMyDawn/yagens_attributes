package yagen.waitmydawn.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.registries.DimensionRegistry;

public class TeleportItem extends Item {
    public TeleportItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel destination = serverPlayer.server.getLevel(
                    serverPlayer.level().dimension() == Level.OVERWORLD
                            ? DimensionRegistry.MIRROR_LEVEL
                            : Level.OVERWORLD
            );
            player.sendSystemMessage(Component.literal("Teleporting to [" + destination + "]"));
            if (destination != null) {
                serverPlayer.teleportTo(
                        destination,
                        serverPlayer.getX(),
                        serverPlayer.getY(),
                        serverPlayer.getZ(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }

//    @Override
//    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx,
//                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
//            tooltip.add(Component.translatable("item.yagens_attributes.endo.tooltip1")
//                    .withStyle(ChatFormatting.AQUA));
//    }
}
