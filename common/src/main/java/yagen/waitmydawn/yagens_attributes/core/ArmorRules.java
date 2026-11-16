package yagen.waitmydawn.yagens_attributes.core;

/**
 * Armor curves return retained damage directly to preserve small fractions at large attributes.
 */
public final class ArmorRules {
    public enum Mode {
        NONE, IMPROVED_VANILLA, LINEAR;

        public String key() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Mode parse(String key) {
            return switch (key) {
                case "none" -> NONE;
                case "improved_vanilla" -> IMPROVED_VANILLA;
                case "linear" -> LINEAR;
                default -> throw new IllegalArgumentException("Unknown armor mode: " + key);
            };
        }
    }

    public static final double DEFAULT_SCALE = 15, DEFAULT_TOUGHNESS_SCALE = 8;
    public static final double MIN_SCALE = 1e-6, MAX_SCALE = 1e12;

    private ArmorRules() {
    }

    public static double remaining(double armor, double toughness, double scale, double toughnessScale) {
        if (!Double.isFinite(armor) || !Double.isFinite(toughness)
                || !Double.isFinite(scale) || !Double.isFinite(toughnessScale)
                || scale < MIN_SCALE || scale > MAX_SCALE || toughnessScale < MIN_SCALE || toughnessScale > MAX_SCALE)
            throw new IllegalArgumentException("Invalid armor inputs or scale");
        return fraction(scale, Math.max(0, armor)) * fraction(toughnessScale, Math.max(0, toughness));
    }

    private static double fraction(double scale, double value) {
        double ratio = Math.min(scale, value) / Math.max(scale, value);
        return scale >= value ? 1 / (1 + ratio) : ratio / (1 + ratio);
    }

    public static double improvedRemaining(double damage, double armor, double toughness) {
        if (!Double.isFinite(damage) || damage < 0 || !Double.isFinite(armor) || !Double.isFinite(toughness))
            throw new IllegalArgumentException("Invalid improved armor inputs");
        armor = Math.max(0, armor);
        toughness = Math.max(0, toughness);
        double base = legacyBase(armor, toughness);
        return Math.min(.75 + .25 * base, base + base * (damage / 20) * fraction(8, toughness));
    }

    private static double legacyBase(double armor, double toughness) {
        if (armor == 0) return 1;
        double retainedToughness = fraction(4, toughness);
        // Both branches avoid forming A*(1+T/L), which can overflow for finite attributes.
        if (armor >= 10) {
            double inverse = (10 / armor) * retainedToughness;
            return inverse / (1 + inverse);
        }
        return retainedToughness / (retainedToughness + armor / 10);
    }
}
