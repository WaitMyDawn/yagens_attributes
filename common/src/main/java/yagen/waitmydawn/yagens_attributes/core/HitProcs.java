package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import java.util.function.DoubleSupplier;

/** Extra procs roll once per qualifying direct hit, independently of status chance and damage weights. */
public final class HitProcs {
    public enum Trigger { CRITICAL }
    public record Rule(String stat, Trigger trigger, DamageType type) {}
    public static final List<Rule> RULES = List.of(new Rule(Stats.CRITICAL_SLASH_CHANCE, Trigger.CRITICAL, DamageType.SLASH));
    public static final Set<String> STATS = Set.of(Stats.CRITICAL_SLASH_CHANCE);
    private HitProcs() {}

    public static void append(List<DamageType> procs, int criticalTier, List<Map<String, Bonus>> sources, DoubleSupplier random) {
        for (var rule : RULES) {
            if (rule.trigger() == Trigger.CRITICAL && criticalTier <= 0) continue;
            // Unit-relative Addition expresses probability points without inventing a nonzero innate proc chance.
            double chance = Math.min(1, Stats.resolve(0, rule.stat(), sources, 1));
            if (chance >= 1 || chance > 0 && random.getAsDouble() < chance) procs.add(rule.type());
        }
    }
}
