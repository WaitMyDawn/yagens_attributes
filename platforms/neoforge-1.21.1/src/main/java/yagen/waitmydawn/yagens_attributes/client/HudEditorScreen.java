package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.ui.HudLayout;

public final class HudEditorScreen extends Screen {
    private final HudElement element;
    private HudElement.Settings draft;
    private boolean moving;
    private double grabX, grabY;
    private Component error;
    private Button visibility;
    private Button countdown;

    public HudEditorScreen(HudElement element) {
        super(text("hud_editor", element.title()));
        this.element = element; this.draft = element.settings();
    }
    private static Component text(String key, Object... args) { return Component.translatable("gui.yagens_attributes." + key, args); }
    public boolean edits(HudElement target) { return element == target; }
    private HudLayout.Placement placement() { return element.place(width, height, draft); }
    @Override protected void init() {
        moving = false;
        int left = (width - 298) / 2, y = height - 28;
        visibility = addRenderableWidget(Button.builder(visibilityLabel(), button -> {
            draft = new HudElement.Settings(draft.offsetX(), draft.offsetY(), draft.scale(), !draft.visible(), draft.countdown());
            visibility.setMessage(visibilityLabel()); error = null;
        }).bounds(left, y, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
            draft = element.defaults(); moving = false; error = null; visibility.setMessage(visibilityLabel());
            if (countdown != null) countdown.setMessage(countdownLabel());
        }).bounds(left + 76, y, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose()).bounds(left + 152, y, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> save()).bounds(left + 228, y, 70, 20).build());
        if (element == HudElement.BUFFS) countdown = addRenderableWidget(Button.builder(countdownLabel(), button -> {
            draft = new HudElement.Settings(draft.offsetX(), draft.offsetY(), draft.scale(), draft.visible(), !draft.countdown());
            countdown.setMessage(countdownLabel()); error = null;
        }).bounds(width / 2 - 58, 48, 116, 20).build());
    }
    private Component countdownLabel() { return text("hud_countdown", Component.translatable(draft.countdown() ? "options.on" : "options.off")); }
    private Component visibilityLabel() { return text("hud_visibility", Component.translatable(draft.visible() ? "options.on" : "options.off")); }
    private void move(double left, double top) {
        var offsets = placement().move(width, height, left, top);
        draft = new HudElement.Settings(offsets.x(), offsets.y(), draft.scale(), draft.visible(), draft.countdown()); error = null;
    }
    private void save() {
        try { element.save(draft); minecraft.setScreen(null); }
        catch (RuntimeException exception) {
            YagensAttributes.LOGGER.error("Could not save HUD layout", exception); error = text("hud_save_failed");
        }
    }
    @Override public void onClose() { moving = false; minecraft.setScreen(null); }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var box = placement();
        graphics.fill(0, 0, width, element == HudElement.BUFFS ? 76 : 64, 0xB0101B23);
        graphics.fill(0, height - 40, width, height, 0xB0101B23);
        graphics.drawCenteredString(font, title, width / 2, 9, 0xFFE1CE90);
        graphics.drawWordWrap(font, text("hud_drag_hint"), 12, 26, Math.max(1, width - 24), 0xFFCBD6DA);
        if (moving) {
            graphics.fill(box.x(), 64, box.x() + 1, height - 40, 0x605DB4C5);
            graphics.fill(0, box.y(), width, box.y() + 1, 0x605DB4C5);
        }
        graphics.fill(box.x(), box.y(), box.x() + box.width(), box.y() + box.height(), 0x5013212A);
        graphics.renderOutline(box.x(), box.y(), box.width(), box.height(), moving || box.contains(mouseX, mouseY) ? 0xFFE1CE90 : 0xFF70BECC);
        graphics.pose().pushPose(); graphics.pose().translate(box.x(), box.y(), 0); graphics.pose().scale(box.scale(), box.scale(), 1);
        element.preview(graphics, draft); graphics.pose().popPose();
        if (error != null) graphics.drawCenteredString(font, error, width / 2, height - 52, 0xFFFF7777);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        var box = placement();
        if (button == 0 && box.contains(mouseX, mouseY)) {
            moving = true; grabX = mouseX - box.x(); grabY = mouseY - box.y(); setFocused(null); return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (moving && button == 0) { move(mouseX - grabX, mouseY - grabY); return true; }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (moving && button == 0) { move(mouseX - grabX, mouseY - grabY); moving = false; return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var box = placement();
        if (box.contains(mouseX, mouseY) && scrollY != 0) {
            double scale = Math.max(HudLayout.MIN_SCALE, Math.min(HudLayout.MAX_SCALE, Math.round((draft.scale() + Math.signum(scrollY) * .1) * 10) / 10D));
            draft = new HudElement.Settings(draft.offsetX(), draft.offsetY(), scale, draft.visible(), draft.countdown());
            var resized = placement();
            move(mouseX - (mouseX - box.x()) * resized.width() / box.width(), mouseY - (mouseY - box.y()) * resized.height() / box.height());
            if (moving) { var moved = placement(); grabX = mouseX - moved.x(); grabY = mouseY - moved.y(); }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int step = hasShiftDown() ? 10 : 1;
        var box = placement();
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> move(box.x() - step, box.y());
            case GLFW.GLFW_KEY_RIGHT -> move(box.x() + step, box.y());
            case GLFW.GLFW_KEY_UP -> move(box.x(), box.y() - step);
            case GLFW.GLFW_KEY_DOWN -> move(box.x(), box.y() + step);
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (getFocused() != null) return super.keyPressed(keyCode, scanCode, modifiers);
                save();
            }
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        return true;
    }
}
