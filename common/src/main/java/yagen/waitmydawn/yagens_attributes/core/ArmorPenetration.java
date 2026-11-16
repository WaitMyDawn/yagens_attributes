package yagen.waitmydawn.yagens_attributes.core;

/** Percent penetration applies before flat penetration; neither changes the target's stored attributes. */
public record ArmorPenetration(double armorFlat, double armorPercent, double toughnessFlat, double toughnessPercent) {
    public static final ArmorPenetration NONE = new ArmorPenetration(0, 0, 0, 0);
    public ArmorPenetration {
        if (!Double.isFinite(armorFlat) || !Double.isFinite(armorPercent) || !Double.isFinite(toughnessFlat) || !Double.isFinite(toughnessPercent)
                || armorFlat < 0 || toughnessFlat < 0 || armorPercent < 0 || armorPercent > 1 || toughnessPercent < 0 || toughnessPercent > 1)
            throw new IllegalArgumentException("Invalid armor penetration");
    }
    public double armor(double value) { return Math.max(0, value * (1 - armorPercent) - armorFlat); }
    public double toughness(double value) { return Math.max(0, value * (1 - toughnessPercent) - toughnessFlat); }
    public ArmorPenetration plus(ArmorPenetration other) {
        return new ArmorPenetration(armorFlat + other.armorFlat, Math.min(1, armorPercent + other.armorPercent),
                toughnessFlat + other.toughnessFlat, Math.min(1, toughnessPercent + other.toughnessPercent));
    }
}
