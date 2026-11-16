package yagen.waitmydawn.yagens_attributes.core;

public final class ExperiencePoints {
    public record State(int level, float progress) {}
    private ExperiencePoints() {}

    /** Cumulative vanilla XP at a level boundary; overflow is an unaffordable sentinel. */
    public static long total(int level) {
        if (level < 0) throw new IllegalArgumentException("Negative experience level");
        long value = level;
        if (level <= 16) return value * (value + 6);
        if (level <= 31) return (5 * value * value - 81 * value + 720) / 2;
        long other = 9 * value - 37;
        value -= 32;
        if ((value & 1) == 0) value /= 2; else other /= 2;
        return value > (Long.MAX_VALUE - 1628) / other ? Long.MAX_VALUE : value * other + 1628;
    }
    public static long next(int level) {
        if (level < 0) throw new IllegalArgumentException("Negative experience level");
        return level >= 30 ? 9L * level - 158 : level >= 15 ? 5L * level - 38 : 2L * level + 7;
    }
    public static long enchanting(int offeredLevel, int tier) {
        if (offeredLevel < 0 || tier < 1 || tier > 3) throw new IllegalArgumentException("Invalid enchantment offer");
        long high = total(offeredLevel);
        return high == Long.MAX_VALUE ? Long.MAX_VALUE : high - total(Math.max(0, offeredLevel - tier));
    }
    public static long balance(int level, float progress) {
        if (level < 0 || !Float.isFinite(progress) || progress < 0 || progress >= 1) return -1;
        long base = total(level), partial = Math.min(next(level) - 1, Math.round(progress * (double) next(level)));
        return base >= Long.MAX_VALUE - partial ? -1 : base + partial;
    }
    public static boolean affordable(long balance, long cost) {
        return balance >= 0 && cost >= 0 && cost < Long.MAX_VALUE && balance >= cost;
    }
    public static State state(long points) {
        if (points < 0 || points == Long.MAX_VALUE) throw new IllegalArgumentException("Unrepresentable experience balance");
        int low = 0, high = Integer.MAX_VALUE;
        while (low < high) {
            int middle = (int) ((low + (long) high + 1) / 2);
            if (total(middle) <= points) low = middle; else high = middle - 1;
        }
        float progress = (float) ((points - total(low)) / (double) next(low));
        return new State(low, Math.min(Math.nextDown(1F), progress));
    }
}
