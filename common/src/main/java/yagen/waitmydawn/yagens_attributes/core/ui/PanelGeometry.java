package yagen.waitmydawn.yagens_attributes.core.ui;

import java.util.*;

public final class PanelGeometry {
    public static final int WIDTH = 1100, HEIGHT = 600;
    public static final int STORAGE_PAGE_SIZE = 18, COMPATIBLE_PAGE_SIZE = 8;
    private static final List<Rect> STORAGE = squares(new Rect(20, 148, WIDTH - 40, 392), 6, 3, 9);
    private static final List<Rect> SLOTS = squares(new Rect(250, 108, 536, 264), 4, 2, 8);
    private static final List<Rect> COMPATIBLE = squares(new Rect(250, 444, 830, 96), 8, 1, 8);
    public static final Rect DETAILS = new Rect(800, 108, 280, 264);
    public static final Rect DISPOSITION = new Rect(154, 150, 62, 12);
    public static final Rect ENDO = new Rect(330, 82, 170, 24);
    public static final Rect FILTER = new Rect(704, 82, 120, 24);
    public static final Rect KUVA = new Rect(516, 82, 170, 24), WEAPON_KUVA = new Rect(250, 566, 36, 28);
    public static final Rect CYCLE_DIALOG = new Rect(230, 98, 640, 420);
    public static final Rect CYCLE_OLD = new Rect(250, 140, 290, 290), CYCLE_NEW = new Rect(560, 140, 290, 290);
    public static final Rect CYCLE_ROLL = new Rect(340, 461, 200, 30), CYCLE_CLOSE = new Rect(560, 461, 200, 30);
    public static final Rect CYCLE_HELP = new Rect(830, 108, 24, 24);
    public static final Rect RIVEN_RULES = new Rect(70, 40, 960, 520), RIVEN_RULE_LIST = new Rect(90, 230, 920, 216);
    public static final Rect RIVEN_RULE_BACK = new Rect(450, 516, 200, 26);
    public static final List<Rect> RIVEN_RULE_LAYOUTS = List.of(new Rect(240, 142, 120, 26), new Rect(370, 142, 120, 26), new Rect(500, 142, 120, 26), new Rect(630, 142, 120, 26));
    public static final int RIVEN_RULE_ROW_HEIGHT = 24;
    public static final Rect CAPACITY = new Rect(250, 94, 536, 5);
    public static final Rect WITHDRAWAL = new Rect(WIDTH / 2 - 160, HEIGHT / 2 - 85, 320, 170);
    public static final Rect PHYSICAL_MOD = new Rect(20, 566, 36, 28);
    public static final Rect PERMISSION = new Rect(66, 566, 36, 28);
    public static final Rect STORAGE_UPGRADE = new Rect(112, 566, 36, 28);
    public static final Rect RECYCLE = new Rect(158, 566, 36, 28);
    public static final Rect RELEASE = new Rect(204, 566, 36, 28);
    public static final Rect DETAIL_UPGRADE = new Rect(812, 338, 36, 22);
    public static final Rect POLARIZE = new Rect(112, 566, 36, 28);
    public static final Rect POLARIZATION = new Rect(360, 190, 380, 220);
    public static final List<Rect> POLARITY_CHOICES = List.of(new Rect(394, 256, 84, 56), new Rect(508, 256, 84, 56), new Rect(622, 256, 84, 56));
    public static final Rect POLARIZE_CONFIRM = new Rect(378, 372, 162, 24), POLARIZE_CANCEL = new Rect(560, 372, 162, 24);
    public static final Rect HELD_WEAPON = new Rect(270, 78, 48, 32);
    public static final Rect MOD_POOL = new Rect(250, 390, 830, 150);
    public static final Rect SLOT_PAGER = new Rect(670, 70, 116, 22);
    public static List<Rect> weaponStrip(int titleWidth, int count) {
        if (count < 0 || count > 9) throw new IllegalArgumentException("Invalid hotbar weapon count");
        int left = Math.min(400, Math.max(120, 40 + titleWidth));
        var result = new ArrayList<Rect>();
        for (int i = 0; i < count; i++) result.add(new Rect(left + i * 44, 4, 36, 36));
        return List.copyOf(result);
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
    }

    private PanelGeometry() {
    }

    public static List<Rect> grid(Rect area, int columns, int rows, int gap) {
        if (columns < 1 || rows < 1) throw new IllegalArgumentException("Invalid grid dimensions");
        int cellWidth = (area.width - (columns - 1) * gap) / columns, cellHeight = (area.height - (rows - 1) * gap) / rows;
        if (cellWidth < 1 || cellHeight < 1) throw new IllegalArgumentException("Grid does not fit");
        List<Rect> result = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            for (int column = 0; column < columns; column++)
                result.add(new Rect(area.x + column * (cellWidth + gap), area.y + row * (cellHeight + gap), cellWidth, cellHeight));
        return List.copyOf(result);
    }

    public static List<Rect> storage() {
        return STORAGE;
    }

    public static List<Rect> slots() {
        return SLOTS;
    }

    public static List<Rect> compatible() {
        return COMPATIBLE;
    }

    public static List<Rect> squares(Rect area, int columns, int rows, int gap) {
        var cells = grid(area, columns, rows, gap);
        int side = Math.min(cells.get(0).width(), cells.get(0).height());
        int left = area.x() + (area.width() - columns * side - (columns - 1) * gap) / 2;
        int top = area.y() + (area.height() - rows * side - (rows - 1) * gap) / 2;
        List<Rect> result = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            for (int column = 0; column < columns; column++)
                result.add(new Rect(left + column * (side + gap), top + row * (side + gap), side, side));
        return List.copyOf(result);
    }

    public static Rect artwork(Rect card) {
        int size = card.height() >= 120 ? Math.max(32, (card.height() - 52) / 32 * 32) : 32;
        // Reserve the top badges and title baseline even when the artwork is fully opaque.
        int header = 24, title = card.height() - (card.height() >= 96 ? 34 : 18);
        int top = header + Math.max(0, (title - header - size) / 2);
        return new Rect(card.x() + (card.width() - size) / 2, card.y() + top, size, size);
    }
}
