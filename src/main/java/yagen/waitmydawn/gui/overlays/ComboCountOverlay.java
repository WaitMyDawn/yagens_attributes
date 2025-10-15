package yagen.waitmydawn.gui.overlays;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;

import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public class ComboCountOverlay implements LayeredDraw.Layer {
    public static final ComboCountOverlay instance = new ComboCountOverlay();

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        var player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        if (!shouldShowComboCount(player))
            return;

        var font = Minecraft.getInstance().font;
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
        DataAttachmentRegistry.Combo combo = player.getData(DataAttachmentRegistry.COMBO.get());
        int comboLevel = combo.getComboLevel();

        String leftInfo = comboLevel + "/";
        String rightInfo = combo.count() + "  " + combo.leftDuration();

        int color;
        if (comboLevel < 2)
            color = ChatFormatting.GRAY.getColor();
        else if (comboLevel >= 2 && comboLevel < 5)
            color = ChatFormatting.GREEN.getColor();
        else if (comboLevel >= 5 && comboLevel < 9)
            color = ChatFormatting.AQUA.getColor();
        else if (comboLevel >= 9 && comboLevel < 14)
            color = ChatFormatting.DARK_PURPLE.getColor();
        else
            color = ChatFormatting.GOLD.getColor();

        int barX = ClientConfigs.COMBO_HUD_X.get() == -1 ? screenWidth / 2 : ClientConfigs.COMBO_HUD_X.get();
        int barY = ClientConfigs.COMBO_HUD_Y.get() == -1 ? screenHeight / 2 : ClientConfigs.COMBO_HUD_Y.get();

        guiHelper.pose().pushPose();
        float scale = 2f;
        guiHelper.pose().scale(scale, scale, 1f);
        guiHelper.drawString(font, leftInfo,
                (int) ((barX) / scale),
                (int) ((barY) / scale),
                color, true);
        guiHelper.pose().popPose();

        int leftWidth = (int) (font.width(leftInfo) * scale);
        guiHelper.drawString(font, rightInfo, barX + leftWidth, barY + 2, color, true);
    }

    public static boolean shouldShowComboCount(Player player) {
        return player.getData(DataAttachmentRegistry.COMBO.get()).count() != 0;
    }
}
