package yagen.waitmydawn.yagens_attributes.core;

import yagen.waitmydawn.yagens_attributes.core.ui.HudLayout;

public final class HudLayoutTests {
    private static int checks;
    private HudLayoutTests() {}
    public static void run() {
        var normal = HudLayout.place(640, 360, 116, 31, 1, 16, 48);
        check(normal.x() == 508 && normal.y() == 281 && normal.width() == 116 && normal.height() == 31);
        check(normal.contains(508, 281) && normal.contains(623.9, 311.9));
        check(!normal.contains(507.9, 281) && !normal.contains(624, 281) && !normal.contains(508, 312));
        check(normal.move(640, 360, normal.x(), normal.y()).equals(new HudLayout.Offsets(16, 48)));
        check(normal.move(640, 360, -500, -300).equals(new HudLayout.Offsets(524, 329)));
        check(normal.move(640, 360, 900, 700).equals(new HudLayout.Offsets(0, 0)));
        check(HudLayout.place(640, 360, 116, 31, 1, 4096, 4096).x() == 0);
        check(HudLayout.place(640, 360, 116, 31, 1, -10, -10).x() == 524);
        for (int[] viewport : new int[][] {{320, 240}, {853, 480}, {1920, 1080}, {60, 20}})
            for (int[] size : new int[][] {{116, 31}, {73, 47}, {17, 200}})
                for (double scale : new double[] {.5, 1, 1.3, 4}) {
                    var box = HudLayout.place(viewport[0], viewport[1], size[0], size[1], scale, 29, 51);
                    check(box.x() >= 0 && box.y() >= 0 && box.x() + box.width() <= viewport[0] && box.y() + box.height() <= viewport[1]);
                    check(box.scale() > 0 && box.scale() <= scale);
                    for (double[] pointer : new double[][] {{-200, 9000}, {38.4, 56.7}, {800, -500}}) {
                        var offsets = box.move(viewport[0], viewport[1], pointer[0], pointer[1]);
                        var moved = HudLayout.place(viewport[0], viewport[1], size[0], size[1], scale, offsets.x(), offsets.y());
                        check(moved.x() == Math.round(Math.max(0, Math.min(viewport[0] - box.width(), pointer[0]))));
                        check(moved.y() == Math.round(Math.max(0, Math.min(viewport[1] - box.height(), pointer[1]))));
                    }
                }
        var resized = HudLayout.place(960, 540, 116, 31, 1, 16, 48);
        check(960 - resized.x() - resized.width() == 16 && 540 - resized.y() - resized.height() == 48);
        check(HudLayout.place(10000, 9000, 73, 47, 1, 0, 0).move(10000, 9000, 0, 0).equals(new HudLayout.Offsets(4096, 4096)));
        rejects(() -> normal.move(100, 20, 0, 0));
        rejects(() -> normal.move(640, 360, Double.NaN, 0));
        rejects(() -> HudLayout.place(0, 360, 116, 31, 1, 0, 0));
        rejects(() -> HudLayout.place(640, 360, -1, 31, 1, 0, 0));
        rejects(() -> HudLayout.place(640, 360, 116, 31, Double.POSITIVE_INFINITY, 0, 0));
        rejects(() -> HudLayout.place(640, 360, 116, 31, 0, 0, 0));
        System.out.println("Passed " + checks + " HUD placement/viewport/drag checks");
    }
    private static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("HUD layout mismatch"); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected invalid geometry rejection"); }
}
