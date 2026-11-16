package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.yagens_attributes.core.ui.*;

public final class PanelButton extends AbstractButton {
    private final Runnable action;
    private boolean selected;
    private boolean centered;
    public PanelButton(int x, int y, int width, int height, Component label, Runnable action) { super(x, y, width, height, label); this.action = action; }
    public void selected(boolean value) { selected = value; }
    public PanelButton centered() { centered = true; return this; }
    @Override public void onPress() { action.run(); }
    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var canvas = new PanelCanvas(graphics, Minecraft.getInstance().font);
        int color = !active ? 0xFF35434B : selected ? PanelPainter.GOLD : isHoveredOrFocused() ? PanelPainter.TEAL : 0xFF607883;
        PanelPainter.frame(canvas, new PanelGeometry.Rect(getX(), getY(), width, height), selected ? 0xD02E3430 : 0xD0111D26, color, 4);
        if (centered) canvas.centeredText(getMessage().getString(), getX() + width / 2, getY() + (height - 10) / 2, 10, !active ? 0xFF5C6C74 : selected ? PanelPainter.GOLD : PanelPainter.TEXT, width - 14);
        else canvas.text(getMessage().getString(), getX() + 7, getY() + (height - 10) / 2, 10, !active ? 0xFF5C6C74 : selected ? PanelPainter.GOLD : PanelPainter.TEXT, width - 14);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
