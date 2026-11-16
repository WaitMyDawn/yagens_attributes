package yagen.waitmydawn.yagens_attributes.api;

import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.Map;

/** Pre-defense damage bases already include launch modifiers, the hit's critical tier and volley share. */
public record StatusContext(Map<DamageType, Double> bases, int criticalTier, java.util.UUID rangedWeapon) {
    public StatusContext(Map<DamageType, Double> bases, int criticalTier) { this(bases, criticalTier, null); }
    public static final StatusContext EMPTY = new StatusContext(Map.of());
    public StatusContext(Map<DamageType, Double> bases) { this(bases, 0); }
    public StatusContext {
        bases = Map.copyOf(bases);
        if (criticalTier < 0 || criticalTier > 100) throw new IllegalArgumentException("Invalid status critical tier");
        for (double value : bases.values()) if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Invalid status base");
    }
    public double base(DamageType type) { return bases.getOrDefault(type, 0D); }
}
