package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public record WeaponProfile(double criticalChance, double criticalDamage, double statusChance,
                            double statusDuration, Map<DamageType, Double> distribution, double baseDamage,
                            ModInfo.Category category, List<ModInfo.Polarity> slots, double fireRate,
                            double multishot, double rivenDisposition) {
    public WeaponProfile(double cc, double cd, double sc, double duration, Map<DamageType, Double> distribution, double base, ModInfo.Category category, List<ModInfo.Polarity> slots, double fireRate, double multishot) {
        this(cc, cd, sc, duration, distribution, base, category, slots, fireRate, multishot, 0);
    }
    public WeaponProfile withDisposition(double value) { return new WeaponProfile(criticalChance, criticalDamage, statusChance, statusDuration, distribution, baseDamage, category, slots, fireRate, multishot, value); }
    public WeaponProfile(double cc, double cd, double sc, double duration, Map<DamageType, Double> distribution, double base, ModInfo.Category category, List<ModInfo.Polarity> slots) {
        this(cc, cd, sc, duration, distribution, base, category, slots, 1, 1);
    }

    public WeaponProfile(double cc, double cd, double sc, double duration, Map<DamageType, Double> distribution) {
        this(cc, cd, sc, duration, distribution, 1, ModInfo.Category.WEAPON, Collections.nCopies(8, ModInfo.Polarity.NONE));
    }

    public WeaponProfile {
        if (!Double.isFinite(rivenDisposition) || rivenDisposition != 0 && rivenDisposition < .5 || rivenDisposition > 1.55) throw new IllegalArgumentException("Invalid Riven disposition");
        if (!Double.isFinite(baseDamage) || baseDamage <= 0 || !ModInfo.Category.WEAPON.contains(category))
            throw new IllegalArgumentException("Invalid weapon baseline/category");
        slots = List.copyOf(slots);
        if (slots.isEmpty() || slots.size() > 64) throw new IllegalArgumentException("Invalid slot count");
        for (double number : new double[]{criticalChance, criticalDamage, statusChance, statusDuration, fireRate, multishot}) {
            if (!Double.isFinite(number) || number < 0) throw new IllegalArgumentException("Invalid weapon stat");
        }
        if (criticalChance > 100 || criticalDamage < 1 || statusChance > 64 || statusDuration > 3600 || fireRate > RangedStats.LIMIT || multishot > RangedStats.LIMIT)
            throw new IllegalArgumentException("Weapon stat outside safety limits");
        double total = 0;
        var normalized = new EnumMap<DamageType, Double>(DamageType.class);
        for (var entry : distribution.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || !Double.isFinite(entry.getValue()) || entry.getValue() < 0)
                throw new IllegalArgumentException("Invalid damage weight");
        }
        // Fixed type order keeps normalization identical across map implementations and JVM runs.
        for (var type : DamageType.values()) total += distribution.getOrDefault(type, 0D);
        if (!Double.isFinite(total) || total <= 0) throw new IllegalArgumentException("Empty damage distribution");
        for (var entry : distribution.entrySet()) if (entry.getValue() > 0) normalized.put(entry.getKey(), entry.getValue() / total);
        distribution = Collections.unmodifiableMap(normalized);
    }
}
