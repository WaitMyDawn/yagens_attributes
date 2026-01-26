package yagen.waitmydawn.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.config.ClientConfigs;

public class ComboPositionScreen extends Screen {

    private int dragStartX, dragStartY;
    private boolean dragging = false;
    private int tempX = -1;
    private int tempY = -1;

    private final Screen parent;

    public ComboPositionScreen(Screen parent) {
        super(Component.translatable("ui.yagens_attributes.combo_position_title"));
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);

        int x, y;
        if (tempX == -1) {
            x = ClientConfigs.COMBO_HUD_X.get() == -1 ? width / 2 + 70 : ClientConfigs.COMBO_HUD_X.get();
            y = ClientConfigs.COMBO_HUD_Y.get() == -1 ? height - 40 : ClientConfigs.COMBO_HUD_Y.get();
        } else {
            x = tempX;
            y = tempY;
        }
        gfx.drawString(font, "3x/32  120", x, y, 0xFFFFFF, true);
        gfx.drawString(font, Component.translatable("ui.yagens_attributes.combo_position_explain.1"),
                (width - font.width(Component.translatable("ui.yagens_attributes.combo_position_explain.1"))) / 2,
                20, 0xFFFF55, true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int x, y;
            if (tempX == -1) {
                x = ClientConfigs.COMBO_HUD_X.get() == -1 ? width / 2 + 70 : ClientConfigs.COMBO_HUD_X.get();
                y = ClientConfigs.COMBO_HUD_Y.get() == -1 ? height - 40 : ClientConfigs.COMBO_HUD_Y.get();
            } else {
                x = tempX;
                y = tempY;
            }
            if (mx >= x && mx <= x + 100 && my >= y && my <= y + 10) {
                dragging = true;
                dragStartX = (int) mx - x;
                dragStartY = (int) my - y;
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn == 0 && dragging) {
            dragging = false;
            tempX = (int) mx - dragStartX;
            tempY = (int) my - dragStartY;
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging) {
            tempX = (int) mx - dragStartX;
            tempY = (int) my - dragStartY;
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (tempX != -1) {
                ClientConfigs.COMBO_HUD_X.set(tempX);
                ClientConfigs.COMBO_HUD_Y.set(tempY);
                ClientConfigs.SPEC.save();
            }
            tempX = -1;
            tempY = -1;
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }
}