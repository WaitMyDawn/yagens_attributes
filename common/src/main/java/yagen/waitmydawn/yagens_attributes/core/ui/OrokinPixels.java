package yagen.waitmydawn.yagens_attributes.core.ui;

/** Deterministic pixel-space overlay shared by generated item models and preview tools. */
public final class OrokinPixels {
    public static final int FRAMES = 48, FRAME_TICKS = 2;
    private OrokinPixels() {}
    public static int[] frame(int[] original, int width, int height, int frame) {
        if (width < 1 || height < 1 || width > 512 || height > 512 || original.length != width * height) throw new IllegalArgumentException("Invalid sprite dimensions");
        int[] result = original.clone();
        double sweep = Math.floorMod(frame, FRAMES) / (double) FRAMES * (width + height + 10) - 5;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int index = y * width + x, pixel = original[index], alpha = pixel >>> 24;
            double light = Math.max(0, 1 - Math.abs(x + y - sweep) / 3.0);
            if (alpha >= 13) {
                if (light > 0) result[index] = tint(pixel, 0xFFF5C7, light * .65);
            } else {
                boolean edge = false;
                for (int dy = -1; dy <= 1 && !edge; dy++) for (int dx = -1; dx <= 1; dx++) {
                    if (Math.abs(dx) + Math.abs(dy) != 1) continue;
                    int nx = x + dx, ny = y + dy;
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && (original[ny * width + nx] >>> 24) >= 128) { edge = true; break; }
                }
                if (edge) result[index] = tint(0xC6B98A37, 0xFFF5D1, .18 + light * .82);
            }
        }
        return result;
    }
    private static int tint(int pixel, int rgb, double amount) {
        int result = pixel & 0xFF000000;
        for (int shift : new int[]{16, 8, 0}) {
            int value = (int) Math.round(((pixel >>> shift) & 255) * (1 - amount) + ((rgb >>> shift) & 255) * amount);
            result |= value << shift;
        }
        return result;
    }
}
