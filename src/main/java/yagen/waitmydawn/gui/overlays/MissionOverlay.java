package yagen.waitmydawn.gui.overlays;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.config.ClientConfigs;

import java.util.Objects;

import static yagen.waitmydawn.api.mission.MissionData.distanceToMissionPosition;

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

        int barX = ClientConfigs.MISSION_HUD_X.get() == -1 ? screenWidth / 5 : ClientConfigs.MISSION_HUD_X.get();
        int barY = ClientConfigs.MISSION_HUD_Y.get() == -1 ? screenHeight / 5 : ClientConfigs.MISSION_HUD_Y.get();
        Component text = Component.literal(
                String.format("[%s] %d / %d  Position：x = %.0f , y = %.0f , z = %.0f",
                        taskData.missionType,
                        taskData.progress,
                        taskData.maxProgress,
                        taskData.missionPosition.x, taskData.missionPosition.y, taskData.missionPosition.z));
        Component distance = Component.literal(
                String.format("SummonCount = %d  Distance = %.0f", taskData.summonCount, distanceToMissionPosition(player, taskData)));
        guiHelper.drawString(font, text, barX, barY, color, true);
        barY += font.lineHeight;
        guiHelper.drawString(font, distance, barX, barY, color, true);
        barY += font.lineHeight;
        drawRing(guiHelper, barX, barY, (float) taskData.progress / taskData.maxProgress);
    }

    private static final ResourceLocation RING_BG = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/ring_background.png");
    private static final ResourceLocation RING_FG = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/ring_foreground.png");

    /**
     * draw 32x ring at (centerX,centerY)
     *
     * @param percent 0.0 ~ 1.0
     */
    private static void drawRing(GuiGraphics gui, int X, int Y, float percent) {
        int ringSize = 32;
        int outerR = 16;
        int innerR = 12;

        gui.blit(RING_BG,
                X, Y,
                0, 0, ringSize, ringSize,
                ringSize, ringSize);
    }
}
