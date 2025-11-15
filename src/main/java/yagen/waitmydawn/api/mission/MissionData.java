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
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.registries.LootTableRegistry;
import yagen.waitmydawn.YagensAttributes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class MissionData extends SavedData {
    private static final String DATA_NAME = YagensAttributes.MODID + "_mission";

    private final Map<ResourceLocation, Map<ResourceLocation, SharedTaskData>> data =
            new ConcurrentHashMap<>();

    public static final int AREA_SIZE = 16;

    public boolean createSharedTask(ServerLevel level, ResourceLocation levelId, ResourceLocation task,
                                    MissionType type, Vec3 pos, int maxProgress, double distance, double missionRange,
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
        sData.missionType = type;
        sData.missionPosition = pos;
        sData.maxProgress = maxProgress;
        sData.progress = 0;
        sData.summonCount = 0;
        sData.distance = distance;
        sData.missionRange = missionRange;
        sData.completed = false;
        sData.players.addAll(players);

        data.computeIfAbsent(levelId, k -> new ConcurrentHashMap<>())
                .put(task, sData);
        setDirty();
        return true;
    }

    public boolean createTreasure(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null) return false;
        Vec3 missionPos = sData.missionPosition;
        BlockPos surface = BlockPos.containing(missionPos);
        ChunkPos chunkPos = new ChunkPos(surface);
        level.getChunk(chunkPos.x, chunkPos.z);
        int y = level.dimension() == Level.NETHER
                ? level.getHeight(Heightmap.Types.MOTION_BLOCKING, surface.getX(), surface.getZ())//nether problem
                : level.getHeight(Heightmap.Types.WORLD_SURFACE, surface.getX(), surface.getZ());

        BlockPos chestPos = new BlockPos(surface.getX(), y, surface.getZ());
        if (!level.getWorldBorder().isWithinBounds(chestPos)) return false;

        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            ResourceKey<LootTable> key;
            switch (sData.missionType) {
                case EXTERMINATE -> key = LootTableRegistry.MISSION_EXTERMINATE_TREASURE_KEY;
                case DEFENSE -> key = LootTableRegistry.MISSION_DEFENSE_TREASURE_KEY;
                case SURVIVAL -> key = LootTableRegistry.MISSION_SURVIVAL_TREASURE_KEY;
                case ASSASSINATION -> key = LootTableRegistry.MISSION_ASSASSINATION_TREASURE_KEY;
                default -> key = LootTableRegistry.MISSION_EXTERMINATE_TREASURE_KEY;
            }
            long seed = level.getRandom().nextLong();
            chest.setLootTable(key, seed);

            chest.getPersistentData().putString("TaskId", taskId.toString());
        }

        for (UUID uuid : sData.players) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.sendSystemMessage(Component.translatable("ui.yagens_attributes.mission_treasure_created")
                        .append(Component.literal("["+ chestPos +"]").withStyle(ChatFormatting.GOLD)));
            }
        }

        return true;
    }

    public boolean anyPlayerInActiveTask(ResourceLocation level, Collection<UUID> players) {
        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return false;

        for (SharedTaskData sData : taskMap.values()) {
            if (sData.completed) continue;
            for (UUID uuid : players) {
                if (sData.players.contains(uuid)) return true;
            }
        }
        return false;
    }

    public void setMissionType(ResourceLocation level, ResourceLocation task, MissionType missionType) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.missionType = missionType;
            setDirty();
        }
    }

    public void setProgress(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId, int progress) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData != null) {
            sData.progress = progress;
            checkCompleted(level, levelId, taskId, sData);
            setDirty();
        }
    }

    public void addProgress(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData == null || sData.completed) return;
        sData.progress++;
        checkCompleted(level, levelId, taskId, sData);
        setDirty();
    }

    public void addSummonCount(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        if (sData == null || sData.completed) return;
        sData.summonCount++;
        setDirty();
    }

    public boolean checkCompleted(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId, SharedTaskData sData) {
        if (sData.progress >= sData.maxProgress|| sData.completed) {
            sData.completed = true;
            createTreasure(level, levelId, taskId);
            return true;
        }
        return false;
    }

//    public double distanceToMissionPosition(SharedTaskData sData) {
//        Vec3 missionPos = sData.missionPosition;
//
//    }

    public static double distanceToMissionPosition(Player player, SharedTaskData sData) {
//        return player.distanceToSqr(sData.missionPosition);
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

    public void setMaxProgress(ResourceLocation level, ResourceLocation task, int maxProgress) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.maxProgress = maxProgress;
            setDirty();
        }
    }

    public void setSummonCount(ResourceLocation level, ResourceLocation task, int summonCount) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.summonCount = summonCount;
            setDirty();
        }
    }

    public void setDistance(ResourceLocation level, ResourceLocation task, double distance) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.distance = distance;
            setDirty();
        }
    }

    public void setMissionRange(ResourceLocation level, ResourceLocation task, double missionRange) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.missionRange = missionRange;
            setDirty();
        }
    }

    public void setMissionPosition(ResourceLocation level, ResourceLocation task, Vec3 missionPosition) {
        SharedTaskData sData = getData(level, task);
        if (sData != null) {
            sData.missionPosition = missionPosition;
            setDirty();
        }
    }

    public void setCompleted(ServerLevel level, ResourceLocation levelId, ResourceLocation taskId, boolean completed) {
        SharedTaskData sData = getData(levelId, taskId);
        if (sData != null) {
            sData.completed = completed;
            checkCompleted(level, levelId, taskId, sData);
            setDirty();
        }
    }

    public boolean isCompleted(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData != null && sData.completed;
    }

    public boolean isPlayerInTask(ResourceLocation level, ResourceLocation task, Player player) {
        SharedTaskData sData = getData(level, task);
        return sData != null && sData.players.contains(player.getUUID());
    }

    public MissionType getMissionType(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData.missionType;
    }

    public int getProgress(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData == null ? 0 : sData.progress;
    }

    public int getMaxProgress(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData == null ? 0 : sData.maxProgress;
    }

    public int getSummonCount(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData == null ? 0 : sData.summonCount;
    }

    public double getDistance(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData == null ? 0 : sData.distance;
    }

    public double getMissionRange(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData == null ? 0 : sData.missionRange;
    }

    public Vec3 getMissionPosition(ResourceLocation level, ResourceLocation task) {
        SharedTaskData sData = getData(level, task);
        return sData.missionPosition;
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

    @Nullable
    public ResourceLocation getPlayerActiveTaskId(Player player) {
        ResourceLocation level = player.level().dimension().location();

        Map<ResourceLocation, SharedTaskData> taskMap = data.get(level);
        if (taskMap == null) return null;

        for (Map.Entry<ResourceLocation, SharedTaskData> e : taskMap.entrySet()) {
            SharedTaskData sData = e.getValue();
            if (!sData.completed && sData.players.contains(player.getUUID())) {
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
            if (!sData.completed && sData.players.contains(player.getUUID())) {
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
            if (!sData.completed && sData.players.contains(player.getUUID())) {
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
                taskTag.putInt("progress", sData.progress);
                taskTag.putInt("maxProgress", sData.maxProgress);
                taskTag.putInt("summonCount", sData.summonCount);
                taskTag.putDouble("distance", sData.distance);
                taskTag.putDouble("missionRange", sData.missionRange);
                taskTag.putDouble("missionX", sData.missionPosition.x);
                taskTag.putDouble("missionY", sData.missionPosition.y);
                taskTag.putDouble("missionZ", sData.missionPosition.z);
                taskTag.putBoolean("completed", sData.completed);
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
                sData.progress = taskTag.getInt("progress");
                sData.maxProgress = taskTag.getInt("maxProgress");
                sData.summonCount = taskTag.getInt("summonCount");
                sData.distance = taskTag.getDouble("distance");
                sData.missionRange = taskTag.getDouble("missionRange");
                sData.missionPosition = new Vec3(
                        taskTag.getDouble("missionX"),
                        taskTag.getDouble("missionY"),
                        taskTag.getDouble("missionZ"));
                sData.completed = taskTag.getBoolean("completed");
                ListTag playerList = taskTag.getList("players", Tag.TAG_INT_ARRAY);
                playerList.forEach(tag -> sData.players.add(NbtUtils.loadUUID(tag)));
                inst.data.computeIfAbsent(level, k -> new ConcurrentHashMap<>())
                        .put(task, sData);
            });
        });
        return inst;
    }


    public static MissionData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(MissionData::new, MissionData::load),
                DATA_NAME);
    }

    public void clearAll() {
        data.clear();
        setDirty();
    }

    public void clearUnfinishedOnly() {
        data.values().forEach(taskMap ->
                taskMap.entrySet().removeIf(e -> !e.getValue().completed)
        );
        data.entrySet().removeIf(e -> e.getValue().isEmpty());
        setDirty();
    }

    public static class SharedTaskData {
        public MissionType missionType;
        public int progress;
        public int maxProgress;
        public int summonCount;
        public double distance;
        public double missionRange;
        public Vec3 missionPosition;
        public boolean completed;
        public Set<UUID> players = new HashSet<>();
    }
}