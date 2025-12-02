package yagen.waitmydawn.api.mission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.entity.SummonEntityBlackList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static yagen.waitmydawn.api.events.EntityLevelBonusEvent.modifierEntityLevel;
import static yagen.waitmydawn.api.mission.MissionData.distanceToMissionPosition;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionHandler {
    private static boolean executed = false;
    private static final List<EntityType<? extends Monster>> MONSTER_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> BOSS_TYPES = new ArrayList<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (executed) return;
        executed = true;
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> type.getCategory() == MobCategory.MONSTER)
                .filter(type -> !type.is(Tags.EntityTypes.BOSSES))
                .filter(type -> !type.is(SummonEntityBlackList.MONSTER_BLACK_LIST))
                .forEach(type -> {
                    try {
                        MONSTER_TYPES.add((EntityType<? extends Monster>) type);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster: " + type);
                    }
                });
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> (type.is(Tags.EntityTypes.BOSSES)
                        || BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals("born_in_chaos_v1:lord_pumpkinhead")
                ))
                .filter(type -> !type.is(SummonEntityBlackList.BOSS_BLACK_LIST))
                .forEach(type -> {
                    try {
                        BOSS_TYPES.add((EntityType<? extends Monster>) type);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster (Boss): " + type);
                    }
                });
//        MONSTER_TYPES.forEach(type ->
//                System.out.println("find Monster : " + type.toString())
//        );
//        BOSS_TYPES.forEach(type ->
//                System.out.println("find Monster (Boss) : " + type.toString())
//        );
    }

    public static EntityType<? extends Monster> randomMonsterType(RandomSource random) {
        if (MONSTER_TYPES.isEmpty()) {
            return EntityType.ZOMBIE;
        }
        return MONSTER_TYPES.get(random.nextInt(MONSTER_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomBossType(RandomSource random) {
        return BOSS_TYPES.get(random.nextInt(BOSS_TYPES.size()));
    }

    public static <T extends Mob> T summonEntity(EntityType<T> type,
                                                 ServerLevel level,
                                                 Vec3 pos) {
        T mob = type.create(level);
        if (mob == null) return null;
        mob.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360F, 0);

        level.addFreshEntity(mob);
        return mob;
    }

    public static <T extends Mob> T summonExterminateEntity(EntityType<T> type,
                                                            ServerLevel level, Vec3 pos,
                                                            ResourceLocation taskId, int missionLevel) {
        T mob = summonEntity(type, level, pos);
        if (mob != null) {
            mob.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    2000000,
                    0,
                    false, false));
            mob.getPersistentData().putString("TaskId", taskId.toString());
            modifierEntityLevel(mob, AttributeModifier.Operation.ADD_VALUE, summonEntityLevelBonus[missionLevel], "exterminate_base_level");
        }
        return mob;
    }

    private static final int[] normalDistance = {400, 800, 1200};
    private static final int[] shortDistance = {100, 200, 400};
    private static final int[] exterminateMaxProgress = {40, 80, 120};
    private static final int[] waveMaxProgress = {5, 10, 20};
    private static final int[] summonEntityLevelBonus = {5, 20, 40};

    public static double getRandMissionDistance(Level level, Player player, int missionLevel, String missionType) {
        int distance;
        switch (missionType) {
            case "Defense", "Survival" -> {
                distance = shortDistance[missionLevel];
            }
            default -> {
                distance = normalDistance[missionLevel];
            }
        }
        return (0.8 + player.getRandom().nextDouble() / 2.5) * distance;
    }

    public static int getRandMaxProgress(Level level, Player player, int missionLevel, String missionType, int playerCount) {
        int maxProgress;
        switch (missionType) {
            case "Defense", "Survival" -> {
                maxProgress = waveMaxProgress[missionLevel];
                return maxProgress;
            }
            default -> {
                maxProgress = exterminateMaxProgress[missionLevel];
            }
        }
        return (int) ((0.8 + player.getRandom().nextDouble() / 2.5) * Math.pow(1.2, playerCount - 1) * maxProgress);
    }

    public static Vec3 getRandMissionPosition(Level level, Player player, double distance) {
        float yaw = player.getRandom().nextFloat() * Mth.TWO_PI;
        double dx = Mth.sin(yaw) * distance;
        double dz = Mth.cos(yaw) * distance;

        Vec3 center = player.position();
        int x = Mth.floor(center.x + dx);
        int z = Mth.floor(center.z + dz);

        int y = (int) player.getY();
//                level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        if (!level.getWorldBorder().isWithinBounds(pos)) {
            pos = new BlockPos(
                    Mth.clamp(x,
                            (int) level.getWorldBorder().getMinX() + 1,
                            (int) level.getWorldBorder().getMaxX() - 1),
                    y,
                    Mth.clamp(z,
                            (int) level.getWorldBorder().getMinZ() + 1,
                            (int) level.getWorldBorder().getMaxZ() - 1)
            );
        }

        return Vec3.atCenterOf(pos);
    }

    public static BlockPos getBasicSpawnBlockPosByEye(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double distance = Mth.nextDouble(player.getRandom(), 6, 24);
        float angle = (player.getRandom().nextFloat() * 2 - 1) * 45 * Mth.DEG_TO_RAD;
        Vec3 offset = new Vec3(
                look.x * Math.cos(angle) - look.z * Math.sin(angle),
                0,
                look.x * Math.sin(angle) + look.z * Math.cos(angle))
                .normalize().scale(distance);
        Vec3 basicSpawnPos = eye.add(offset);
        return BlockPos.containing(basicSpawnPos.x, player.getY(), basicSpawnPos.z);
    }

    public static BlockPos getBasicSpawnBlockPosByArea(Vec3 missionPosition, double missionRange, Player player) {
        double distance = Mth.nextDouble(player.getRandom(), missionRange / 2, missionRange);
        float angle = player.getRandom().nextFloat() * Mth.DEG_TO_RAD;
        Vec3 offset = new Vec3(
                Math.cos(angle),
                0,
                Math.sin(angle))
                .normalize().scale(distance);
        return BlockPos.containing(missionPosition.add(offset));
    }

    public static Vec3 getCorrectSpawnPos(Level level, BlockPos basicSpawnBlock) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, basicSpawnBlock.getX(), basicSpawnBlock.getZ());
        Vec3 spawnPos;
        if (level.dimension() == Level.NETHER && y > 125) {//nether floor
            int startY = basicSpawnBlock.getY();
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

            for (int yi = startY; yi <= startY + 12; yi++) {
                m.set(basicSpawnBlock.getX(), yi, basicSpawnBlock.getZ());
                BlockState below = level.getBlockState(m.below());
                BlockState here = level.getBlockState(m);

                if (below.canOcclude() && here.isAir() && level.noCollision(
                        AABB.ofSize(Vec3.atCenterOf(m).add(0, 0.5, 0), 0.8, 1.8, 0.8))) {
                    spawnPos = Vec3.atCenterOf(m);
                    return spawnPos;
                }
            }
        }
        if (y - basicSpawnBlock.getY() > 12) return null;// too high or air land blocked
        spawnPos = Vec3.atCenterOf(new BlockPos(basicSpawnBlock.getX(), y, basicSpawnBlock.getZ()));
        return spawnPos;
    }

    public static BlockPos getCorrectTreasurePos(Level level, BlockPos basicSpawnBlock) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, basicSpawnBlock.getX(), basicSpawnBlock.getZ());
        BlockPos spawnPos;
        int startY = basicSpawnBlock.getY();
        if (level.dimension() == Level.NETHER && y > 125) {//nether floor
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

            for (int yi = startY / 2; yi <= 124; yi++) {
                m.set(basicSpawnBlock.getX(), yi, basicSpawnBlock.getZ());
                BlockState here = level.getBlockState(m);
                BlockState below = level.getBlockState(m.below());
                if (below.canOcclude() && here.isAir()) {
                    spawnPos = m;
                    return spawnPos;
                }
            }
        }
        spawnPos = new BlockPos(basicSpawnBlock.getX(), y, basicSpawnBlock.getZ());
        if (level.getBlockState(spawnPos.below()).canOcclude()
                && level.getBlockState(spawnPos).isAir())
            return spawnPos;
        else {
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

            for (int yi = y; yi >= -32; yi--) {
                m.set(basicSpawnBlock.getX(), yi, basicSpawnBlock.getZ());
                BlockState here = level.getBlockState(m);
                BlockState below = level.getBlockState(m.below());
                if (below.canOcclude() && here.isAir()) {
                    spawnPos = m;
                    return spawnPos;
                }
            }
        }
        return spawnPos;
    }

    public static boolean isAllInMissionPosition(MinecraftServer server, MissionData.SharedTaskData sData) {
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

    public static Set<UUID> nearbyPlayers(Player creator, double radius) {
        AABB box = AABB.ofSize(creator.position(), radius * 2, radius * 2, radius * 2);
        return creator.level().getEntities(EntityType.PLAYER, box,
                        p -> p.distanceToSqr(creator) <= radius * radius)
                .stream()
                .map(Player::getUUID)
                .collect(Collectors.toSet());
    }
}
