package yagen.waitmydawn.yagens_attributes.core.ui;

/** Pixel geometry shared by the in-game overlay and its offscreen design fixture. */
public final class ShieldPainter {
    @FunctionalInterface public interface Fill { void rect(int x, int y, int right, int bottom, int argb); }
    public enum Style {
        HEALTH(0xffda4147, 0xffff999c, 0xff90292e), SHIELD(0xff4ca9ec, 0xffb3e8ff, 0xff205181), OVERLOAD(0xffa478ea, 0xffddc4ff, 0xff5d3d91);
        final int base, light, dark;
        Style(int base, int light, int dark) { this.base = base; this.light = light; this.dark = dark; }
    }
    private static final String[] PIXELS = {".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."};
    private ShieldPainter() {}
    public static void heart(Fill fill, int x, int y, boolean half, boolean highlight, Style style) {
        for (int row = 0; row < PIXELS.length; row++) for (int column = 0; column < 7; column++)
            if (PIXELS[row].charAt(column) == '#' && (!half || column < 4))
                fill.rect(x + 1 + column, y + 1 + row, x + 2 + column, y + 2 + row, row < 2 && column < 3 ? highlight ? 0xffecf8ff : style.light : row > 3 ? style.dark : style.base);
    }
    public static void lock(Fill fill, int x, int y) {
        fill.rect(x + 2, y + 1, x + 7, y + 8, 0xff101b2b);
        fill.rect(x + 3, y + 1, x + 6, y + 2, 0xffdbf4ff);
        fill.rect(x + 2, y + 2, x + 3, y + 4, 0xffdbf4ff);
        fill.rect(x + 6, y + 2, x + 7, y + 4, 0xffdbf4ff);
        fill.rect(x + 2, y + 4, x + 7, y + 8, 0xffdbf4ff);
        fill.rect(x + 4, y + 5, x + 5, y + 7, 0xff305782);
    }
}
