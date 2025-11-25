package yagen.waitmydawn.gui.overlays;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.config.ClientConfigs;

import java.util.Objects;

import static yagen.waitmydawn.api.mission.MissionData.distanceToMissionPosition;

public class MissionOverlay implements LayeredDraw.Layer {
    public static final MissionOverlay instance = new MissionOverlay();

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;

        if (minecraft.options.hideGui || player.isSpectator()) {
            return;
        }
        MissionData missionData;
        if (minecraft.getSingleplayerServer() != null) {
            missionData = MissionData.get(minecraft.getSingleplayerServer());
        } else {
            missionData = MissionData.get(null);
        }

        var task = missionData.getPlayerActiveTask(minecraft.player);
        if (task == null)
            return;

        MissionData.SharedTaskData taskData = task.getValue();
        ResourceLocation taskId = task.getKey();

        var font = minecraft.font;
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();

        int color = ChatFormatting.WHITE.getColor();

        int barX = ClientConfigs.MISSION_HUD_X.get() == -1 ? 0 : ClientConfigs.MISSION_HUD_X.get();
        int barY = ClientConfigs.MISSION_HUD_Y.get() == -1 ? screenHeight / 4 : ClientConfigs.MISSION_HUD_Y.get();
        Component type = Component.literal(
                String.format("%s",
                        taskData.missionType));
        Component position = Component.literal(
                String.format("[%.0f,%.0f,%.0f]",
                        taskData.missionPosition.x, taskData.missionPosition.y, taskData.missionPosition.z));
        Component progressLeft = Component.literal(
                String.format("%d", taskData.maxProgress - taskData.progress));
        Component curMax = Component.translatable(
                "overlay.yagens_attributes.ring_cur_max", taskData.progress, taskData.maxProgress);
        Component summonCount = Component.translatable(
                "overlay.yagens_attributes.ring_summon_count", taskData.summonCount);
        Component distance = Component.translatable(
                "overlay.yagens_attributes.ring_distance",  String.format("%.0f",distanceToMissionPosition(player, taskData)));
        int typeWidth = font.width(type);
        int positionWidth = font.width(position);
        int curMaxWidth = font.width(curMax);
        int summonCountWidth = font.width(summonCount);
        int distanceWidth = font.width(distance);
        int maxWidth = Math.max(32, typeWidth);
        if(maxWidth<positionWidth) maxWidth = positionWidth;
        if (maxWidth < curMaxWidth) maxWidth = curMaxWidth;
        if (maxWidth < summonCountWidth) maxWidth = summonCountWidth;
        if (maxWidth < distanceWidth) maxWidth = distanceWidth;

        guiHelper.drawString(font, type, barX + (maxWidth - typeWidth) / 2, barY, color, true);
        barY += font.lineHeight;
        guiHelper.drawString(font, position, barX + (maxWidth - positionWidth) / 2, barY, color, true);
        barY += font.lineHeight;
        drawRing(guiHelper, barX + (maxWidth - 32) / 2, barY, Math.min(36, taskData.progress * 36 / taskData.maxProgress));
        barY = barY + (32 - font.lineHeight) / 2;
        guiHelper.drawString(font, progressLeft, barX+ (maxWidth - 32) / 2 + (32 - font.width(progressLeft)) / 2, barY, color, true);
        barY += font.lineHeight;
        barY = barY + (32 - font.lineHeight) / 2;
        guiHelper.drawString(font, curMax, barX + (maxWidth - curMaxWidth) / 2, barY, color, true);
        barY += font.lineHeight;
        guiHelper.drawString(font, distance, barX + (maxWidth - distanceWidth) / 2, barY, color, true);
        barY += font.lineHeight;
        guiHelper.drawString(font, summonCount, barX + (maxWidth - summonCountWidth) / 2, barY, color, true);
    }

    private static final ResourceLocation RING = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/ring.png");

    /**
     * draw 32x ring at (centerX,centerY)
     *
     * @param part 0 ~ 36
     */
    private static void drawRing(GuiGraphics gui, int X, int Y, int part) {
        int TEXTURE_WIDTH = 192;
        int TEXTURE_HEIGHT = 224;
        part--;
        int partX;
        int partY;
        if (part == -1) {
            partX = 0;
            partY = 6;
        } else {
            partX = part % 6;
            partY = part / 6;
        }

        gui.blit(RING,
                X, Y,
                partX * 32, partY * 32,
                32, 32,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
