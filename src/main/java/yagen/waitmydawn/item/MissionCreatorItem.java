package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import static yagen.waitmydawn.api.events.MissionEvent.randomMonsterType;
import static yagen.waitmydawn.api.events.MissionEvent.summonEntity;
import static yagen.waitmydawn.api.mission.MissionCreator.getRandMissionDistance;
import static yagen.waitmydawn.api.mission.MissionCreator.getRandMissionPosition;

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
            int maxProgress = 5;
            ResourceLocation levelId = level.dimension().location();
            ResourceLocation taskId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "exterminate_shared");

            MissionData data = MissionData.get(((ServerLevel) level).getServer());
            data.createSharedTask(
                    levelId,
                    taskId,
                    MissionType.EXTERMINATE,
                    missionPosition,
                    5,distance,missionRange);
//            data.setMissionType(levelId, taskId, MissionType.EXTERMINATE);
//            data.setMissionPosition(levelId, taskId, missionPosition);
//            data.setMissionRange(levelId, taskId, missionRange);
//            data.setMaxProgress(levelId, taskId, maxProgress);
//            data.setProgress(levelId, taskId, 0);
//            data.setCompleted(levelId, taskId, false);
            player.sendSystemMessage(Component.literal("Mission Created!").withStyle(ChatFormatting.DARK_PURPLE));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }
}
