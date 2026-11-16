package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import java.util.function.DoubleSupplier;

public final class RangedStats {
    public static final int LIMIT = 64;

    public record Volley(int count, double damagePerProjectile) {
    }

    private RangedStats() {
    }

    public static double value(double base, String key, List<Map<String, Bonus>> sources) {
        return Math.min(LIMIT, Stats.resolve(base, key, sources));
    }

    public static Volley volley(double multishot, DoubleSupplier random) {
        if (!Double.isFinite(multishot) || multishot < 0 || multishot > LIMIT)
            throw new IllegalArgumentException("Invalid multishot");
        if (multishot == 0) return new Volley(0, 0);
        if (multishot < 1) return new Volley(1, multishot);
        int count = (int) multishot;
        if (multishot > count && random.getAsDouble() < multishot - count) count++;
        count = Math.max(1, count);
        return new Volley(count, multishot / count);
    }

    /**
     * A clock belongs to one use action; fractional progress never carries into another shot.
     */
    public static final class ChargeClock {
        private double rate;
        private long ticks, advanced;

        public ChargeClock(double rate) {
            rate(rate);
        }

        public void rate(double rate) {
            if (!Double.isFinite(rate) || rate < 0 || rate > LIMIT)
                throw new IllegalArgumentException("Invalid fire rate");
            this.rate = rate;
        }

        public int beforeTick(int remaining) {
            ticks++;
            long wanted = (long) Math.min(Integer.MAX_VALUE, Math.floor(Math.nextUp(ticks * rate)));
            long delta = wanted - advanced;
            advanced = wanted;
            return (int) Math.max(1, Math.min(Integer.MAX_VALUE, (long) remaining - delta + 1));
        }
    }
}
