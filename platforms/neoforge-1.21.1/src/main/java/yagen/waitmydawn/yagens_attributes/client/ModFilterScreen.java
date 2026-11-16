package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.network.ClientPackets;
import java.util.*;

public final class ModFilterScreen extends Screen {
    private final ModPanelScreen parent;
    private final ModFilter original;
    private final ModFilterEditor editor;
    private int scroll, selectedGroup, selectedIndex, bankIndex, noticeTicks;
    private boolean bankFocused = true, dragging;
    private ModFilterLayout.Chip pressed;
    private double pressX, pressY, pointerX, pointerY;
    private String notice = "";

    public ModFilterScreen(ModPanelScreen parent, ModFilter filter) {
        super(text("filter_title")); this.parent = Objects.requireNonNull(parent); original = filter;
        editor = new ModFilterEditor(filter); selectedGroup = filter.groups().isEmpty() ? -1 : 0;
    }
    private static Component text(String key, Object... args) { return Component.translatable("gui.yagens_attributes." + key, args); }
    private double scale() { return Math.min(width / (double) PanelGeometry.WIDTH, height / (double) PanelGeometry.HEIGHT); }
    private double originX() { return (width - PanelGeometry.WIDTH * scale()) / 2; }
    private double originY() { return (height - PanelGeometry.HEIGHT * scale()) / 2; }
    private double logicalX(double x) { return (x - originX()) / scale(); }
    private double logicalY(double y) { return (y - originY()) / scale(); }
    private ModFilterLayout layout() { return new ModFilterLayout(editor.value(), scroll); }
    private Component tagName(ModFilter.Tag tag) {
        var rarity = tag.rarity(); var quality = rarity == null ? null : ClientPackets.modCatalog().qualities().get(rarity);
        return Component.translatable(quality == null ? "filter.yagens_attributes." + tag.key() : quality.name());
    }
    private int tagColor(ModFilter.Tag tag) {
        var rarity = tag.rarity(); var quality = rarity == null ? null : ClientPackets.modCatalog().qualities().get(rarity);
        return quality == null ? PanelPainter.TEAL : 0xFF000000 | quality.color();
    }
    @Override protected void init() {
        pressed = null; dragging = false;
        addButton(ModFilterLayout.CLEAR, text("filter_clear"), this::clear);
        addButton(ModFilterLayout.CANCEL, Component.translatable("gui.cancel"), this::onClose);
        addButton(ModFilterLayout.APPLY, text("filter_apply"), () -> parent.closeFilters(editor.value()));
    }
    private void addButton(PanelGeometry.Rect bounds, Component label, Runnable action) {
        addRenderableWidget(new PanelButton(bounds.x(), bounds.y(), bounds.width(), bounds.height(), label, action).centered());
    }
    private void clear() { editor.clear(); scroll = 0; selectedGroup = -1; selectedIndex = 0; pressed = null; dragging = false; notice = ""; }
    private void failed() { notice = "filter_drop_rejected"; noticeTicks = 80; }
    private void normalizeSelection() {
        selectedGroup = Math.min(Math.max(0, selectedGroup), editor.value().groups().size() - 1);
        selectedIndex = selectedGroup < 0 ? 0 : Math.max(0, Math.min(selectedIndex, editor.value().groups().get(selectedGroup).size() - 1));
        scroll = Math.max(0, Math.min(scroll, layout().maximumScroll));
    }
    private void revealSelection() {
        normalizeSelection(); if (selectedGroup < 0) return;
        for (var chip : layout().chips) if (chip.group() == selectedGroup && chip.index() == selectedIndex) {
            if (chip.bounds().y() < ModFilterLayout.ACTIVE.y() + 8) scroll -= ModFilterLayout.ACTIVE.y() + 8 - chip.bounds().y();
            if (chip.bounds().y() + 28 > ModFilterLayout.ACTIVE.y() + ModFilterLayout.ACTIVE.height() - 8) scroll += chip.bounds().y() + 28 - (ModFilterLayout.ACTIVE.y() + ModFilterLayout.ACTIVE.height() - 8);
            break;
        }
        normalizeSelection();
    }
    private void drop(ModFilterLayout.Chip source, ModFilterLayout.Drop target) {
        if (target == null) return;
        boolean empties = source.group() >= 0 && editor.value().groups().get(source.group()).size() == 1;
        int nextGroup = target.group() < 0 ? target.index() : target.group();
        if (empties && source.group() < nextGroup) nextGroup--;
        if (!editor.drop(source.tag(), source.group(), source.index(), target.group(), target.index())) { failed(); return; }
        notice = ""; selectedGroup = nextGroup; normalizeSelection();
        if (selectedGroup >= 0) selectedIndex = editor.value().groups().get(selectedGroup).indexOf(source.tag());
        revealSelection();
    }
    @Override public void renderBackground(GuiGraphics graphics, int mx, int my, float partialTick) {}
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int mx = (int) logicalX(mouseX), my = (int) logicalY(mouseY); pointerX = mx; pointerY = my;
        var layout = layout(); var split = dragging ? null : layout.link(mx, my); var target = dragging ? layout.drop(mx, my) : null;
        var state = new ModFilterPainter.State(selectedGroup, selectedIndex, bankFocused, bankIndex, split, target, dragging && pressed != null ? pressed.tag() : null, mx, my);
        graphics.fill(0, 0, width, height, 0xFF04090E);
        graphics.pose().pushPose(); graphics.pose().translate(originX(), originY(), 0); graphics.pose().scale((float) scale(), (float) scale(), 1);
        var canvas = new PanelCanvas(graphics, font);
        ModFilterPainter.backdrop(canvas, key -> text(key).getString());
        ModFilterPainter.palette(canvas, tag -> tagName(tag).getString(), this::tagColor, state);
        var active = ModFilterLayout.ACTIVE;
        graphics.enableScissor((int) (originX() + active.x() * scale()), (int) (originY() + active.y() * scale()),
                (int) Math.ceil(originX() + (active.x() + active.width()) * scale()), (int) Math.ceil(originY() + (active.y() + active.height()) * scale()));
        ModFilterPainter.groups(canvas, editor.value(), layout, tag -> tagName(tag).getString(), key -> text(key).getString(), this::tagColor, state);
        graphics.disableScissor();
        if (layout.maximumScroll > 0) {
            int h = Math.max(18, active.height() * active.height() / (active.height() + layout.maximumScroll));
            canvas.fill(active.x() + active.width() - 5, active.y() + (active.height() - h) * scroll / layout.maximumScroll, 3, h, PanelPainter.TEAL);
        }
        if (!notice.isEmpty()) canvas.text(text(notice).getString(), 190, 534, 9, 0xFFFF8A79, 580);
        for (var renderable : renderables) renderable.render(graphics, mx, my, partialTick);
        if (dragging && pressed != null) {
            ModFilterPainter.chip(canvas, new ModFilterLayout.Chip(-1, 0, pressed.tag(), new PanelGeometry.Rect(mx - 66, my - 14, 132, 28)), tag -> tagName(tag).getString(), this::tagColor, true, false);
        }
        graphics.pose().popPose();
        if (!dragging && split != null) {
            var tags = editor.value().groups().get(split.group());
            graphics.renderComponentTooltip(font, List.of(text("filter_split"), text("filter_split_preview", expression(tags.subList(0, split.splitAt())), expression(tags.subList(split.splitAt(), tags.size())))), mouseX, mouseY);
        }
    }
    private String expression(List<ModFilter.Tag> tags) { return String.join(" " + text("filter_or").getString() + " ", tags.stream().map(tag -> tagName(tag).getString()).toList()); }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (pressed != null && button == 1) { pressed = null; dragging = false; return true; }
        double mx = logicalX(x), my = logicalY(y);
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0 && button != 1) return false;
        var layout = layout(); var link = layout.link(mx, my);
        if (button == 0 && link != null) {
            if (!editor.split(link.group(), link.splitAt())) failed(); else { selectedGroup = link.group() + 1; selectedIndex = 0; bankFocused = false; notice = ""; revealSelection(); }
            return true;
        }
        var chip = ModFilterLayout.palette().stream().filter(c -> c.bounds().contains(mx, my)).findFirst().orElseGet(() -> layout.chip(mx, my));
        if (chip == null) return false;
        bankFocused = chip.group() < 0;
        if (bankFocused) bankIndex = chip.index(); else { selectedGroup = chip.group(); selectedIndex = chip.index(); }
        if (button == 1) {
            if (chip.group() >= 0) { editor.remove(chip.group(), chip.index()); normalizeSelection(); }
        } else { pressed = chip; pressX = mx; pressY = my; dragging = false; }
        return true;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (button != 0 || pressed == null) return false;
        pointerX = logicalX(x); pointerY = logicalY(y);
        if (Math.hypot(pointerX - pressX, pointerY - pressY) >= 4) dragging = true;
        return true;
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (button != 0 || pressed == null) return super.mouseReleased(logicalX(x), logicalY(y), button);
        var source = pressed; double mx = logicalX(x), my = logicalY(y);
        if (dragging) {
            if (ModFilterLayout.BANK.contains(mx, my) && source.group() >= 0) { editor.remove(source.group(), source.index()); normalizeSelection(); }
            else drop(source, layout().drop(mx, my));
        } else if (source.group() < 0) {
            int group = hasShiftDown() ? selectedGroup : -1;
            drop(source, new ModFilterLayout.Drop(group, group < 0 ? editor.value().groups().size() : editor.value().groups().get(group).size()));
        }
        pressed = null; dragging = false; return true;
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (!ModFilterLayout.ACTIVE.contains(logicalX(x), logicalY(y))) return false;
        scroll = Math.max(0, Math.min(layout().maximumScroll, scroll - (int) Math.round(dy * 30))); return true;
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) { if (pressed != null) { pressed = null; dragging = false; } else onClose(); return true; }
        pressed = null; dragging = false;
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (hasControlDown()) parent.closeFilters(editor.value());
            else if (bankFocused) {
                var chip = ModFilterLayout.palette().get(bankIndex); int group = hasShiftDown() ? selectedGroup : -1;
                drop(chip, new ModFilterLayout.Drop(group, group < 0 ? editor.value().groups().size() : editor.value().groups().get(group).size()));
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_TAB) { bankFocused = !bankFocused || selectedGroup < 0; return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && hasControlDown()) { clear(); return true; }
        if (key == GLFW.GLFW_KEY_DELETE && !bankFocused && selectedGroup >= 0) { editor.remove(selectedGroup, selectedIndex); normalizeSelection(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !bankFocused && selectedGroup >= 0 && selectedIndex > 0) {
            if (!editor.split(selectedGroup, selectedIndex)) failed(); else { selectedGroup++; selectedIndex = 0; revealSelection(); } return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            if (bankFocused) bankIndex = Math.floorMod(bankIndex + (key == GLFW.GLFW_KEY_LEFT ? -1 : key == GLFW.GLFW_KEY_RIGHT ? 1 : key == GLFW.GLFW_KEY_UP ? -2 : 2), ModFilter.Tag.values().length);
            else if (selectedGroup >= 0) {
                if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) selectedGroup += key == GLFW.GLFW_KEY_UP ? -1 : 1;
                else selectedIndex += key == GLFW.GLFW_KEY_LEFT ? -1 : 1;
                revealSelection();
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void tick() {
        if (noticeTicks > 0 && --noticeTicks == 0) notice = "";
        if (dragging && ModFilterLayout.ACTIVE.contains(pointerX, pointerY)) {
            if (pointerY < ModFilterLayout.ACTIVE.y() + 22) scroll = Math.max(0, scroll - 7);
            if (pointerY > ModFilterLayout.ACTIVE.y() + ModFilterLayout.ACTIVE.height() - 22) scroll = Math.min(layout().maximumScroll, scroll + 7);
        }
    }
    @Override public void onClose() { parent.closeFilters(original); }
}
