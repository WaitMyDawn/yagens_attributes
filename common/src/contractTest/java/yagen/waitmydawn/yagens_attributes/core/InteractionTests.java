package yagen.waitmydawn.yagens_attributes.core;

import yagen.waitmydawn.yagens_attributes.core.ui.ModGesture;
import java.util.*;

public final class InteractionTests {
    private static int checks;
    private InteractionTests() {}
    public static void run() {
        UUID id = UUID.randomUUID(); var gesture = new ModGesture();
        check(!gesture.press(id, -1, 0, 0, 100)); gesture.release();
        check(gesture.press(id, -1, 0, 0, 200)); gesture.release();
        check(!gesture.press(id, -1, 0, 0, 250)); gesture.drag(2, 2); check(!gesture.dragging());
        gesture.drag(4, 0); check(gesture.dragging());
        check(gesture.drop(1, false, false, false) == ModGesture.Action.INSTALL);
        check(gesture.drop(-1, false, true, false) == ModGesture.Action.WITHDRAW_ITEM);
        check(gesture.drop(-1, false, false, true) == ModGesture.Action.INSTALL);
        check(gesture.drop(-1, true, false, false) == ModGesture.Action.NONE);
        gesture.cancel(); check(!gesture.active());
        check(!gesture.press(id, 2, 10, 10, 260)); gesture.drag(10, 15);
        check(gesture.drop(2, false, false, false) == ModGesture.Action.NONE);
        check(gesture.drop(3, false, false, false) == ModGesture.Action.MOVE);
        check(gesture.drop(-1, true, false, false) == ModGesture.Action.REMOVE);
        check(gesture.drop(-1, false, true, false) == ModGesture.Action.NONE);
        gesture.release(); check(!gesture.dragging());
        check(!gesture.press(UUID.randomUUID(), 2, 0, 0, 270)); gesture.release();
        UUID owner = UUID.randomUUID(), other = UUID.randomUUID(), weapon = UUID.randomUUID(), first = UUID.randomUUID(), second = UUID.randomUUID();
        int[] changes = {0}; ModLedger[] holder = new ModLedger[1];
        holder[0] = new ModLedger(() -> {
            changes[0]++;
            var slots = holder[0].installed(weapon).stream().map(ModLedger.Entry::slot).toList();
            check(new HashSet<>(slots).size() == slots.size());
        });
        var ledger = holder[0];
        ledger.add(new ModLedger.Entry(first, owner, weapon, false, Map.of(), ModInfo.DEFAULT, 0));
        ledger.add(new ModLedger.Entry(second, other, weapon, false, Map.of(), ModInfo.DEFAULT, 1));
        ledger.bonuses(weapon); int previous = changes[0];
        ledger.move(owner, first, weapon, 3, 4); check(ledger.entry(first).slot() == 3); check(changes[0] == previous + 1);
        rejects(() -> ledger.move(owner, first, weapon, 1, 4)); check(ledger.entry(first).slot() == 3); check(ledger.entry(second).slot() == 1);
        ledger.share(other, second, true); previous = changes[0];
        ledger.move(owner, first, weapon, 1, 4); check(ledger.entry(first).slot() == 1); check(ledger.entry(second).slot() == 3); check(changes[0] == previous + 1);
        check(ledger.entry(second).owner().equals(other)); check(ledger.usedCapacity(weapon) == 2);
        rejects(() -> ledger.move(UUID.randomUUID(), first, weapon, 0, 4));
        rejects(() -> ledger.move(owner, first, UUID.randomUUID(), 0, 4)); rejects(() -> ledger.move(owner, first, weapon, 4, 4));
        ledger.uninstall(owner, second, weapon, true); check(ledger.entry(second).owner().equals(owner)); check(ledger.owned(other).isEmpty());
        System.out.println("Passed " + checks + " gesture, atomic slot move/swap and permission checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Interaction contract failed at " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
