package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public record WeaponProgress(int level, int experience) {
    public static final int MAX_LEVEL = 30;
    public static final WeaponProgress ZERO = new WeaponProgress(0, 0);

    public record Gain(WeaponProgress progress, int consumed) {
    }

    public record Allocation(List<Gain> gains, int remaining) {
    }

    public static Allocation distribute(int amount, List<WeaponProgress> recipients) {
        if (amount < 0) throw new IllegalArgumentException("Negative experience allocation");
        if (recipients.isEmpty()) return new Allocation(List.of(), amount);
        List<Gain> gains = new ArrayList<>();
        int consumed = 0;
        for (int i = 0; i < recipients.size(); i++) {
            var gain = recipients.get(i).gain(amount / recipients.size() + (i < amount % recipients.size() ? 1 : 0));
            gains.add(gain);
            consumed += gain.consumed();
        }
        return new Allocation(List.copyOf(gains), amount - consumed);
    }

    public WeaponProgress {
        if (level < 0 || level > MAX_LEVEL || experience < 0 || level == MAX_LEVEL && experience != 0
                || level < MAX_LEVEL && experience >= 7 + 2 * level)
            throw new IllegalArgumentException("Invalid weapon progress");
    }

    public int nextLevelExperience() {
        return level == MAX_LEVEL ? 0 : 7 + 2 * level;
    }

    public float fraction() {
        return level == MAX_LEVEL ? 1 : (float) experience / nextLevelExperience();
    }

    /**
     * Retains the legacy unpolarized curve and returns only experience actually absorbed.
     */
    public Gain gain(int amount) {
        if (amount <= 0 || level == MAX_LEVEL) return new Gain(this, 0);
        int remaining = 0;
        for (int l = level; l < MAX_LEVEL; l++) remaining += 7 + 2 * l;
        int consumed = Math.min(amount, remaining - experience);
        int exp = experience + consumed, next = level;
        while (next < MAX_LEVEL && exp >= 7 + 2 * next) {
            exp -= 7 + 2 * next;
            next++;
        }
        return new Gain(new WeaponProgress(next, next == MAX_LEVEL ? 0 : exp), consumed);
    }
}
