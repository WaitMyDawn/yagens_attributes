package yagen.waitmydawn.api.mission;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.network.SyncMissionDataPacket;
import yagen.waitmydawn.registries.LootTableRegistry;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static yagen.waitmydawn.api.mission.MissionHandler.getCorrectTreasurePos;


public class MissionData extends SavedData {
    private static final String DATA_NAME = YagensAttributes.MODID + "_mission";

    private final Map<ResourceLocation, Map<ResourceLocation, SharedTaskData>> data =
            new ConcurrentHashMap<>();

    public static final int AREA_SIZE = 16;

    public boolean createSharedTask(ServerLevel level, ResourceLocation levelId, ResourceLocation task,
                                    MissionType missionType, int missionLevel, Vec3 pos, int maxProgress, double distance, double missionRange,
                                    Set<UUID> players) {
        if (anyPlayerInActiveTask(levelId, players)) {
            for (UUID uuid : players) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("ui.yagens_attributes.mission_create_failed"));
                }
            }
            return false;
        }
        SharedTaskData sData = new SharedTaskData();
        sData.missionType = missionType;
        sData.missionLevel = missionLevel;
        sData.missionPosition = pos;
        sData.maxProgress = maxProgress;
        sData.progress = 0;
        sData.summonCount = 0;
        sData.distance = distance;
        sData.missionRange = missionRange;
        sData.completed = 0;
        sData.players.addAll(players);

        data.computeIfAbsent(levelId, k -> new ConcurrentHashMap<>())
                .put(task, sData);
        setDirty();
        sendPacket(level, task, sData);
        return true;
    }

    public boolean createTreasure(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null) return false;
        Vec3 missionPos = sData.missionPosition;
        BlockPos missionPosBlock = BlockPos.containing(missionPos);
        ChunkPos chunkPos = new ChunkPos(missionPosBlock);
        level.getChunk(chunkPos.x, chunkPos.z);

        BlockPos basicChestPos = new BlockPos(missionPosBlock);
        BlockPos chestPos = getCorrectTreasurePos(level, basicChestPos);
        if (!level.getWorldBorder().isWithinBounds(chestPos)) return false;
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            ResourceKey<LootTable> key = LootTableRegistry.getMissionTreasureKey(
                    sData.missionType
                    , sData.missionLevel);
            long seed = level.getRandom().nextLong();
            chest.setLootTable(key, seed);

            chest.getPersistentData().putString("TaskId", taskId.toString());
        }

        for (UUID uuid : sData.players) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.sendSystemMessage(Component.translatable("ui.yagens_attributes.mission_treasure_created")
                        .append(Component.literal("[" + chestPos + "]").withStyle(ChatFormatting.GOLD)));
            }
        }

        return true;
    }

    public boolean anyPlayerInActiveTask(ResourceLocation level, Collection<UUID> players) {
        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return false;

        for (SharedTaskData sData : taskMap.values()) {
            if (sData.completed > 0) continue;
            for (UUID uuid : players) {
                if (sData.players.contains(uuid)) return true;
            }
        }
        return false;
    }

    public void addProgress(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null || sData.completed > 0) return;
        sData.progress++;
        checkCompleted(level, levelId, taskId, sData);
        setDirty();
        sendPacket(level, taskId, sData);
    }

    public static void sendPacket(ServerLevel level, ResourceLocation taskId, SharedTaskData sData) {
        SyncMissionDataPacket pkt = createPacket(taskId, sData);
        for (UUID id : sData.players) {
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(id);
            if (sp != null) PacketDistributor.sendToPlayer(sp, pkt);
        }
    }

    public static SyncMissionDataPacket createPacket(ResourceLocation taskId, MissionData.SharedTaskData sData) {
        return new SyncMissionDataPacket(taskId,
                sData.missionType.getValue(),
                sData.missionLevel,
                sData.progress,
                sData.maxProgress,
                sData.summonCount,
                sData.deathCount,
                sData.distance,
                sData.missionRange,
                sData.missionPosition.x,
                sData.missionPosition.y,
                sData.missionPosition.z,
                sData.completed);
    }

    public void addSummonCount(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null || sData.completed > 0) return;
        sData.summonCount++;
        setDirty();
        sendPacket(level, taskId, sData);
    }

    public void addDeathCount(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null || sData.completed > 0) return;
        sData.deathCount++;
        checkCompleted(level, levelId, taskId, sData);
        setDirty();
        sendPacket(level, taskId, sData);
    }

    public int checkCompleted(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId, SharedTaskData sData) {
        if (sData.progress >= sData.maxProgress || sData.completed == 1) {
            sData.completed = 1;
            clearSummonedEntitiesByTaskId(level, taskId);
            createTreasure(level, levelId, taskId);
            return 1;
        } else if (sData.deathCount >= 4 || sData.completed == 2) {
            sData.completed = 2;
            clearSummonedEntitiesByTaskId(level, taskId);
            return 2;
        }
        return 0;
    }

    public static int clearSummonedEntitiesByTaskId(ServerLevel level, ResourceLocation taskId) {
        List<Mob> toRemove = new ArrayList<>();
        for (Entity entity : level.getAllEntities())
            if (entity instanceof Mob mob)
                if (taskId.toString().equals(mob.getPersistentData().getString("TaskId")))
                    toRemove.add(mob);
        for (Mob mob : toRemove)
            mob.discard();
        return toRemove.size();
    }

    public static int clearSummonedEntities(ServerLevel level) {
        List<Mob> toRemove = new ArrayList<>();
        for (Entity entity : level.getAllEntities())
            if (entity instanceof Mob mob)
                if (!mob.getPersistentData().getString("TaskId").equals(""))
                    toRemove.add(mob);
        for (Mob mob : toRemove)
            mob.discard();
        return toRemove.size();
    }

    public static double distanceToMissionPosition(Player player, SharedTaskData sData) {
        return Math.sqrt(Math.pow(player.getX() - sData.missionPosition.x, 2)
                + Math.pow(player.getZ() - sData.missionPosition.z, 2));
    }

    public static Map.Entry<ServerPlayer, Double> nearestPlayer(MinecraftServer server,
                                                                SharedTaskData sData) {
        double minDistance = Double.MAX_VALUE;
        ServerPlayer nearest = null;

        for (UUID uuid : sData.players) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            double distance = distanceToMissionPosition(player, sData);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        return nearest == null ? null :
                Map.entry(nearest, minDistance);
    }

    public static int getExterminateAreaCount(SharedTaskData sData) {
        return (int) sData.distance / AREA_SIZE + 1;
    }

    public static int getExterminateAreaEntityCount(SharedTaskData sData, int areaCount) {
        return (int) Math.ceil(sData.maxProgress * 1.5 / areaCount);
    }

    public void setMissionPosition(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId, Vec3 missionPosition) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData != null) {
            sData.missionPosition = missionPosition;
            setDirty();
            sendPacket(level, taskId, sData);
        }
    }

    public void setCompleted(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData != null) {
            sData.completed = 1;
            checkCompleted(level, levelId, taskId, sData);
            setDirty();
            sendPacket(level, taskId, sData);
        }
    }

    public void setFailed(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData != null) {
            sData.completed = 2;
            checkCompleted(level, levelId, taskId, sData);
            setDirty();
            sendPacket(level, taskId, sData);
        }
    }

    public boolean isPlayerInTask(ResourceLocation level, ResourceLocation task, Player player) {
        SharedTaskData sData = getData(level, task);
        return sData != null && sData.players.contains(player.getUUID());
    }

//    public Set<UUID> getPlayersInTask(ResourceLocation level, ResourceLocation task) {
//        return Collections.unmodifiableSet(
//                data.getOrDefault(level, Map.of())
//                        .getOrDefault(task, Map.of())
//                        .keySet());
//    }

    private SharedTaskData getData(ResourceLocation level, ResourceLocation task) {
        return data.getOrDefault(level, Map.of()).get(task);
    }

    public SharedTaskData getSharedTaskById(ResourceLocation levelId, ResourceLocation taskId) {
        return getData(levelId, taskId);
    }

    @Nullable
    public ResourceLocation getPlayerActiveTaskId(Player player) {
        ResourceLocation level = player.level().dimension().location();

        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return null;

        for (Map.Entry<ResourceLocation, SharedTaskData> e : taskMap.entrySet()) {
            SharedTaskData sData = e.getValue();
            if (sData.completed == 0 && sData.players.contains(player.getUUID())) {
                return e.getKey();
            }
        }
        return null;
    }

    @Nullable
    public Map.Entry<ResourceLocation, SharedTaskData> getPlayerActiveTask(Player player) {
        ResourceLocation level = player.level().dimension().location();

        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return null;

        for (Map.Entry<ResourceLocation, SharedTaskData> e : taskMap.entrySet()) {
            SharedTaskData sData = e.getValue();
            if (sData.completed == 0 && sData.players.contains(player.getUUID())) {
                return e;
            }
        }
        return null;
    }

    public boolean isPlayerOnMission(Player player) {
        ResourceLocation level = player.level().dimension().location();

        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return false;

        for (Map.Entry<ResourceLocation, SharedTaskData> e : taskMap.entrySet()) {
            SharedTaskData sData = e.getValue();
            if (sData.completed == 0 && sData.players.contains(player.getUUID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        data.forEach((level, taskMap) -> {
            CompoundTag levelTag = new CompoundTag();
            taskMap.forEach((task, sData) -> {
                CompoundTag taskTag = new CompoundTag();
                taskTag.putString("missionType", sData.missionType.getValue());
                taskTag.putInt("missionLevel", sData.missionLevel);
                taskTag.putInt("progress", sData.progress);
                taskTag.putInt("maxProgress", sData.maxProgress);
                taskTag.putInt("summonCount", sData.summonCount);
                taskTag.putInt("deathCount", sData.deathCount);
                taskTag.putDouble("distance", sData.distance);
                taskTag.putDouble("missionRange", sData.missionRange);
                taskTag.putDouble("missionX", sData.missionPosition.x);
                taskTag.putDouble("missionY", sData.missionPosition.y);
                taskTag.putDouble("missionZ", sData.missionPosition.z);
                taskTag.putInt("completed", sData.completed);
                ListTag playerList = new ListTag();
                sData.players.forEach(id -> playerList.add(NbtUtils.createUUID(id)));
                taskTag.put("players", playerList);
                levelTag.put(task.toString(), taskTag);
            });
            root.put(level.toString(), levelTag);
        });
        nbt.put("mission", root);
        return nbt;
    }

    public static MissionData load(CompoundTag nbt, HolderLookup.Provider provider) {
        MissionData inst = new MissionData();
        if (!nbt.contains("mission", Tag.TAG_COMPOUND)) return inst;
        CompoundTag root = nbt.getCompound("mission");
        root.getAllKeys().forEach(levelKey -> {
            ResourceLocation level = ResourceLocation.tryParse(levelKey);
            if (level == null) return;
            CompoundTag levelTag = root.getCompound(levelKey);
            levelTag.getAllKeys().forEach(taskKey -> {
                ResourceLocation task = ResourceLocation.tryParse(taskKey);
                if (task == null) return;
                CompoundTag taskTag = levelTag.getCompound(taskKey);
                SharedTaskData sData = new SharedTaskData();
                sData.missionType = MissionType.fromString(taskTag.getString("missionType"));
                sData.missionLevel = taskTag.getInt("missionLevel");
                sData.progress = taskTag.getInt("progress");
                sData.maxProgress = taskTag.getInt("maxProgress");
                sData.summonCount = taskTag.getInt("summonCount");
                sData.deathCount = taskTag.getInt("deathCount");
                sData.distance = taskTag.getDouble("distance");
                sData.missionRange = taskTag.getDouble("missionRange");
                sData.missionPosition = new Vec3(
                        taskTag.getDouble("missionX"),
                        taskTag.getDouble("missionY"),
                        taskTag.getDouble("missionZ"));
                sData.completed = taskTag.getInt("completed");
                ListTag playerList = taskTag.getList("players", Tag.TAG_INT_ARRAY);
                playerList.forEach(tag -> sData.players.add(NbtUtils.loadUUID(tag)));
                inst.data.computeIfAbsent(level, k -> new ConcurrentHashMap<>())
                        .put(task, sData);
            });
        });
        return inst;
    }

    public static MissionData get(@Nullable MinecraftServer server) {
        if (server == null) {
            return ClientMissionDataProxy.INSTANCE;
        }
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(MissionData::new, MissionData::load),
                DATA_NAME);
    }

    public void clearAll(ServerLevel level) {
        ResourceLocation taskId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mission_clear");
        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level.dimension().location());
        if (taskMap == null) return;
        Set<UUID> allPlayers = new HashSet<>();
        for (Map.Entry<ResourceLocation, SharedTaskData> e : taskMap.entrySet()) {
            SharedTaskData tempData = e.getValue();
            for (UUID id : tempData.players)
                if (id != null)
                    allPlayers.add(id);

        }
        data.clear();
        setDirty();
        SharedTaskData sData = new SharedTaskData();
        sData.players = allPlayers;
        sData.missionType = MissionType.EXTERMINATE;
        sData.missionPosition = new Vec3(0, 0, 0);
        sData.completed = 1;
        sendPacket(level, taskId, sData);
    }

    public void clearTaskByStatusId(int statusId) {// 0 = ing; 1 = completed; 2 = failed
        data.values().forEach(taskMap ->
                taskMap.entrySet().removeIf(e -> e.getValue().completed == statusId)
        );
        data.entrySet().removeIf(e -> e.getValue().isEmpty());
        setDirty();
    }

    public static class SharedTaskData {
        public MissionType missionType;
        public int missionLevel;
        public int progress;
        public int maxProgress;
        public int summonCount;
        public int deathCount;
        public double distance;
        public double missionRange;
        public Vec3 missionPosition;
        public int completed;
        public Set<UUID> players = new HashSet<>();
    }
}