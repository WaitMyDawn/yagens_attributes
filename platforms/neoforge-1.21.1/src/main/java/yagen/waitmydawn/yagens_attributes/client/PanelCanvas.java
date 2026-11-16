package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.yagens_attributes.core.ui.PanelPainter;

public record PanelCanvas(GuiGraphics graphics, Font font) implements PanelPainter.Canvas {
    @Override public void fill(int x, int y, int width, int height, int color) { graphics.fill(x, y, x + width, y + height, color); }
    @Override public void gradient(int x, int y, int width, int height, int top, int bottom) { graphics.fillGradient(x, y, x + width, y + height, top, bottom); }
    @Override public void text(String value, int x, int y, float size, int color, int maximumWidth) {
        float scale = size / 9F;
        graphics.pose().pushPose(); graphics.pose().translate(x, y, 0); graphics.pose().scale(scale, scale, 1);
        int available = Math.max(1, (int) (maximumWidth / scale));
        String shown = font.width(value) <= available ? value : font.plainSubstrByWidth(value, Math.max(1, available - font.width("…"))) + "…";
        graphics.drawString(font, shown, 0, 0, color, false);
        graphics.pose().popPose();
    }
    @Override public void centeredText(String value, int centerX, int y, float size, int color, int maximumWidth) {
        text(value, centerX - Math.round(Math.min(maximumWidth, font.width(value) * size / 9F) / 2F), y, size, color, maximumWidth);
    }
    public void component(Component value, int x, int y, float size, int color, int maximumWidth) {
        component(value, x, y, size, color, maximumWidth, false);
    }
    public void centeredComponent(Component value, int centerX, int y, float size, int color, int maximumWidth) {
        component(value, centerX, y, size, color, maximumWidth, true);
    }
    private void component(Component value, int x, int y, float size, int color, int maximumWidth, boolean centered) {
        float scale = size / 9F;
        graphics.pose().pushPose(); graphics.pose().translate(x, y, 0); graphics.pose().scale(scale, scale, 1);
        var lines = font.split(value, Math.max(1, (int) (maximumWidth / scale)));
        if (!lines.isEmpty()) graphics.drawString(font, lines.get(0), centered ? -font.width(lines.get(0)) / 2 : 0, 0, color, false);
        graphics.pose().popPose();
    }
}
