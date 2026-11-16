package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Design fixture for glyph legibility and the bottom-anchored marker layout, not a game screenshot. */
public final class MarkerPreview {
    private MarkerPreview() {}
    public static void render() throws Exception {
        var image = new BufferedImage(1040, 650, BufferedImage.TYPE_INT_ARGB); var g = image.createGraphics();
        g.setColor(new Color(0x101B24)); g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(new Color(0xE1CE90)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 23)); g.drawString("极性符号与头顶触发标记 · 布局示意", 28, 38);
        int column = 0;
        for (var polarity : ModInfo.Polarity.values()) {
            int x = 30 + column++ * 112;
            try (var input = MarkerPreview.class.getResourceAsStream("/assets/yagens_attributes/textures/polarity/" + polarity.key() + ".png")) {
                g.drawImage(ImageIO.read(Objects.requireNonNull(input)), x + 18, 62, 64, 64, null);
            }
            g.setColor(Color.LIGHT_GRAY); g.setFont(new Font("SansSerif", Font.PLAIN, 14)); g.drawString(polarity.name(), x, 151);
        }
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16)); g.drawString("普通  等级 5", 28, 194);
        g.drawString("容量消耗 7     ｜     远程 三叉戟", 174, 194);
        try (var input = MarkerPreview.class.getResourceAsStream("/assets/yagens_attributes/textures/polarity/madurai.png")) { g.drawImage(ImageIO.read(input), 143, 177, 18, 18, null); }
        var counts = new EnumMap<DamageType, Integer>(DamageType.class);
        for (var type : DamageType.values()) counts.put(type, type.ordinal() + 1);
        scene(g, 190, 1.8, counts);
        scene(g, 530, 1.8, Map.of(DamageType.SLASH, 1024, DamageType.COLD, 10, DamageType.CORROSIVE, 6));
        scene(g, 865, .6, Map.of(DamageType.TOXIN, 8, DamageType.HEAT, 4));
        g.dispose(); Files.createDirectories(Path.of("build/ui-preview")); ImageIO.write(image, "PNG", Path.of("build/ui-preview/status-markers.png").toFile());
    }
    private static void scene(Graphics2D g, int center, double height, Map<DamageType, Integer> counts) throws Exception {
        int top = 465, bottom = top - (int) Math.round(height * 80 * .2);
        var rows = StatusMarkerLayout.rows(counts, 3); g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (int row = 0; row < rows.size(); row++) {
            int width = rows.get(row).stream().mapToInt(entry -> 25 + g.getFontMetrics().stringWidth("" + entry.count()) + 14).sum() - 14;
            int x = center - width / 2, y = bottom + Math.round(StatusMarkerLayout.rowY(row, rows.size()) * 2);
            for (var entry : rows.get(row)) {
                try (var input = MarkerPreview.class.getResourceAsStream("/assets/yagens_attributes/textures/status/" + entry.type().key() + "_status.png")) { g.drawImage(ImageIO.read(input), x, y, 18, 18, null); }
                g.setColor(Color.WHITE); g.drawString("" + entry.count(), x + 25, y + 16); x += 25 + g.getFontMetrics().stringWidth("" + entry.count()) + 14;
            }
        }
        g.setColor(new Color(0x75BFC8)); g.drawLine(center - 120, bottom, center + 120, bottom);
        g.setColor(new Color(0x324D5C)); g.fillRect(center - 25, top, 50, (int) (height * 80));
        g.setColor(Color.GRAY); g.drawLine(center + 78, bottom, center + 78, top);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14)); g.setColor(Color.LIGHT_GRAY);
        g.drawString("高度 " + height + " · 间距 " + String.format(Locale.ROOT, "%.2f", height * .2), center - 100, 630);
    }
}
