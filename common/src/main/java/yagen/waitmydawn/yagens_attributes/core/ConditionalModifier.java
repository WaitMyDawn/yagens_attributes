package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Compiled rank-specific addition; conditions never modify the underlying item or permanent attributes. */
public record ConditionalModifier(String stat, double addition, Condition condition) {
    public enum Trigger { ALWAYS, TARGET_STATUS_TYPES, MELEE_KILLS, RANGED_KILLS }
    public record Condition(Trigger trigger, double durationSeconds, int maxStacks) {
        public static final Condition ALWAYS = new Condition(Trigger.ALWAYS, 0, 0);
        public static final Condition STATUS_TYPES = new Condition(Trigger.TARGET_STATUS_TYPES, 0, 0);
        public Condition {
            Objects.requireNonNull(trigger);
            if (!Double.isFinite(durationSeconds) || (trigger == Trigger.MELEE_KILLS || trigger == Trigger.RANGED_KILLS
                    ? durationSeconds <= 0 || durationSeconds > 3600 || maxStacks < 1 || maxStacks > 64
                    : durationSeconds != 0 || maxStacks != 0)) throw new IllegalArgumentException("Invalid Mod condition");
        }
        public int durationTicks() { return (int) Math.ceil(durationSeconds * 20); }
    }
    public ConditionalModifier {
        Objects.requireNonNull(condition);
        if (!Stats.KEYS.contains(stat) || !Double.isFinite(addition) || condition.trigger() == Trigger.ALWAYS)
            throw new IllegalArgumentException("Invalid conditional modifier");
    }
    public Map<String, Bonus> source(int count) {
        if (count < 0 || count > (condition.trigger() == Trigger.TARGET_STATUS_TYPES ? DamageType.values().length : condition.maxStacks()))
            throw new IllegalArgumentException("Invalid conditional stack count");
        return count == 0 ? Map.of() : Map.of(stat, new Bonus(0, addition * count, List.of(), 0));
    }
    public record Counter(int stacks, long expires) {
        public static final Counter EMPTY = new Counter(0, 0);
        public int count(long now) { return expires <= now ? 0 : stacks; }
        public Counter hit(long now, Condition rule) {
            return new Counter(Math.min(rule.maxStacks(), count(now) + 1), Math.addExact(now, rule.durationTicks()));
        }
    }
}
