package yagen.waitmydawn.gui.overlays;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public class BatteryOverlay implements LayeredDraw.Layer {
    public static final BatteryOverlay instance = new BatteryOverlay();
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/battery.png");
    private static final int WIDTH = 44;
    private static final int HEIGHT = 72;

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        if (!shouldShowBattery(player))
            return;

        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
        Font font = Minecraft.getInstance().font;

        int barX = ClientConfigs.BATTERY_X.get() == -1 ? (screenWidth - 24) : ClientConfigs.BATTERY_X.get();
        int barY = ClientConfigs.BATTERY_Y.get() == -1 ? (screenHeight - 74) : ClientConfigs.BATTERY_Y.get();

        double batteryPower = DataAttachmentRegistry.getBatteryPower(player);
        int curHeight = (int) (batteryPower / 100.0 * HEIGHT);
        Component text = Component.literal(String.format("%3d%%", (int) batteryPower));

        guiHelper.blit(TEXTURE, barX, barY, 0, 0, WIDTH / 2, HEIGHT - curHeight, WIDTH, HEIGHT);
        guiHelper.blit(TEXTURE, barX, barY + HEIGHT - curHeight, (float) WIDTH / 2, HEIGHT - curHeight, WIDTH / 2, curHeight, WIDTH, HEIGHT);
//        guiHelper.drawString(font, text, barX - font.width(text) / 2, barY + 26, 0xFFFFFF);
        guiHelper.pose().pushPose();
        guiHelper.pose().scale(0.5f, 0.5f, 1f);
        float scale = 0.5f;
        int scaledX = (int) ((barX - (float) font.width(text) / 2+6) / scale);
        int scaledY = (int) ((barY + 29) / scale);
        guiHelper.drawString(font, text, scaledX, scaledY, getBatteryColor(batteryPower));
        guiHelper.pose().popPose();
    }

    public static boolean shouldShowBattery(Player player) {
        return DataAttachmentRegistry.getBatteryPower(player) > 0;
    }

    private static int getBatteryColor(double batteryPower) {
        int startR = 0x87;
        int startG = 0xCE;
        int startB = 0xEB;

        int endR = 0xFF;
        int endG = 0x45;
        int endB = 0x00;

        double t = Math.max(0, Math.min(1, batteryPower / 100.0));

        int r = (int) (startR + (endR - startR) * t);
        int g = (int) (startG + (endG - startG) * t);
        int b = (int) (startB + (endB - startB) * t);

        return (r << 16) | (g << 8) | b;
    }
}
