package yagen.waitmydawn.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.ClientConfigs;

public class MissionPositionScreen extends Screen {

    private GuiGraphics gfx;
    private int dragStartX, dragStartY;
    private boolean dragging = false;
    private int tempX = -1;
    private int tempY = -1;

    private final Screen parent;

    public MissionPositionScreen(Screen parent) {
        super(Component.translatable("ui.yagens_attributes.mission_position_title"));
        this.parent = parent;
    }

    private static final ResourceLocation AREA = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/mission_info_area.png");

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.gfx = gfx;
        int x, y;
        if (tempX == -1) {
            x = ClientConfigs.MISSION_HUD_X.get() == -1 ? 0 : ClientConfigs.MISSION_HUD_X.get();
            y = ClientConfigs.MISSION_HUD_Y.get() == -1 ? height / 4 : ClientConfigs.MISSION_HUD_Y.get();
        } else {
            x = tempX;
            y = tempY;
        }

        gfx.blit(AREA,
                x, y,
                0, 0,
                96, 112,
                96, 112);
        gfx.drawString(font, Component.translatable("ui.yagens_attributes.mission_position_explain.1"),
                (width - font.width(Component.translatable("ui.yagens_attributes.mission_position_explain.1"))) / 2,
                20, 0xFFFF55, true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int x, y;
            if (tempX == -1) {
                x = ClientConfigs.MISSION_HUD_X.get() == -1 ? 0 : ClientConfigs.MISSION_HUD_X.get();
                y = ClientConfigs.MISSION_HUD_Y.get() == -1 ? height / 4 : ClientConfigs.MISSION_HUD_Y.get();
            } else {
                x = tempX;
                y = tempY;
            }
            if (mx >= x && mx <= x + 96 && my >= y && my <= y + 112) {
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
            tempX=(int) mx - dragStartX;
            tempY=(int) my - dragStartY;
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging) {
            tempX=(int) mx - dragStartX;
            tempY=(int) my - dragStartY;
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            ClientConfigs.MISSION_HUD_X.set(tempX);
            ClientConfigs.MISSION_HUD_Y.set(tempY);
            ClientConfigs.SPEC.save();
            tempX = -1;
            tempY = -1;
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }
}