package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.ModStyle;

/** Shared frame metadata for atlas generation and headless previews. */
public final class ModAnimations {
    private ModAnimations() {}
    public static int frames(ModStyle.Visual visual) {
        return switch (visual) {
            case OROKIN -> OrokinPixels.FRAMES;
            case GALVANIZED -> GalvanizedPixels.FRAMES;
            case NONE -> throw new IllegalArgumentException("Static art has no animation");
        };
    }
    public static int frameTicks(ModStyle.Visual visual) {
        return switch (visual) {
            case OROKIN -> OrokinPixels.FRAME_TICKS;
            case GALVANIZED -> GalvanizedPixels.FRAME_TICKS;
            case NONE -> throw new IllegalArgumentException("Static art has no animation");
        };
    }
    public static int[] frame(ModStyle.Visual visual, int[] pixels, int width, int height, int frame) {
        return switch (visual) {
            case OROKIN -> OrokinPixels.frame(pixels, width, height, frame);
            case GALVANIZED -> GalvanizedPixels.frame(pixels, width, height, frame);
            case NONE -> throw new IllegalArgumentException("Static art has no animation");
        };
    }
}
