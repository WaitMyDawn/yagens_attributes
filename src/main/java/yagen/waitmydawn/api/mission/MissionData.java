package yagen.waitmydawn.api.mission;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionData extends SavedData {
    private static final String DATA_NAME = YagensAttributes.MODID + "_mission";

    private final Map<ResourceLocation, Map<ResourceLocation, SharedTaskData>> data =
            new ConcurrentHashMap<>();

    public void createSharedTask(ResourceLocation level, ResourceLocation task,
                                 MissionType type, Vec3 pos, int maxProgress,double distance,double missionRange) {
        SharedTaskData sData = new SharedTaskData();
        sData.missionType = type;
        sData.missionPosition = pos;
        sData.maxProgress = maxProgress;
        sData.progress = 0;
        sData.distance = distance;
        sData.missionRange = missionRange;
        sData.completed = false;
        data.computeIfAbsent(level, k -> new ConcurrentHashMap<>())
                .put(task, sData);
        setDirty();
    }

    public void setMissionType(ResourceLocation level, ResourceLocation task, MissionType missionType) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.missionType = missionType;
            setDirty();
        }
    }

    public void setProgress(ResourceLocation level, ResourceLocation task, int progress) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.progress = progress;
            setDirty();
        }
    }

    public void setMaxProgress(ResourceLocation level, ResourceLocation task, int maxProgress) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.maxProgress = maxProgress;
            setDirty();
        }
    }

    public void setDistance(ResourceLocation level, ResourceLocation task, double distance) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.distance = distance;
            setDirty();
        }
    }

    public void setMissionRange(ResourceLocation level, ResourceLocation task, double missionRange) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.missionRange = missionRange;
            setDirty();
        }
    }

    public void setMissionPosition(ResourceLocation level, ResourceLocation task, Vec3 missionPosition) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.missionPosition = missionPosition;
            setDirty();
        }
    }

    public void setCompleted(ResourceLocation level, ResourceLocation task, boolean completed) {
        SharedTaskData data = getData(level, task);
        if (data != null) {
            data.completed = completed;
            setDirty();
        }
    }

    public boolean isCompleted(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data != null && data.completed;
    }

    public MissionType getMissionType(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data.missionType;
    }

    public int getProgress(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data == null ? 0 : data.progress;
    }

    public int getMaxProgress(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data == null ? 0 : data.maxProgress;
    }

    public double getDistance(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data == null ? 0 : data.distance;
    }

    public double getMissionRange(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data == null ? 0 : data.missionRange;
    }

    public Vec3 getMissionPosition(ResourceLocation level, ResourceLocation task) {
        SharedTaskData data = getData(level, task);
        return data.missionPosition;
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

    @Override
    public CompoundTag save(@NotNull CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        data.forEach((level, taskMap) -> {
            CompoundTag levelTag = new CompoundTag();
            taskMap.forEach((task, sData) -> {
                CompoundTag taskTag = new CompoundTag();
                taskTag.putString("missionType", sData.missionType.getValue());
                taskTag.putInt("progress", sData.progress);
                taskTag.putInt("maxProgress", sData.maxProgress);
                taskTag.putDouble("distance", sData.distance);
                taskTag.putDouble("missionRange", sData.missionRange);
                taskTag.putDouble("missionX", sData.missionPosition.x);
                taskTag.putDouble("missionY", sData.missionPosition.y);
                taskTag.putDouble("missionZ", sData.missionPosition.z);
                taskTag.putBoolean("completed", sData.completed);
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
                sData.distance = taskTag.getDouble("distance");
                sData.missionRange = taskTag.getDouble("missionRange");
                sData.missionPosition = new Vec3(
                        taskTag.getDouble("missionX"),
                        taskTag.getDouble("missionY"),
                        taskTag.getDouble("missionZ"));
                sData.completed = taskTag.getBoolean("completed");
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

    private static class SharedTaskData {
        MissionType missionType;
        int progress;
        int maxProgress;
        double distance;
        double missionRange;
        Vec3 missionPosition;
        boolean completed;
    }
}