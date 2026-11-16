package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Side-by-side art inspection uses nearest-neighbor scaling to expose individual source pixels. */
public final class StatusIconPreview {
    private StatusIconPreview() {}
    public static void render() {
        var sheet = new BufferedImage(896, 652, BufferedImage.TYPE_INT_RGB);
        var graphics = sheet.createGraphics();
        graphics.setColor(new Color(0x121E29)); graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        graphics.setColor(new Color(0xE2C36D)); graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        graphics.drawString("Original 16px (left) / Retouched 32px (right)", 16, 28);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        try {
            int index = 0;
            for (DamageType type : DamageType.values()) {
                String name = type.name().toLowerCase(java.util.Locale.ROOT);
                var source = ImageIO.read(Path.of("../../art/status_icons/source16", name + ".png").toFile());
                BufferedImage result;
                try (var stream = StatusIconPreview.class.getResourceAsStream("/assets/yagens_attributes/textures/status/" + name + "_status.png")) {
                    if (stream == null) throw new IOException("Missing status texture: " + name);
                    result = ImageIO.read(stream);
                }
                if (source.getWidth() != 16 || source.getHeight() != 16 || result.getWidth() != 32 || result.getHeight() != 32)
                    throw new AssertionError("Unexpected status icon dimensions: " + name);
                int x = 16 + index % 4 * 224, y = 52 + index / 4 * 148;
                graphics.setColor(new Color(0xE4ECEF)); graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                graphics.drawString(name, x, y + 14);
                graphics.drawImage(source, x, y + 24, 96, 96, null);
                graphics.drawImage(result, x + 104, y + 24, 96, 96, null);
                index++;
            }
            Files.createDirectories(Path.of("build/ui-preview"));
            ImageIO.write(sheet, "png", Path.of("build/ui-preview/status-icons.png").toFile());
        } catch (IOException exception) { throw new IllegalStateException("Cannot inspect status icons", exception); }
        finally { graphics.dispose(); }
    }
}
