package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class Stats {
    public static final String CRITICAL_CHANCE = "critical_chance";
    public static final String CRITICAL_DAMAGE = "critical_damage";
    public static final String STATUS_CHANCE = "status_chance";
    public static final String STATUS_DURATION = "status_duration";
    public static final String DAMAGE = "damage";
    public static final String FIRE_RATE = "fire_rate", MULTISHOT = "multishot";
    public static final String COMBO_DURATION = "combo_duration", COMBO_CRITICAL_CHANCE = "critical_chance_per_combo", COMBO_STATUS_CHANCE = "status_chance_per_combo";
    public static final String ATTACK_SPEED = "attack_speed", MELEE_RANGE = "melee_range";
    public static final String COMBO_COUNT_CHANCE = "combo_count_chance";
    public static final Set<String> KEYS;
    public static final Set<String> DAMAGE_COMPONENTS;

    static {
        var keys = new LinkedHashSet<>(List.of(CRITICAL_CHANCE, CRITICAL_DAMAGE, STATUS_CHANCE, STATUS_DURATION, DAMAGE, FIRE_RATE, MULTISHOT,
                COMBO_DURATION, COMBO_CRITICAL_CHANCE, COMBO_STATUS_CHANCE, ATTACK_SPEED, MELEE_RANGE, COMBO_COUNT_CHANCE));
        var components = new LinkedHashSet<String>();
        for (DamageType type : DamageType.values()) components.add("damage." + type.key());
        DAMAGE_COMPONENTS = Collections.unmodifiableSet(components); keys.addAll(components);
        KEYS = Collections.unmodifiableSet(keys);
    }

    private Stats() {
    }

    public static double base(WeaponProfile profile, String key) {
        return switch (key) {
            case CRITICAL_CHANCE -> profile.criticalChance();
            case CRITICAL_DAMAGE -> profile.criticalDamage();
            case STATUS_CHANCE -> profile.statusChance();
            case STATUS_DURATION -> profile.statusDuration();
            case FIRE_RATE -> profile.fireRate();
            case MULTISHOT -> profile.multishot();
            case COMBO_DURATION -> 5;
            case ATTACK_SPEED -> 1;
            case COMBO_COUNT_CHANCE -> 1;
            case MELEE_RANGE -> 0;
            default -> throw new IllegalArgumentException("No profile base for " + key);
        };
    }

    public static double resolve(double base, String key, List<Map<String, Bonus>> sources) {
        return resolve(base, key, sources, 0);
    }

    public static double resolve(double base, String key, List<Map<String, Bonus>> sources, double input) {
        List<Bonus> bonuses = new ArrayList<>();
        for (var source : sources) {
            Bonus bonus = source.get(key);
            if (bonus != null) bonuses.add(bonus);
        }
        return Math.max(0, Bonus.evaluate(base, bonuses, input));
    }
}
