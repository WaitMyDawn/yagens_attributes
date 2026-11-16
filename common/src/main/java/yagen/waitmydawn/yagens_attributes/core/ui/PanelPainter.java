package yagen.waitmydawn.yagens_attributes.core.ui;

public final class PanelPainter {
    public static final int GOLD = 0xFFE1CE90, TEXT = 0xFFE0E8EA, MUTED = 0xFF879AA4, TEAL = 0xFF75BFC8;
    public static final int MAX_LEVEL = 0xFF55FFFF;

    public interface Canvas {
        void fill(int x, int y, int width, int height, int color);

        void gradient(int x, int y, int width, int height, int top, int bottom);

        void text(String value, int x, int y, float size, int color, int maximumWidth);

        void centeredText(String value, int centerX, int y, float size, int color, int maximumWidth);
    }

    public record Card(String title, String subtitle, String polarity, int quantity, int rarity, boolean selected,
                       boolean occupied, int capacityCost, String rank, boolean maxRank, int capacityColor) {
        public Card(String title, String subtitle, String polarity, int quantity, int rarity, boolean selected, boolean occupied, int capacityCost, String rank, boolean maxRank) {
            this(title, subtitle, polarity, quantity, rarity, selected, occupied, capacityCost, rank, maxRank, 0);
        }
        public Card(String title, String subtitle, String polarity, int quantity, int rarity, boolean selected, boolean occupied, int capacityCost) {
            this(title, subtitle, polarity, quantity, rarity, selected, occupied, capacityCost, "", false);
        }
        public Card(String title, String subtitle, String polarity, int quantity, int rarity, boolean selected, boolean occupied) {
            this(title, subtitle, polarity, quantity, rarity, selected, occupied, 1);
        }
    }

    private PanelPainter() {
    }
    public static void disposition(Canvas canvas, int stars) {
        var bounds = PanelGeometry.DISPOSITION;
        String[] shape = {"...#...", "...#...", "#######", ".#####.", "..###..", ".##.##.", ".#...#."};
        for (int star = 0; star < 5; star++) for (int y = 0; y < shape.length; y++) for (int x = 0; x < shape[y].length(); x++)
            if (shape[y].charAt(x) == '#') canvas.fill(bounds.x() + star * 12 + x, bounds.y() + 2 + y, 1, 1, star < stars ? 0xFFB169F5 : 0xFF463352);
    }
    public enum Icon { INVENTORY, UPGRADE, PRIVATE, SHARED, RELEASE }
    public static void icon(Canvas c, PanelGeometry.Rect r, Icon icon, boolean enabled, boolean highlighted) {
        int color = !enabled ? 0xFF43515C : highlighted ? GOLD : TEAL;
        frame(c, r, 0xD0111D26, color, 4);
        int x = r.x() + r.width() / 2, y = r.y() + r.height() / 2;
        if (icon == Icon.INVENTORY) {
            c.fill(x - 9, y + 1, 2, 7, color); c.fill(x + 7, y + 1, 2, 7, color); c.fill(x - 9, y + 6, 18, 2, color);
            c.fill(x - 1, y - 8, 2, 12, color);
            for (int i = 0; i < 5; i++) c.fill(x - i, y + 2 - i, 2 * i + 1, 1, color);
        } else if (icon == Icon.RELEASE) {
            c.fill(x - 10, y - 6, 7, 2, color); c.fill(x - 10, y - 5, 2, 9, color); c.fill(x - 10, y + 3, 7, 2, color);
            c.fill(x + 3, y - 3, 7, 2, color); c.fill(x + 8, y - 2, 2, 9, color); c.fill(x + 3, y + 6, 7, 2, color);
            c.fill(x - 3, y - 10, 2, 4, color); c.fill(x + 1, y + 7, 2, 4, color);
        } else if (icon == Icon.UPGRADE) {
            c.fill(x - 1, y - 5, 2, 13, color);
            for (int i = 0; i < 7; i++) c.fill(x - i, y - 7 + i, 2 * i + 1, 1, color);
        } else {
            c.fill(x - 7, y - 1, 14, 9, color); c.fill(x - 1, y + 1, 2, 4, 0xFF111D26);
            int shift = icon == Icon.SHARED ? 4 : 0;
            c.fill(x - 5 + shift, y - 8, 10, 2, color); c.fill(x - 5 + shift, y - 7, 2, 7, color);
            if (icon == Icon.PRIVATE) c.fill(x + 3, y - 7, 2, 7, color);
        }
    }

    public static void background(Canvas c, String title, String subtitle) {
        c.gradient(0, 0, PanelGeometry.WIDTH, PanelGeometry.HEIGHT, 0xFF101A24, 0xFF04080C);
        for (int i = 0; i < 20; i++) c.fill(40 + i * 48, 0, 1, PanelGeometry.HEIGHT, 0x082FACBE);
        c.gradient(0, 0, PanelGeometry.WIDTH, 65, 0xD8223039, 0x0012202A);
        c.fill(20, 64, PanelGeometry.WIDTH - 40, 1, 0x806C8792);
        c.text(title, 22, 14, 20, GOLD, 360);
        c.text(subtitle, 24, 42, 10, MUTED, 650);
        c.fill(20, PanelGeometry.HEIGHT - 41, PanelGeometry.WIDTH - 40, 1, 0x806C8792);
        c.text("Y A G E N ' S   A T T R I B U T E S", PanelGeometry.WIDTH - 272, 20, 10, MUTED, 245);
    }

    public static void capacity(Canvas c, int used, int maximum, String label) {
        var bar = PanelGeometry.CAPACITY;
        c.centeredText(label, bar.x() + bar.width() / 2, bar.y() - 19, 12, used > maximum ? 0xFFE89A89 : GOLD, bar.width());
        c.fill(bar.x(), bar.y(), bar.width(), bar.height(), 0xFF263943);
        int filled = maximum <= 0 ? used > 0 ? bar.width() : 0 : (int) Math.round(bar.width() * Math.min(1, used / (double) maximum));
        c.fill(bar.x(), bar.y(), filled, bar.height(), used > maximum ? 0xFFE89A89 : TEAL);
    }

    public static void frame(Canvas c, PanelGeometry.Rect r, int fill, int edge, int cut) {
        int x = r.x(), y = r.y(), w = r.width(), h = r.height();
        c.fill(x + cut, y, w - 2 * cut, h, fill);
        c.fill(x, y + cut, w, h - 2 * cut, fill);
        for (int i = 0; i < cut; i++) {
            c.fill(x + cut - i, y + i, w - 2 * (cut - i), 1, fill);
            c.fill(x + cut - i, y + h - 1 - i, w - 2 * (cut - i), 1, fill);
            c.fill(x + cut - i, y + i, 1, 1, edge);
            c.fill(x + w - cut + i - 1, y + i, 1, 1, edge);
            c.fill(x + cut - i, y + h - i - 1, 1, 1, edge);
            c.fill(x + w - cut + i - 1, y + h - i - 1, 1, 1, edge);
        }
        c.fill(x + cut, y, w - 2 * cut, 1, edge);
        c.fill(x + cut, y + h - 1, w - 2 * cut, 1, edge);
        c.fill(x, y + cut, 1, h - 2 * cut, edge);
        c.fill(x + w - 1, y + cut, 1, h - 2 * cut, edge);
    }

    public static void card(Canvas c, PanelGeometry.Rect r, Card card) {
        int color = 0xFF000000 | card.rarity(), edge = card.selected() ? GOLD : color;
        frame(c, r, 0xFF111B24, edge, Math.min(9, r.height() / 5));
        c.gradient(r.x() + 3, r.y() + 3, r.width() - 6, r.height() - 6, (color & 0x00FFFFFF) | 0x30000000, 0x10101A24);
        if (r.height() >= 96) {
            c.centeredText(card.title(), r.x() + r.width() / 2, r.y() + r.height() - 32, 12, color, r.width() - 16);
            if (card.rank().isEmpty()) c.text(card.subtitle(), r.x() + 9, r.y() + r.height() - 17, 9, MUTED, r.width() - 18);
            else {
                c.text(card.rank(), r.x() + 9, r.y() + r.height() - 17, 9, card.maxRank() ? MAX_LEVEL : MUTED, r.width() / 2 - 12);
                c.text(card.subtitle(), r.x() + r.width() / 2 + 3, r.y() + r.height() - 17, 9, MUTED, r.width() / 2 - 12);
            }
        } else {
            c.centeredText(card.title(), r.x() + r.width() / 2, r.y() + r.height() - 16, 10, color, r.width() - 16);
        }
        if (card.quantity() > 1) c.text("×" + card.quantity(), r.x() + 9, r.y() + 7, 10, GOLD, 45);
        int badge = card.capacityColor() == 0 ? edge : card.capacityColor();
        frame(c, new PanelGeometry.Rect(r.x() + r.width() - 36, r.y() + 4, 29, 16), 0xD0091119, badge, 4);
        c.text((card.capacityCost() > 999 ? "999+" : String.valueOf(card.capacityCost())) + card.polarity(), r.x() + r.width() - 31, r.y() + 7, 9, badge, 24);
        if (card.occupied()) c.fill(r.x() + 8, r.y() + r.height() - 4, r.width() - 16, 2, TEAL);
        c.fill(r.x() + 10, r.y() + 2, 16, 1, 0xD0FFFFFF);
        c.fill(r.x() + r.width() - 26, r.y() + r.height() - 3, 16, 1, 0xC0FFFFFF);
    }

    public static void emptySlot(Canvas c, PanelGeometry.Rect r, String label, String polarity, boolean selected) {
        frame(c, r, 0xA817242D, selected ? GOLD : 0xFF526875, 10);
        frame(c, new PanelGeometry.Rect(r.x() + 4, r.y() + 4, r.width() - 8, r.height() - 8), 0x101B3441, 0x304D768A, 7);
        int cx = r.x() + r.width() / 2, cy = r.y() + r.height() / 2;
        c.fill(cx - 14, cy, 28, 1, 0x806F8C98);
        c.fill(cx, cy - 14, 1, 28, 0x806F8C98);
        c.text(polarity, r.x() + r.width() - 28, r.y() + 9, 13, GOLD, 20);
        c.text(label, r.x() + 12, r.y() + r.height() - 20, 10, MUTED, r.width() - 24);
    }
}
