package yagen.waitmydawn.api.mission;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.network.SyncMissionDataPacket;

import javax.annotation.Nullable;
import java.util.Map;

public class ClientMissionData {
    private static MissionData.SharedTaskData snapshot;
    private static ResourceLocation snapshotTaskId;

    public static void update(SyncMissionDataPacket p) {
        snapshot = new MissionData.SharedTaskData();
        snapshot.missionType = MissionType.fromString(p.missionType());
        snapshot.missionLevel = p.missionLevel();
        snapshot.progress = p.progress();
        snapshot.maxProgress = p.maxProgress();
        snapshot.summonCount = p.summonCount();
        snapshot.distance = p.distance();
        snapshot.missionRange = p.missionRange();
        snapshot.missionPosition = new Vec3(p.x(), p.y(), p.z());
        snapshot.completed = p.completed();
        snapshot.players.add(Minecraft.getInstance().player.getUUID());
        snapshotTaskId = p.taskId();
    }

    @Nullable
    public static Map.Entry<ResourceLocation, MissionData.SharedTaskData>
    getPlayerActiveTask(LocalPlayer player) {
        if (snapshot == null || snapshot.completed) return null;
        return Map.entry(snapshotTaskId, snapshot);
    }
}
