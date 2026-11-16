package yagen.waitmydawn.yagens_attributes.datagen;

import yagen.waitmydawn.yagens_attributes.core.ModInfo.Polarity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Original references remain unchanged; runtime glyphs use their alpha silhouette for tintable text.
 */
public final class PolarityTextures {
    private PolarityTextures() {
    }

    public static BufferedImage export(Path source, Polarity polarity) throws IOException {
        var result = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        if (polarity == Polarity.NONE) {
            var g = result.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(9, 15, 14, 2);
            g.dispose();
            return result;
        }
        return glyph(source.resolve(polarity.key() + ".png"));
    }
    public static BufferedImage glyph(Path source) throws IOException {
        var result = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        var image = ImageIO.read(source.toFile());
        if (image == null) throw new IOException("Unsupported glyph image: " + source);
        int left = image.getWidth(), top = image.getHeight(), right = -1, bottom = -1;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
        if (right < left) throw new IOException("Empty glyph silhouette: " + source);
        var mask = new BufferedImage(right - left + 1, bottom - top + 1, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < mask.getHeight(); y++)
            for (int x = 0; x < mask.getWidth(); x++)
                mask.setRGB(x, y, (image.getRGB(left + x, top + y) & 0xFF000000) | 0xFFFFFF);
        double scale = 28D / Math.max(mask.getWidth(), mask.getHeight());
        int width = Math.max(1, (int) Math.round(mask.getWidth() * scale)), height = Math.max(1, (int) Math.round(mask.getHeight() * scale));
        var g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scale >= 1 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(mask, (32 - width) / 2, (32 - height) / 2, width, height, null);
        g.dispose();
        return result;
    }
}
