package yagen.waitmydawn.gui.overlays;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.config.ClientConfigs;

import java.util.Objects;

public class MissionOverlay implements LayeredDraw.Layer {
    public static final MissionOverlay instance = new MissionOverlay();

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        var player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        var task = MissionData.get(Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer()))
                .getPlayerActiveTask(player);
        if (task == null)
            return;

        MissionData.SharedTaskData taskData = task.getValue();
        ResourceLocation taskId = task.getKey();

        var font = Minecraft.getInstance().font;
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();

        int color = ChatFormatting.GOLD.getColor();

        int barX = ClientConfigs.MISSION_HUD_X.get() == -1 ? screenWidth / 2 : ClientConfigs.MISSION_HUD_X.get();
        int barY = ClientConfigs.MISSION_HUD_Y.get() == -1 ? screenHeight / 2 : ClientConfigs.MISSION_HUD_Y.get();
        Component text = Component.literal(
                        String.format("[%s] %d / %d  Position：x = %.0f , y = %.0f , z = %.0f",
                                taskData.missionType,
                                taskData.progress,
                                taskData.maxProgress,
                                taskData.missionPosition.x, taskData.missionPosition.y, taskData.missionPosition.z))
                .append(Component.literal(
                        String.format("Distance = %.0f", Math.sqrt(player.distanceToSqr(taskData.missionPosition)))));
        guiHelper.drawString(font, text, barX, barY, color, true);
    }
}
