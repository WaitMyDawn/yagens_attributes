package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Build previews use packaged artwork; historical source comparison is an explicit local operation. */
public final class StatusIconPreview {
    private StatusIconPreview() {}
    public static void render() { render(null, Path.of("build/ui-preview/status-icons.png")); }
    public static void main(String[] args) {
        if (args.length != 2) throw new IllegalArgumentException("Expected original-icon directory and output PNG path");
        render(Path.of(args[0]), Path.of(args[1]));
    }
    private static BufferedImage published(DamageType type) throws IOException {
        String resource = "/assets/yagens_attributes/textures/status/" + type.key() + "_status.png";
        try (var stream = StatusIconPreview.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("Missing published texture: " + resource);
            var image = ImageIO.read(stream);
            if (image == null) throw new IOException("Unsupported image: " + resource);
            if (image.getWidth() != 32 || image.getHeight() != 32) throw new IOException("Expected 32x32 published texture: " + resource);
            return image;
        }
    }
    private static void render(Path originals, Path output) {
        var sheet = new BufferedImage(896, 652, BufferedImage.TYPE_INT_RGB);
        var graphics = sheet.createGraphics();
        graphics.setColor(new Color(0x121E29)); graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        graphics.setColor(new Color(0xE2C36D)); graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        graphics.drawString(originals == null ? "Published status icons: native 32px / nearest-neighbor 3x" : "Original 16px (left) / Published 32px (right)", 16, 28);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        try {
            int index = 0;
            for (DamageType type : DamageType.values()) {
                String name = type.key(); var result = published(type); BufferedImage source = result;
                if (originals != null) {
                    Path file = originals.resolve(name + ".png");
                    if (!Files.isRegularFile(file)) throw new IOException("Missing original status icon: " + file);
                    source = ImageIO.read(file.toFile());
                    if (source == null || source.getWidth() != 16 || source.getHeight() != 16) throw new IOException("Expected 16x16 original status icon: " + file);
                }
                int x = 16 + index % 4 * 224, y = 52 + index / 4 * 148;
                graphics.setColor(new Color(0xE4ECEF)); graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                graphics.drawString(name, x, y + 14);
                if (originals == null) graphics.drawImage(source, x + 24, y + 56, 32, 32, null);
                else graphics.drawImage(source, x, y + 24, 96, 96, null);
                graphics.drawImage(result, x + 104, y + 24, 96, 96, null);
                index++;
            }
            Path file = output.toAbsolutePath(); Files.createDirectories(file.getParent());
            if (!ImageIO.write(sheet, "png", file.toFile())) throw new IOException("PNG writer unavailable: " + file);
        } catch (IOException exception) { throw new IllegalStateException("Cannot inspect status icons: " + exception.getMessage(), exception); }
        finally { graphics.dispose(); }
    }
}
