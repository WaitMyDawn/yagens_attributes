package yagen.waitmydawn.yagens_attributes.core;

public enum DamageType {
    IMPACT, PUNCTURE, SLASH,
    COLD, HEAT, ELECTRICITY, TOXIN,
    BLAST, RADIATION, CORROSIVE, VIRAL, GAS, MAGNETIC;

    // Wire ordinals remain stable; presentation order is independent of serialization and proc rolls.
    public static final java.util.List<DamageType> DISPLAY_ORDER = java.util.List.of(
            BLAST, RADIATION, CORROSIVE, VIRAL, GAS, MAGNETIC,
            COLD, HEAT, ELECTRICITY, TOXIN,
            SLASH, PUNCTURE, IMPACT);

    private final String key = name().toLowerCase(java.util.Locale.ROOT);
    private final String statKey = "damage." + key;

    public String key() { return key; }
    String statKey() { return statKey; }

    public static DamageType parse(String key) { return valueOf(key.toUpperCase(java.util.Locale.ROOT)); }
}
