package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry;
import java.util.*;

public final class HotbarContracts {
    private static int checks;
    private HotbarContracts() {}
    public static void run() {
        for (int width : List.of(0, 80, 220, 2000)) for (int count : List.of(0, 1, 9)) {
            var cells = PanelGeometry.weaponStrip(width, count); equal(count, cells.size());
            for (int i = 0; i < cells.size(); i++) {
                var cell = cells.get(i); check(cell.x() >= 120 && cell.x() + cell.width() <= 800 && cell.y() + cell.height() <= 40);
                if (i > 0) check(cells.get(i - 1).x() + cells.get(i - 1).width() < cell.x());
            }
        }
        rejects(() -> PanelGeometry.weaponStrip(80, 10)); rejects(() -> PanelGeometry.weaponStrip(80, -1));
        System.out.println("Passed " + checks + " hotbar selector geometry and bounds checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Hotbar contract failed at " + checks); }
    private static void equal(Object expected, Object actual) { check(expected.equals(actual)); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
