package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import java.util.function.DoubleSupplier;

public final class Combat {
    public record Result(double damage, int criticalTier, List<DamageType> procs, double durationSeconds) {
    }
    public record Evaluation(Result result, Map<DamageType, Double> components, double componentTotal) {}

    private Combat() {
    }

    /**
     * Input is native attack damage before armor, resistance and protection reductions.
     */
    public static Result calculate(double input, WeaponProfile weapon, List<Map<String, Bonus>> bonuses,
                                   Map<DamageType, Double> material, DoubleSupplier random) {
        return calculate(input, weapon, bonuses, material, random, 0, 0);
    }

    public static Result calculate(double input, WeaponProfile weapon, List<Map<String, Bonus>> bonuses,
                                   Map<DamageType, Double> material, DoubleSupplier random, double addedChance, double addedCriticalDamage) {
        return evaluate(input, weapon, bonuses, material, random, addedChance, addedCriticalDamage).result();
    }

    /** A hit and its pre-material components, shared by offense and defense resolution. */
    public static Evaluation evaluate(double input, WeaponProfile weapon, List<Map<String, Bonus>> bonuses,
                                      Map<DamageType, Double> material, DoubleSupplier random, double addedChance, double addedCriticalDamage) {
        var amounts = components(input, weapon, bonuses);
        double total = 0, adjusted = 0;
        for (DamageType type : DamageType.values()) {
            double amount = amounts.get(type);
            double multiplier = material.getOrDefault(type, 1D);
            if (!Double.isFinite(multiplier) || multiplier < 0)
                throw new IllegalArgumentException("Invalid material multiplier");
            total += amount;
            adjusted += amount * multiplier;
        }
        int tier = roll(Math.min(100, Stats.resolve(weapon.criticalChance(), Stats.CRITICAL_CHANCE, bonuses) + addedChance), random);
        double criticalDamage = Math.max(1, Stats.resolve(weapon.criticalDamage(), Stats.CRITICAL_DAMAGE, bonuses) + addedCriticalDamage);
        int count = roll(Math.min(64, Stats.resolve(weapon.statusChance(), Stats.STATUS_CHANCE, bonuses)), random);
        List<DamageType> procs = new ArrayList<>();
        if (total > 0) {
            for (int i = 0; i < count; i++) {
                double position = random.getAsDouble() * total;
                for (DamageType type : DamageType.values()) {
                    position -= amounts.get(type);
                    if (position < 0) {
                        procs.add(type);
                        break;
                    }
                }
            }
            HitProcs.append(procs, tier, bonuses, random);
        }
        double result = adjusted * (1 + tier * (criticalDamage - 1));
        if (!Double.isFinite(result)) throw new IllegalArgumentException("Damage overflow");
        var hit = new Result(Math.min(Float.MAX_VALUE, result), tier, List.copyOf(procs),
                Math.min(3600, Stats.resolve(weapon.statusDuration(), Stats.STATUS_DURATION, bonuses)));
        return new Evaluation(hit, amounts, total);
    }

    /**
     * Component values before material and critical multipliers; absolute bonuses depend on input.
     */
    public static Map<DamageType, Double> components(double input, WeaponProfile weapon, List<Map<String, Bonus>> bonuses) {
        if (!Double.isFinite(input) || input < 0) throw new IllegalArgumentException("Invalid incoming damage");
        var amounts = new EnumMap<DamageType, Double>(DamageType.class);
        var damageBonuses = new ArrayList<Bonus>();
        for (var source : bonuses) {
            if (source.containsKey(Stats.DAMAGE)) damageBonuses.add(source.get(Stats.DAMAGE));
            if (source.containsKey(Stats.DIRECT_DAMAGE)) damageBonuses.add(source.get(Stats.DIRECT_DAMAGE));
        }
        // Direct-hit Addition shares the base-damage bucket, while status snapshots only read DAMAGE.
        double global = Math.max(0, Bonus.evaluate(1, damageBonuses));
        for (DamageType type : DamageType.values()) {
            double amount = Stats.resolve(input * weapon.distribution().getOrDefault(type, 0D), type.statKey(), bonuses, input) * global;
            if (!Double.isFinite(amount)) throw new IllegalArgumentException("Component damage overflow");
            amounts.put(type, amount);
        }
        ElementFusion.combineInPlace(amounts, bonuses);
        return Collections.unmodifiableMap(amounts);
    }

    /** Status bases exclude direct-hit material/defense and already include the original critical and volley share. */
    public static Map<DamageType, Double> statusBases(double input, WeaponProfile weapon, List<Map<String, Bonus>> bonuses,
                                                       int criticalTier, double addedCriticalDamage, double share) {
        if (!Double.isFinite(input) || input < 0 || criticalTier < 0 || criticalTier > 100 || !Double.isFinite(share) || share < 0)
            throw new IllegalArgumentException("Invalid status damage snapshot");
        double critical = 1 + criticalTier * (Math.max(1, Stats.resolve(weapon.criticalDamage(), Stats.CRITICAL_DAMAGE, bonuses) + addedCriticalDamage) - 1);
        double global = Stats.resolve(1, Stats.DAMAGE, bonuses) * critical * share;
        var bases = new EnumMap<DamageType, Double>(DamageType.class);
        for (var type : DamageType.values()) {
            double base = switch (type) { case HEAT, ELECTRICITY, TOXIN -> Stats.resolve(input, type.statKey(), bonuses, input); default -> input; };
            double amount = base * global;
            if (!Double.isFinite(amount)) throw new IllegalArgumentException("Status damage overflow");
            bases.put(type, Math.min(Float.MAX_VALUE, amount));
        }
        return Map.copyOf(bases);
    }

    private static int roll(double chance, DoubleSupplier random) {
        int guaranteed = (int) chance;
        return guaranteed + (chance > guaranteed && random.getAsDouble() < chance - guaranteed ? 1 : 0);
    }
}
