package yagen.waitmydawn.util;

public enum SupportedMod {
    IRONSSPELLBOOK("irons_spellbook"),
    CATACLYSM("cataclysm"),
    TWILIGHTFOREST("twilightforest"),
    IAF("iceandfire"),
    L2ARCHERY("l2archery"),
    DUNGEONS_ARISE("dungeons_arise"),;

    private final String value;

    SupportedMod(final String newValue) {
        value = newValue;
    }

    public String getValue() {
        return this.value;
    }

    public static SupportedMod fromString(String str) {
        for (SupportedMod t : values()) {
            if (t.getValue().equals(str)) return t;
        }
        throw new IllegalArgumentException("Unknown SupportedMod: " + str);
    }


}
