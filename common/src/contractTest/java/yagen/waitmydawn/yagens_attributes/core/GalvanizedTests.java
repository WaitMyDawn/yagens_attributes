package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;

final class GalvanizedTests {
    private static int checks;
    private GalvanizedTests() {}
    static void run() {
        for (int[] size : List.of(new int[]{32, 32}, new int[]{16, 16}, new int[]{20, 28}, new int[]{28, 20})) {
            int width = size[0], height = size[1];
            var subject = new int[width * height]; Arrays.fill(subject, 0xFF305070);
            var inset = GalvanizedPixels.subject(subject, width, height);
            int thickness = Math.min(4, Math.min(width, height) / 8), radius = Math.min(3, thickness);
            int protectedInset = Math.max(thickness + 2, Math.max(radius, thickness - 1) + radius + 1);
            int[] ornament = GalvanizedPixels.ornament(width, height);
            boolean symmetric = true;
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
                symmetric &= ornament[y * width + x] == ornament[y * width + width - 1 - x]
                        && ornament[y * width + x] == ornament[(height - 1 - y) * width + x];
            check(symmetric);
            var contour = new HashSet<Integer>();
            for (int x = protectedInset; x < width - protectedInset; x++) {
                int y = 0; while (y < height && ornament[y * width + x] == 0) y++;
                contour.add(y);
            }
            check(contour.size() > 1);
            for (int frame : List.of(0, 14, 39, 81, -1)) {
                int[] result = GalvanizedPixels.frame(subject, width, height, frame);
                check(result != subject); check(result.length == subject.length);
                boolean inputUnchanged = true, interiorUnchanged = true;
                for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    inputUnchanged &= subject[index] == 0xFF305070;
                    if (x >= protectedInset && y >= protectedInset && x < width - protectedInset && y < height - protectedInset)
                        interiorUnchanged &= result[index] == inset[index];
                }
                check(inputUnchanged); check(interiorUnchanged);
                check(Arrays.equals(result, GalvanizedPixels.frame(subject, width, height, frame + GalvanizedPixels.FRAMES)));
            }
        }
        var transparent = new int[1024];
        for (int frame : List.of(0, 14, 39, 81)) {
            var result = GalvanizedPixels.frame(transparent, 32, 32, frame);
            long visible = Arrays.stream(result).filter(pixel -> (pixel >>> 24) > 0).count(); check(visible > 300 && visible < 600);
            check(result[16 * 32 + 16] == 0);
        }
        check(Arrays.equals(GalvanizedPixels.frame(transparent, 32, 32, 0), GalvanizedPixels.frame(transparent, 32, 32, 128)));
        boolean observedTwo = false;
        for (int sample = 0; sample < 8; sample++) {
            int[] pixels = new int[1024]; pixels[16 * 32 + 16] = 0xFF000000 | sample * 0x12345;
            int[] base = GalvanizedPixels.base(pixels, 32, 32);
            int quiet = 0, active = 0; boolean bounded = true;
            for (int frame = 0; frame < GalvanizedPixels.FRAMES; frame++) {
                int[] result = GalvanizedPixels.frame(pixels, 32, 32, frame);
                int count = components(base, result, 32, 32);
                if (count == 0) quiet++; else active++;
                observedTwo |= count == 2; bounded &= count <= 2;
            }
            check(quiet > 0); check(active > 0); check(bounded);
        }
        check(observedTwo);
        var galv = BuiltinMods.catalog().definitions().get("yagens_attributes:galvanized_chamber");
        check(galv.style().visual() == ModStyle.Visual.GALVANIZED);
        check(ModAnimations.frames(ModStyle.Visual.GALVANIZED) * ModAnimations.frameTicks(ModStyle.Visual.GALVANIZED) == 256);
        check(galv.style().conflicts(ModStyle.family("yagens_attributes:split_chamber")));
        System.out.println("Passed " + checks + " galvanized boundary, subject preservation and animation-loop checks");
    }
    private static int components(int[] base, int[] frame, int width, int height) {
        boolean[] changed = new boolean[frame.length];
        for (int i = 0; i < changed.length; i++) changed[i] = base[i] != frame[i];
        int[] queue = new int[frame.length]; int components = 0;
        for (int start = 0; start < changed.length; start++) if (changed[start]) {
            components++; int read = 0, write = 0; queue[write++] = start; changed[start] = false;
            while (read < write) {
                int position = queue[read++], x = position % width, y = position / width;
                for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                    int neighbor = ny * width + nx;
                    if (changed[neighbor]) { changed[neighbor] = false; queue[write++] = neighbor; }
                }
            }
        }
        return components;
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Galvanized animation check " + checks); }
}
