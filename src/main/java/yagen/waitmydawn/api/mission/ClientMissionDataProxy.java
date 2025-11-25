package yagen.waitmydawn.api.mission;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Map;

public class ClientMissionDataProxy extends MissionData {
    public static final ClientMissionDataProxy INSTANCE = new ClientMissionDataProxy();

    public ClientMissionDataProxy() {}

    @Override
    @Nullable
    public Map.Entry<ResourceLocation, MissionData.SharedTaskData>
    getPlayerActiveTask(Player player) {
        return ClientMissionData.getPlayerActiveTask((LocalPlayer) player);
    }

}
