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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.api.mission.MissionType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static yagen.waitmydawn.api.mission.MissionHandler.*;

public class MissionCreatorItem extends Item {
    public MissionCreatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        if (!level.isClientSide) {
            double distance = getRandMissionDistance(level, player);
            Vec3 missionPosition = getRandMissionPosition(level, player, distance);
            double missionRange = 10;
            int maxProgress = 40;
            ResourceLocation levelId = level.dimension().location();
            Set<UUID> players = nearbyPlayers(player, 3);

            long uuidSum = players.stream()
                    .map(uuid -> uuid.toString().substring(0, 8))
                    .mapToLong(hex -> Long.parseLong(hex, 16))
                    .sum();
            long timestamp = System.currentTimeMillis() / 1000L;

            ResourceLocation taskId = ResourceLocation.fromNamespaceAndPath(
                    YagensAttributes.MODID, "exterminate_" + uuidSum + "_" + timestamp);

            MissionData data = MissionData.get(((ServerLevel) level).getServer());
            if (data.createSharedTask(
                    (ServerLevel) level,
                    levelId,
                    taskId,
                    MissionType.EXTERMINATE,
                    missionPosition,
                    maxProgress, distance, missionRange, players))
                player.sendSystemMessage(Component.literal("Mission Created!").withStyle(ChatFormatting.DARK_PURPLE));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }
}
