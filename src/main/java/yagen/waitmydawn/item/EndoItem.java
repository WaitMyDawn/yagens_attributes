package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.api.mission.MissionType;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static yagen.waitmydawn.api.mission.MissionHandler.*;

public class EndoItem extends Item {
    public EndoItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ComponentRegistry.EndoInfo endoInfo = ComponentRegistry.getEndoInfo(player.getItemInHand(hand));
        if (!endoInfo.missionType().equals(MissionType.EXTERMINATE.getValue())
                || endoInfo == ComponentRegistry.EndoInfo.EMPTY) {
            if (level.isClientSide)
                player.sendSystemMessage(Component.literal("It is not supported now!"));

            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        if (!level.isClientSide) {
            double distance = getRandMissionDistance(level, player, endoInfo.level(), endoInfo.missionType());
            Vec3 missionPosition = getRandMissionPosition(level, player, distance);
            double missionRange = 10;
            ResourceLocation levelId = level.dimension().location();
            Set<UUID> players = nearbyPlayers(player, 3);
            int maxProgress = getRandMaxProgress(level, player, endoInfo.level(), endoInfo.missionType(), players.size());
            long uuidSum = players.stream()
                    .map(uuid -> uuid.toString().substring(0, 4))
                    .mapToLong(hex -> Long.parseLong(hex, 16))
                    .sum();
            long timestamp = System.currentTimeMillis() / 1000L;
            ResourceLocation taskId = ResourceLocation.fromNamespaceAndPath(
                    YagensAttributes.MODID, endoInfo.missionType().toLowerCase() + "_" + uuidSum + "_" + timestamp);
            MissionData data = MissionData.get(((ServerLevel) level).getServer());
            if (data.createSharedTask(
                    (ServerLevel) level,
                    levelId,
                    taskId,
                    MissionType.fromString(endoInfo.missionType()), endoInfo.level(),
                    missionPosition,
                    maxProgress, distance, missionRange, players))
                player.sendSystemMessage(Component.translatable("ui.yagens_attributes.mission_created").withStyle(ChatFormatting.DARK_PURPLE));
            if (!player.isCreative())
                player.getItemInHand(hand).shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ComponentRegistry.EndoInfo endoInfo = ComponentRegistry.getEndoInfo(stack);
        if (endoInfo != ComponentRegistry.EndoInfo.EMPTY) {
            tooltip.add(Component.translatable("item.yagens_attributes.endo.tooltip1", endoInfo.level() + 1)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.yagens_attributes.endo.tooltip2", endoInfo.missionType())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.yagens_attributes.endo.tooltip3", endoInfo.level() + 1)
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
