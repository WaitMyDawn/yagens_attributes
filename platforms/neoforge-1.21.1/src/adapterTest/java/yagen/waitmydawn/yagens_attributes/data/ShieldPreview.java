package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.ui.ShieldPainter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

public final class ShieldPreview {
    private ShieldPreview() {}
    public static void render() {
        var image = new BufferedImage(800, 270, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setColor(new Color(0x101a24)); graphics.fillRect(0, 0, 800, 270);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 20));
        String[] labels = {"普通护盾", "过载护盾", "破盾保护"};
        ShieldPainter.Style[] styles = {ShieldPainter.Style.SHIELD, ShieldPainter.Style.OVERLOAD, ShieldPainter.Style.HEALTH};
        for (int row = 0; row < 3; row++) {
            graphics.setColor(new Color(0xd9e6ef)); graphics.drawString(labels[row], 28, 48 + row * 70);
            for (int index = 0; index < 10; index++) {
                int x = 200 + index * 48, y = 21 + row * 70;
                ShieldPainter.Fill fill = (left, top, right, bottom, color) -> { graphics.setColor(new Color(color, true)); graphics.fillRect(x + left * 4, y + top * 4, (right - left) * 4, (bottom - top) * 4); };
                ShieldPainter.heart(fill, 0, 0, index == 9 && row != 2, false, styles[row]);
                if (row == 2 && index == 9) ShieldPainter.lock(fill, 0, 0);
            }
        }
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 14)); graphics.setColor(new Color(0x839ba9));
        graphics.drawString("共享绘制器离屏预览 · 非游戏截图 · 4× 像素展示", 28, 249);
        graphics.dispose();
        try {
            var path = Path.of("build/ui-preview/shields.png"); Files.createDirectories(path.getParent()); ImageIO.write(image, "PNG", path.toFile());
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
}
