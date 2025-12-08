package yagen.waitmydawn.api.mission;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.item.MapItem;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.entity.SummonEntityBlackList;
import yagen.waitmydawn.entity.others.DarkDoppelgangerEntity;

import java.util.*;
import java.util.stream.Collectors;

import static yagen.waitmydawn.api.events.AttackEventHandler.forceEffect;
import static yagen.waitmydawn.api.events.EntityLevelBonusEvent.modifierEntityLevel;
import static yagen.waitmydawn.api.mission.MissionData.distanceToMissionPosition;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionHandler {
    private static boolean executed = false;
    private static final List<EntityType<? extends Monster>> MONSTER_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> MONSTER_UNDER_20_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> MONSTER_20_30_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> MONSTER_30_60_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> MONSTER_60_200_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> MONSTER_OVER_200_TYPES = new ArrayList<>();
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
                    EntityType<? extends Monster> type1 = (EntityType<? extends Monster>) type;
                    try {
                        MONSTER_TYPES.add(type1);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster: " + type);
                    }

                    Entity entity = type.create(ServerLifecycleHooks.getCurrentServer().overworld());
                    if (entity instanceof LivingEntity living) {
                        double maxHealth = living.getMaxHealth();
                        if (maxHealth < 20)
                            MONSTER_UNDER_20_TYPES.add(type1);
                        else if (maxHealth > 20 && maxHealth < 30)
                            MONSTER_20_30_TYPES.add(type1);
                        else if (maxHealth > 30 && maxHealth < 60)
                            MONSTER_30_60_TYPES.add(type1);
                        else if (maxHealth > 60 && maxHealth < 200)
                            MONSTER_60_200_TYPES.add(type1);
                        else if (maxHealth > 200)
                            MONSTER_OVER_200_TYPES.add(type1);
                        else if (maxHealth == 20) {
                            MONSTER_UNDER_20_TYPES.add(type1);
                            MONSTER_20_30_TYPES.add(type1);
                        } else if (maxHealth == 30) {
                            MONSTER_20_30_TYPES.add(type1);
                            MONSTER_30_60_TYPES.add(type1);
                        } else if (maxHealth == 60) {
                            MONSTER_30_60_TYPES.add(type1);
                            MONSTER_60_200_TYPES.add(type1);
                        } else if (maxHealth == 200) {
                            MONSTER_60_200_TYPES.add(type1);
                            MONSTER_OVER_200_TYPES.add(type1);
                        }
                    }
                });
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> (type.is(Tags.EntityTypes.BOSSES)
                        || BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals("born_in_chaos_v1:lord_pumpkinhead")
                        || type == DarkDoppelgangerEntity.DARK_DOPPELGANGER.get()
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

    public static EntityType<? extends Monster> randomMonsterByMaxHealthLevel(RandomSource random, int level) {
        switch (level) {
            case 1 -> {
                return randomMonster20To30Type(random);
            }
            case 2 -> {
                return randomMonster30To60Type(random);
            }
            case 3 -> {
                return randomMonster60To200Type(random);
            }
            case 4 -> {
                return randomMonsterOver200Type(random);
            }
            default -> {
                return randomMonsterUnder20Type(random);
            }
        }
    }

    public static EntityType<? extends Monster> randomMonsterUnder20Type(RandomSource random) {
        if (MONSTER_UNDER_20_TYPES.isEmpty()) {
            return EntityType.ZOMBIE;
        }
        return MONSTER_UNDER_20_TYPES.get(random.nextInt(MONSTER_UNDER_20_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomMonster20To30Type(RandomSource random) {
        if (MONSTER_20_30_TYPES.isEmpty()) {
            return EntityType.VINDICATOR;
        }
        return MONSTER_20_30_TYPES.get(random.nextInt(MONSTER_20_30_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomMonster30To60Type(RandomSource random) {
        if (MONSTER_30_60_TYPES.isEmpty()) {
            return EntityType.PIGLIN_BRUTE;
        }
        return MONSTER_30_60_TYPES.get(random.nextInt(MONSTER_30_60_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomMonster60To200Type(RandomSource random) {
        if (MONSTER_60_200_TYPES.isEmpty()) {
            return EntityType.ELDER_GUARDIAN;
        }
        return MONSTER_60_200_TYPES.get(random.nextInt(MONSTER_60_200_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomMonsterOver200Type(RandomSource random) {
        if (MONSTER_OVER_200_TYPES.isEmpty()) {
            return EntityType.WARDEN;
        }
        return MONSTER_OVER_200_TYPES.get(random.nextInt(MONSTER_OVER_200_TYPES.size()));
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

        if (mob instanceof Warden warden) {
            Player nearest = warden.level().getNearestPlayer(warden, 32.0D);
            warden.increaseAngerAt(nearest, 80, true);
        }

        return mob;
    }

    public static <T extends Mob> T summonExterminateEntity(EntityType<T> type,
                                                            ServerLevel level, Vec3 pos,
                                                            ResourceLocation taskId, int missionLevel) {
        T mob = summonEntity(type, level, pos);
        if (mob != null) {
            forceEffect(mob, new MobEffectInstance(
                    MobEffects.GLOWING,
                    2000000,
                    0,
                    false, false));
            mob.getPersistentData().putString("TaskId", taskId.toString());
            modifierEntityLevel(mob, AttributeModifier.Operation.ADD_VALUE, summonEntityLevelBonus[missionLevel], "exterminate_base_level");
        }
        return mob;
    }

    public static <T extends Mob> T summonAssassinationEntity(EntityType<T> type,
                                                              ServerLevel level, Vec3 pos,
                                                              ResourceLocation taskId, int missionLevel) {
        T mob = summonEntity(type, level, pos);
        if (mob != null) {
            forceEffect(mob, new MobEffectInstance(
                    MobEffects.GLOWING,
                    2000000,
                    0,
                    false, false));
            mob.getPersistentData().putString("TaskId", taskId.toString());
            modifierEntityLevel(mob, AttributeModifier.Operation.ADD_VALUE, summonBossLevelBonus[missionLevel], "assassination_base_level");
        }
        return mob;
    }

    private static final int[] normalDistance = {400, 800, 1200};
    private static final int[] shortDistance = {100, 200, 400};
    private static final int[] exterminateMaxProgress = {40, 80, 120};
    private static final int[] waveMaxProgress = {5, 10, 20};
    private static final int[] summonEntityLevelBonus = {5, 20, 40};
    private static final int[] summonBossLevelBonus = {50, 100, 200};

    public static double getRandMissionDistance(Level level, Player player, int missionLevel, String missionType) {
        int distance;
        switch (missionType) {
            case "Defense", "Survival" -> {
                distance = shortDistance[missionLevel];
            }
            case "Assassination" -> {
                distance = 200;
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
            case "Assassination" -> {
                return 1;
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

    public static void sendMissionMap(ServerLevel serverLevel, Set<UUID> players, Vec3 missionPosition, ResourceLocation taskId) {
        MapItemSavedData mapData = MapItemSavedData.createFresh(
                missionPosition.x(),
                missionPosition.z(),
                (byte) 4,
                true,
                true,
                serverLevel.dimension()
        );

        int mapId = serverLevel.getFreeMapId().id();
//        serverLevel.getDataStorage().set(new MapId(mapId).key(), mapData);
        serverLevel.setMapData(new MapId(mapId), mapData);

        ItemStack mapStack = new ItemStack(Items.FILLED_MAP);
        MapItemSavedData.addTargetDecoration(mapStack, BlockPos.containing(missionPosition), "+", MapDecorationTypes.TARGET_X);
        mapStack.set(DataComponents.MAP_ID, new MapId(mapId));
        mapStack.set(DataComponents.CUSTOM_NAME,
                Component.translatable("ui.yagens_attributes.mission_map_name").withStyle(ChatFormatting.GOLD));

        players.forEach(uuid -> {
            ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (p == null) return;
            if (!p.getInventory().add(mapStack.copy()))
                p.drop(mapStack.copy(), false);
        });
    }
}
