package yagen.waitmydawn.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
import java.util.UUID;

import static yagen.waitmydawn.api.mission.MissionData.*;
import static yagen.waitmydawn.api.mission.MissionHandler.randomMonsterType;
import static yagen.waitmydawn.api.mission.MissionHandler.summonExterminateEntity;


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
        if (sData.summonCount < areaEntityCount * areaCur) {// summon according to current area
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0F);
            double distance = Mth.nextDouble(player.getRandom(), 6, 24);
            float angle = (player.getRandom().nextFloat() * 2 - 1) * 45 * Mth.DEG_TO_RAD;
            Vec3 offset = new Vec3(
                    look.x * Math.cos(angle) - look.z * Math.sin(angle),
                    0,
                    look.x * Math.sin(angle) + look.z * Math.cos(angle))
                    .normalize().scale(distance);
            Vec3 spawnPos = eye.add(offset);
            Vec3 onSurface = null;
            BlockPos surface = BlockPos.containing(spawnPos.x, 0, spawnPos.z);
            int y = getCorrectY(level, spawnPos, surface);
            if (y == -999) return;
            onSurface = Vec3.atCenterOf(new BlockPos(surface.getX(), y, surface.getZ()));
            if (!level.getWorldBorder().isWithinBounds(onSurface)) return;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Mob mob = summonExterminateEntity(randomMonsterType(serverPlayer.getRandom()), serverPlayer.serverLevel(), onSurface, taskId);
            mob.setTarget(player);
            data.addSummonCount(player.level().dimension().location(), taskId);
        }
        if (isAllInMissionPosition(server, sData)
                && sData.progress < sData.maxProgress
                && sData.summonCount >= areaEntityCount * areaCur) {

        }
    }

    private static int getCorrectY(Level level, Vec3 spawnPos, BlockPos surface) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, surface.getX(), surface.getZ());
        Vec3 onSurface = null;
        if (level.dimension() == Level.NETHER && y > 125) {//nether floor
            int startY = Mth.floor(spawnPos.y);
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

            for (int yi = startY; yi <= startY + 12; yi++) {
                m.set(surface.getX(), yi, surface.getZ());
                BlockState below = level.getBlockState(m.below());
                BlockState here = level.getBlockState(m);

                if (below.canOcclude() && here.isAir() && level.noCollision(AABB.ofSize(Vec3.atCenterOf(m), 0.8, 1.8, 0.8))) {
                    onSurface = Vec3.atCenterOf(m.below()).add(0, 1, 0);
                    break;
                }
            }
            if (onSurface == null) return -999;
        }
        if (y - spawnPos.y >= 12) return -999;// too high
        return y;
    }

    public static boolean isAllInMissionPosition(MinecraftServer server, SharedTaskData sData) {
        double maxDistance = 0;
        for (UUID uuid : sData.players) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            double distance = distanceToMissionPosition(player, sData);
            if (distance > maxDistance) {
                if (distance > sData.missionRange) return false;
                maxDistance = distance;
            }
        }
        return true;
    }


}
