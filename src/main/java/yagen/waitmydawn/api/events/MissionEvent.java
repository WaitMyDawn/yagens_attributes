package yagen.waitmydawn.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.api.mission.MissionType;

import java.util.Objects;
import java.util.Random;

import static yagen.waitmydawn.api.mission.MissionData.*;
import static yagen.waitmydawn.api.mission.MissionHandler.*;


@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionEvent {
    @SubscribeEvent
    public static void exterminateEvent(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        Level level = player.level();
        if (level.isClientSide) return;

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MissionData data = MissionData.get(server);
        var active = data.getPlayerActiveTask(player);
        if (active == null) return;
        MissionData.SharedTaskData sData = active.getValue();
        if (sData.missionType != MissionType.EXTERMINATE) return;
        ResourceLocation taskId = active.getKey();
        if (!mob.getPersistentData().getString("TaskId").equals(taskId.toString())) return;

        data.addProgress((ServerLevel) level, level.dimension().location(), taskId);
    }

    @SubscribeEvent
    public static void exterminateEntitySummonEvent(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 10 != 0) return;
        Level level = player.level();
        if (level.isClientSide) return;

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MissionData data = MissionData.get(server);
        var active = data.getPlayerActiveTask(player);
        if (active == null) return;
        MissionData.SharedTaskData sData = active.getValue();
        if (sData.missionType != MissionType.EXTERMINATE) return;
        ResourceLocation taskId = active.getKey();

        var nearestPlayerMap = MissionData.nearestPlayer(server, sData);
        Player nearestPlayer = nearestPlayerMap.getKey();
        if (!(nearestPlayer == player)) return;
        double moveDistance = (int) Math.max(sData.distance - nearestPlayerMap.getValue(), 0);
        int areaCount = getExterminateAreaCount(sData);
        int areaEntityCount = getExterminateAreaEntityCount(sData, areaCount);
        int areaCur = (int) (moveDistance / AREA_SIZE) + 1;
        Vec3 spawnPos = null;
        if (sData.summonCount < areaEntityCount * areaCur) {// summon according to current area
            BlockPos basicSpawnBlock = getBasicSpawnBlockPosByEye(player);
            spawnPos = getCorrectSpawnPos(level, basicSpawnBlock);
        } else if (isAllInMissionPosition(server, sData)
                && sData.progress < sData.maxProgress) {// compensatory summon
            BlockPos basicSpawnBlock = getBasicSpawnBlockPosByArea(sData.missionPosition, sData.missionRange, player);
            spawnPos = getCorrectSpawnPos(level, basicSpawnBlock);
        }
        if (spawnPos == null || !level.getWorldBorder().isWithinBounds(spawnPos)) return;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        Mob mob = summonExterminateEntity(
                randomMonsterByMaxHealthLevel(serverPlayer.getRandom(),
                        getExterminateSummonLevel(serverPlayer.getRandom(),sData.missionLevel)),
                serverPlayer.serverLevel(),
                spawnPos,
                taskId, sData.missionLevel);
        mob.setTarget(player);
        data.addSummonCount(serverPlayer.serverLevel(), player.level().dimension().location(), taskId);
    }

    public static int getExterminateSummonLevel(RandomSource random,int missionLevel) {
        double randomNum = random.nextDouble();
        if (missionLevel == 0) {
            if (randomNum > 0.6 && randomNum < 0.9) {
                return 1;
            } else if (randomNum >= 0.9)
                return 2;
        }
        if(missionLevel==1){
            if (randomNum > 0.6 && randomNum < 0.9) {
                return 2;
            } else if (randomNum >= 0.9)
                return 3;
        }
        if(missionLevel==2){
            if (randomNum > 0.6 && randomNum < 0.9) {
                return 3;
            } else if (randomNum >= 0.9)
                return 4;
        }
        return missionLevel;
    }

}
