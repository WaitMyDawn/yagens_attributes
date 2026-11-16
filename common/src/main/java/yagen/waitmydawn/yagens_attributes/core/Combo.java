package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class Combo {
    public record Rules(int hitsPerTier, int maxTier) {
        public static final Rules DEFAULT = new Rules(20, 12);
        public Rules {
            if (hitsPerTier < 1 || hitsPerTier > 10000 || maxTier < 2 || maxTier > 100) throw new IllegalArgumentException("Invalid combo rules");
        }
        public int maximumCount() { return hitsPerTier * (maxTier - 1); }
        public int tier(int count) { return Math.min(maxTier, 1 + Math.max(0, count) / hitsPerTier); }
    }

    public record Counter(int count, long expires, int duration) {
        public static final Counter EMPTY = new Counter(0, 0, 0);
        public Counter {
            if (count < 0 || count > 990000 || expires < 0 || duration < 0 || duration > 144000) throw new IllegalArgumentException("Invalid combo counter");
        }
        public int count(long now, Rules rules) { return now < expires ? Math.min(count, rules.maximumCount()) : 0; }
        public int remaining(long now) { return (int) Math.max(0, Math.min(duration, expires - now)); }
        public Counter hit(long now, int ticks, Rules rules) {
            return hit(now, ticks, rules, 1);
        }
        public Counter hit(long now, int ticks, Rules rules, int gained) {
            if (now < 0 || ticks < 0 || ticks > 144000) throw new IllegalArgumentException("Invalid combo time");
            if (gained < 0 || gained > 64) throw new IllegalArgumentException("Invalid combo gain");
            return ticks == 0 ? EMPTY : new Counter(Math.min(rules.maximumCount(), count(now, rules) + gained), Math.addExact(now, ticks), ticks);
        }
    }

    private Combo() {}
    public static int gains(double chance, java.util.function.DoubleSupplier random) {
        if (!Double.isFinite(chance)) throw new IllegalArgumentException("Invalid combo chance");
        chance = Math.max(0, Math.min(64, chance)); int whole = (int) chance;
        return whole + (chance > whole && random.getAsDouble() < chance - whole ? 1 : 0);
    }

    public static int duration(double baseSeconds, List<Map<String, Bonus>> sources) {
        double seconds = Stats.resolve(baseSeconds, Stats.COMBO_DURATION, sources);
        return seconds <= 0 ? 0 : (int) Math.ceil(Math.min(7200, Math.max(.1, seconds)) * 20);
    }

    /** Conditional chance entries join the same additive zones as ordinary chance Mods. */
    public static List<Map<String, Bonus>> sources(List<Map<String, Bonus>> sources, int tier) {
        if (tier <= 1) return sources;
        var result = new ArrayList<>(sources);
        for (var source : sources) {
            var extra = new HashMap<String, Bonus>();
            if (source.containsKey(Stats.COMBO_CRITICAL_CHANCE)) extra.put(Stats.CRITICAL_CHANCE, scale(source.get(Stats.COMBO_CRITICAL_CHANCE), tier - 1));
            if (source.containsKey(Stats.COMBO_STATUS_CHANCE)) extra.put(Stats.STATUS_CHANCE, scale(source.get(Stats.COMBO_STATUS_CHANCE), tier - 1));
            if (!extra.isEmpty()) result.add(Map.copyOf(extra));
        }
        return List.copyOf(result);
    }

    private static Bonus scale(Bonus bonus, int steps) {
        return new Bonus(bonus.addin() * steps, bonus.addition() * steps,
                bonus.multiply().stream().map(factor -> Math.max(0, 1 + (factor - 1) * steps)).toList(), bonus.addout() * steps,
                bonus.inputAddin() * steps, bonus.inputAddition() * steps);
    }
}
