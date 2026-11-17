package yagen.waitmydawn.yagens_attributes.core.ui;

import java.util.Arrays;

/** Symmetric carved metal contours with two independent, precomputed four-point sparkle tracks. */
public final class GalvanizedPixels {
    public static final int FRAMES = 128, FRAME_TICKS = 2;
    private static final int SPARKLE_CHANNELS = 2, SPARKLE_WINDOWS = 4, SPARKLE_DURATION = 7;
    private static final String[] CORNER = {"  dddd", " dhhmd", "dhxmdl", "dhmxml", "dmmxl ", "ddll  "};
    private GalvanizedPixels() {}

    public static int[] frame(int[] original, int width, int height, int frame) {
        int[] result = base(original, width, height), metal = ornament(width, height);
        int radius = Math.min(3, thickness(width, height));
        int[] anchors = new int[metal.length]; int count = 0;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int band = Math.min(Math.min(x, width - 1 - x), Math.min(y, height - 1 - y));
            if (metal[y * width + x] != 0 && band <= Math.max(radius, thickness(width, height) - 1)
                    && x >= radius && y >= radius && x < width - radius && y < height - radius)
                anchors[count++] = y * width + x;
        }
        if (count == 0) return result;
        int phase = Math.floorMod(frame, FRAMES), seed = mix(Arrays.hashCode(original) ^ width * 73 ^ height * 151);
        for (int channel = 0; channel < SPARKLE_CHANNELS; channel++) {
            for (int window = 0; window < SPARKLE_WINDOWS; window++) {
                int windowStart = window * FRAMES / SPARKLE_WINDOWS, windowEnd = (window + 1) * FRAMES / SPARKLE_WINDOWS;
                if (phase < windowStart || phase >= windowEnd) continue;
                int eventSeed = mix(seed ^ channel * 0x632BE59B ^ window * 0x9E3779B9);
                int start = windowStart + Math.floorMod(mix(eventSeed ^ 0x51ED270B), windowEnd - windowStart - SPARKLE_DURATION + 1);
                int age = phase - start;
                if (age <= 0 || age >= SPARKLE_DURATION - 1) continue;
                int anchor = anchors[Math.floorMod(mix(eventSeed ^ 0x68E31DA4), count)];
                double pulse = Math.pow(Math.sin(Math.PI * age / (SPARKLE_DURATION - 1)), 2);
                sparkle(result, width, anchor % width, anchor / width, radius, pulse);
            }
        }
        return result;
    }

    public static int[] base(int[] original, int width, int height) {
        int[] result = subject(original, width, height), metal = ornament(width, height);
        for (int i = 0; i < result.length; i++) if (metal[i] != 0) result[i] = over(result[i], metal[i]);
        return result;
    }

    /** Folded coordinates keep both the cut-out silhouette and its relief symmetric on both axes. */
    public static int[] ornament(int width, int height) {
        validate(width, height);
        int[] result = new int[width * height];
        int thickness = thickness(width, height), reach = thickness + 2;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            result[y * width + x] = surface(Math.min(x, width - 1 - x), Math.min(y, height - 1 - y), width, height, thickness, reach);
        return result;
    }

    /** Reserves frame space without rewriting the original subject resource. */
    public static int[] subject(int[] original, int width, int height) {
        validate(width, height);
        if (original.length != width * height) throw new IllegalArgumentException("Invalid subject dimensions");
        int inset = thickness(width, height), innerWidth = width - 2 * inset, innerHeight = height - 2 * inset;
        int[] result = new int[original.length];
        for (int y = 0; y < innerHeight; y++) for (int x = 0; x < innerWidth; x++)
            result[(y + inset) * width + x + inset] = original[(y * height / innerHeight) * width + x * width / innerWidth];
        return result;
    }

    private static int thickness(int width, int height) { return Math.max(1, Math.min(4, Math.min(width, height) / 8)); }

    private static void validate(int width, int height) {
        if (width < 8 || height < 8 || width > 512 || height > 512) throw new IllegalArgumentException("Invalid sprite dimensions");
    }

    private static int surface(int x, int y, int width, int height, int thickness, int reach) {
        if (x < reach && y < reach) {
            char pixel = CORNER[y * 6 / reach].charAt(x * 6 / reach);
            return switch (pixel) {
                case 'd' -> 0xF030354A;
                case 'm' -> 0xF4636A8E;
                case 'l' -> 0xFA8792AC;
                case 'h' -> 0xFFA9B8CE;
                case 'x' -> 0xF0202638;
                default -> 0;
            };
        }
        int band = Math.min(x, y), along = y <= x ? x : y, length = y <= x ? width : height;
        int step = along * 32 / length;
        int notch = step == 9 || step == 10 ? 2 : step == 8 || step == 11 || step >= 14 ? 1 : 0;
        notch = Math.min(thickness - 1, notch);
        boolean crest = step == 6 || step == 7 || step == 12 || step == 13;
        int inner = thickness - 1 + (crest ? 1 : 0);
        if (band < notch || band > inner) return 0;
        if (band == notch) return 0xED30354A;
        if (band == notch + 1) return step % 4 == 0 ? 0xF0636A8E : 0xFA8792AC;
        if (band == inner) return crest ? 0xEE8F9DB6 : 0xED3C455F;
        return 0xF4636A8E;
    }

    private static void sparkle(int[] pixels, int width, int x, int y, int maximumRadius, double pulse) {
        int radius = Math.max(1, (int) Math.round(maximumRadius * (.5 + .5 * pulse)));
        for (int dy = -radius; dy <= radius; dy++) for (int dx = -radius; dx <= radius; dx++) {
            int ax = Math.abs(dx), ay = Math.abs(dy);
            if (ax != 0 && ay != 0 && (radius < 2 || ax > 1 || ay > 1)) continue;
            int distance = Math.max(ax, ay);
            double coverage = ax == 0 && ay == 0 ? 1 : ax != 0 && ay != 0 ? .62 : 1 - distance / (radius + 1.5);
            int alpha = (int) Math.round(255 * pulse * coverage);
            if (alpha == 0) continue;
            int color = distance == 0 ? 0xF4FBFF : distance < radius ? 0xD5ECFF : 0xAECFEA;
            int index = (y + dy) * width + x + dx;
            pixels[index] = over(pixels[index], alpha << 24 | color);
        }
    }

    private static int mix(int value) {
        value ^= value >>> 16; value *= 0x7FEB352D; value ^= value >>> 15; value *= 0x846CA68B;
        return value ^ value >>> 16;
    }

    private static int over(int subject, int overlay) {
        double front = (overlay >>> 24) / 255D, back = (subject >>> 24) / 255D;
        double alpha = front + back * (1 - front);
        int result = (int) Math.round(alpha * 255) << 24;
        for (int shift : new int[]{16, 8, 0}) {
            double value = ((overlay >>> shift & 255) * front + (subject >>> shift & 255) * back * (1 - front)) / alpha;
            result |= (int) Math.round(value) << shift;
        }
        return result;
    }
}
