package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Compiled rank-specific addition; conditions never modify the underlying item or permanent attributes. */
public record ConditionalModifier(String stat, double addition, Condition condition) {
    public enum Trigger { ALWAYS, TARGET_STATUS_TYPES, MELEE_KILLS, RANGED_KILLS }
    public enum Decay { ALL_AT_ONCE, ONE_AT_A_TIME, INDEPENDENT }
    public record Condition(Trigger trigger, double durationSeconds, int maxStacks, boolean perTargetStatus, Decay decay) {
        public Condition(Trigger trigger, double durationSeconds, int maxStacks) {
            this(trigger, durationSeconds, maxStacks, false, Decay.ALL_AT_ONCE);
        }
        public static final Condition ALWAYS = new Condition(Trigger.ALWAYS, 0, 0);
        public static final Condition STATUS_TYPES = new Condition(Trigger.TARGET_STATUS_TYPES, 0, 0);
        public Condition {
            Objects.requireNonNull(trigger);
            Objects.requireNonNull(decay);
            if (!Double.isFinite(durationSeconds) || (trigger == Trigger.MELEE_KILLS || trigger == Trigger.RANGED_KILLS
                    ? durationSeconds <= 0 || durationSeconds > 3600 || maxStacks < 1 || maxStacks > 64
                    : durationSeconds != 0 || maxStacks != 0)) throw new IllegalArgumentException("Invalid Mod condition");
            boolean kill = trigger == Trigger.MELEE_KILLS || trigger == Trigger.RANGED_KILLS;
            if (!kill && (perTargetStatus || decay != Decay.ALL_AT_ONCE))
                throw new IllegalArgumentException("Only kill conditions support target factors and timed decay");
        }
        public boolean onKill() { return trigger == Trigger.MELEE_KILLS || trigger == Trigger.RANGED_KILLS; }
        public boolean needsTarget() { return trigger == Trigger.TARGET_STATUS_TYPES || perTargetStatus; }
        public int durationTicks() { return (int) Math.ceil(durationSeconds * 20); }
    }
    public ConditionalModifier {
        Objects.requireNonNull(condition);
        if (!Stats.KEYS.contains(stat) || !Double.isFinite(addition) || condition.trigger() == Trigger.ALWAYS)
            throw new IllegalArgumentException("Invalid conditional modifier");
    }
    public Map<String, Bonus> source(int count) {
        return source(count, 0);
    }
    public Map<String, Bonus> source(int count, int targetTypes) {
        if (count < 0 || count > (condition.trigger() == Trigger.TARGET_STATUS_TYPES ? DamageType.values().length : condition.maxStacks()))
            throw new IllegalArgumentException("Invalid conditional stack count");
        if (targetTypes < 0 || targetTypes > DamageType.values().length) throw new IllegalArgumentException("Invalid target status count");
        double value = addition * count * (condition.perTargetStatus() ? targetTypes : 1);
        return value == 0 ? Map.of() : Map.of(stat, new Bonus(0, value, List.of(), 0));
    }
    public record Counter(int stacks, long expires, List<Long> deadlines) {
        public Counter(int stacks, long expires) { this(stacks, expires, List.of()); }
        public Counter {
            deadlines = List.copyOf(deadlines);
            if (stacks < 0 || stacks > 64 || !deadlines.isEmpty() && (deadlines.size() != stacks
                    || deadlines.stream().anyMatch(value -> value < 0))) throw new IllegalArgumentException("Invalid kill counter");
        }
        public static final Counter EMPTY = new Counter(0, 0);
        public int count(long now) { return deadlines.isEmpty() ? expires <= now ? 0 : stacks : (int) deadlines.stream().filter(value -> value > now).count(); }
        public int count(long now, Condition rule) {
            if (rule.decay() == Decay.INDEPENDENT) return count(now);
            if (now < expires) return stacks;
            if (rule.decay() == Decay.ALL_AT_ONCE) return 0;
            return (int) Math.max(0, stacks - 1L - (now - expires) / rule.durationTicks());
        }
        public Counter hit(long now, Condition rule) {
            if (!rule.onKill()) throw new IllegalArgumentException("Counter requires a kill condition");
            if (rule.decay() == Decay.INDEPENDENT) {
                var active = new ArrayList<>(expirations(now, rule));
                while (active.size() >= rule.maxStacks()) active.remove(Collections.min(active));
                active.add(Math.addExact(now, rule.durationTicks())); active.sort(Long::compare);
                return new Counter(active.size(), active.get(0), active);
            }
            return new Counter(Math.min(rule.maxStacks(), count(now, rule) + 1), Math.addExact(now, rule.durationTicks()));
        }
        public List<Long> expirations(long now, Condition rule) {
            int count = count(now, rule);
            if (count == 0) return List.of();
            if (rule.decay() == Decay.INDEPENDENT && !deadlines.isEmpty()) return deadlines.stream().filter(value -> value > now).sorted().toList();
            if (rule.decay() != Decay.ONE_AT_A_TIME) return Collections.nCopies(count, expires);
            long first = now < expires ? expires : Math.addExact(expires, Math.multiplyExact(1 + (now - expires) / rule.durationTicks(), rule.durationTicks()));
            var result = new ArrayList<Long>();
            for (int i = 0; i < count; i++) result.add(Math.addExact(first, Math.multiplyExact((long) i, rule.durationTicks())));
            return List.copyOf(result);
        }
    }
}
