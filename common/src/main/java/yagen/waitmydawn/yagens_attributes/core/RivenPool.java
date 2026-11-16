package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Shared candidate and completion rules for sampling and exact probability analysis. */
final class RivenPool {
    record Choice(String id, RivenRules.Trait trait, long stat) {}

    private final List<Choice> eligible;
    private final long positive, negative;

    RivenPool(RivenRules rules, WeaponProfile profile) {
        var entries = rules.traits().entrySet().stream().filter(entry -> entry.getValue().accepts(profile))
                .sorted(Map.Entry.comparingByKey()).toList();
        var identities = new HashMap<String, Long>();
        var choices = new ArrayList<Choice>();
        long positives = 0, negatives = 0;
        for (var entry : entries) {
            // At most 64 traits are allowed; aliases reserve their shared stat rather than their ID.
            long bit = identities.computeIfAbsent(entry.getValue().stat(), key -> 1L << identities.size());
            choices.add(new Choice(entry.getKey(), entry.getValue(), bit));
            if (entry.getValue().positive()) positives |= bit;
            if (entry.getValue().negative()) negatives |= bit;
        }
        eligible = List.copyOf(choices); positive = positives; negative = negatives;
    }

    List<Choice> eligible() { return eligible; }

    boolean canComplete(long used, int positives, int negatives) {
        long availablePositive = positive & ~used, availableNegative = negative & ~used;
        int count = Long.bitCount(availablePositive);
        if (negatives == 0) return count >= positives;
        return availableNegative != 0 && count - ((availableNegative & ~availablePositive) == 0 ? 1 : 0) >= positives;
    }

    List<Choice> choices(long used, boolean negative, int positivesRemaining, int negativesReserved) {
        var result = new ArrayList<Choice>();
        for (var candidate : eligible)
            if ((candidate.stat() & used) == 0 && (negative ? candidate.trait().negative() : candidate.trait().positive())
                    && canComplete(used | candidate.stat(), positivesRemaining, negativesReserved)) result.add(candidate);
        return result;
    }
}
