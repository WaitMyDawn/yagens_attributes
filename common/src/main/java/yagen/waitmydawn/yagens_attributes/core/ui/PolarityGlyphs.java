package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.ModInfo.Polarity;

/** Stable polarity font codepoints shared by data contracts and UI previews. */
public final class PolarityGlyphs {
    public static final int FIRST = 0xE100, SIZE = 32;
    private PolarityGlyphs() {}
    public static Polarity type(char codepoint) {
        int ordinal = codepoint - FIRST;
        return ordinal >= 0 && ordinal < Polarity.values().length ? Polarity.values()[ordinal] : null;
    }
}
