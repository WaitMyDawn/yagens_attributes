package yagen.waitmydawn.yagens_attributes.core.ui;

import java.util.UUID;

/** Pointer state is independent of rendering scale and never mutates authoritative inventory data. */
public final class ModGesture {
    public enum Action { NONE, INSTALL, MOVE, REMOVE, WITHDRAW_ITEM }
    private UUID pressed, lastClick;
    private int sourceSlot, lastSlot;
    private double startX, startY;
    private long lastTime;
    private boolean dragging;
    public boolean press(UUID id, int slot, double x, double y, long now) {
        boolean twice = id.equals(lastClick) && slot == lastSlot && now >= lastTime && now - lastTime <= 300;
        pressed = id; sourceSlot = slot; startX = x; startY = y; dragging = false;
        lastClick = twice ? null : id; lastSlot = slot; lastTime = now;
        return twice;
    }
    public void drag(double x, double y) {
        if (pressed != null && Math.hypot(x - startX, y - startY) >= 4) { dragging = true; lastClick = null; }
    }
    public UUID pressed() { return pressed; }
    public boolean active() { return pressed != null; }
    public boolean dragging() { return dragging; }
    public Action drop(int slot, boolean pool, boolean physicalItem, boolean heldWeapon) {
        if (!dragging) return Action.NONE;
        if (slot >= 0) return sourceSlot < 0 ? Action.INSTALL : slot == sourceSlot ? Action.NONE : Action.MOVE;
        if (sourceSlot >= 0) return pool ? Action.REMOVE : Action.NONE;
        if (physicalItem) return Action.WITHDRAW_ITEM;
        return heldWeapon ? Action.INSTALL : Action.NONE;
    }
    public void release() { pressed = null; dragging = false; }
    public void cancel() { release(); lastClick = null; }
}
