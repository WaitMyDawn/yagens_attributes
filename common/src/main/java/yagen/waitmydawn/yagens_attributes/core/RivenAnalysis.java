package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Exact per-cycle inclusion probabilities conditional on one selected layout. */
public final class RivenAnalysis {
    public record Range(double minimum, double maximum) {
        public Range {
            if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum)
                throw new IllegalArgumentException("Invalid Riven value range");
        }
    }
    public record Outcome(String id, String stat, ModDefinition.Operation operation, double positiveChance,
                          double negativeChance, Range positiveRange, Range negativeRange) {}
    public record Result(boolean possible, double layoutChance, List<Outcome> outcomes) {
        public Result { outcomes = List.copyOf(outcomes); }
    }

    private RivenAnalysis() {}

    public static Result analyze(RivenRules rules, WeaponProfile profile, RivenRules.Factors layout, int level) {
        Objects.requireNonNull(rules); Objects.requireNonNull(profile); Objects.requireNonNull(layout);
        if (level < 0 || level > rules.maxLevel() || !rules.layouts().contains(layout))
            throw new IllegalArgumentException("Invalid analysis rank or layout");
        var pool = new RivenPool(rules, profile);
        double layoutChance = 1D / rules.layouts().size();
        if (!pool.canComplete(0, layout.positiveCount(), layout.negativeCount()))
            return new Result(false, layoutChance, List.of());
        var positive = new HashMap<String, Double>();
        var negative = new HashMap<String, Double>();
        Map<Long, Double> states = Map.of(0L, 1D);
        for (int slot = 0; slot < layout.positiveCount(); slot++) {
            var next = new HashMap<Long, Double>();
            for (var state : states.entrySet()) {
                var choices = pool.choices(state.getKey(), false, layout.positiveCount() - slot - 1, layout.negativeCount());
                if (choices.isEmpty()) throw new IllegalStateException("Feasible Riven state has no positive transition");
                double probability = state.getValue() / choices.size();
                for (var choice : choices) {
                    positive.merge(choice.id(), probability, Double::sum);
                    next.merge(state.getKey() | choice.stat(), probability, Double::sum);
                }
            }
            // Orders with the same used stats have identical future choices, including trait aliases.
            states = next;
        }
        if (layout.negativeCount() != 0) for (var state : states.entrySet()) {
            var choices = pool.choices(state.getKey(), true, 0, 0);
            if (choices.isEmpty()) throw new IllegalStateException("Feasible Riven state has no negative transition");
            double probability = state.getValue() / choices.size();
            for (var choice : choices) negative.merge(choice.id(), probability, Double::sum);
        }
        var order = new ArrayList<>(Stats.KEYS);
        var outcomes = new ArrayList<Outcome>();
        for (var candidate : pool.eligible()) {
            double positiveChance = positive.getOrDefault(candidate.id(), 0D), negativeChance = negative.getOrDefault(candidate.id(), 0D);
            if (positiveChance == 0 && negativeChance == 0) continue;
            outcomes.add(new Outcome(candidate.id(), candidate.trait().stat(), candidate.trait().operation(),
                    Math.min(1, positiveChance), Math.min(1, negativeChance),
                    positiveChance == 0 ? new Range(0, 0) : range(rules, candidate.trait(), profile, layout, false, level),
                    negativeChance == 0 ? new Range(0, 0) : range(rules, candidate.trait(), profile, layout, true, level)));
        }
        outcomes.sort(Comparator.comparingInt((Outcome outcome) -> order.indexOf(outcome.stat())).thenComparing(Outcome::id));
        return new Result(true, layoutChance, outcomes);
    }

    private static Range range(RivenRules rules, RivenRules.Trait trait, WeaponProfile profile, RivenRules.Factors layout, boolean negative, int level) {
        double first = rules.value(trait, profile.category(), profile.rivenDisposition(), layout, negative, level, 0);
        double last = rules.value(trait, profile.category(), profile.rivenDisposition(), layout, negative, level, 1);
        return new Range(Math.min(first, last), Math.max(first, last));
    }
}
