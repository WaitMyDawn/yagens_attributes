package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public record RivenRoll(String weapon, ModInfo.Category category, ModInfo.Polarity polarity,
                        double disposition, List<Line> lines, int cycles) {
    public RivenRoll(String weapon, ModInfo.Category category, ModInfo.Polarity polarity, double disposition, List<Line> lines) {
        this(weapon, category, polarity, disposition, lines, 0);
    }

    public enum Pool {
        MELEE(ModInfo.Category.MELEE), RANGED(ModInfo.Category.RANGED);
        private final ModInfo.Category category;

        Pool(ModInfo.Category category) {
            this.category = category;
        }

        public boolean accepts(ModInfo.Category value) {
            return category.contains(value);
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Line(String trait, double quality, boolean negative) {
        public Line {
            Objects.requireNonNull(trait);
            if (trait.isBlank() || trait.length() > 80 || !Double.isFinite(quality) || quality < 0 || quality > 1)
                throw new IllegalArgumentException("Invalid Riven line");
        }
    }

    public RivenRoll {
        ModDefinition.id(weapon);
        Objects.requireNonNull(category);
        Objects.requireNonNull(polarity);
        lines = List.copyOf(lines);
        if (cycles < 0 || !ModInfo.Category.MELEE.contains(category) && !ModInfo.Category.RANGED.contains(category)
                || !PolarityRules.FORMA_CHOICES.contains(polarity) || !Double.isFinite(disposition) || disposition != 0 && disposition < .5 || disposition > 1.55)
            throw new IllegalArgumentException("Invalid Riven specialization");
        long negatives = lines.stream().filter(Line::negative).count(), positives = lines.size() - negatives;
        if (positives < 2 || positives > 3 || negatives > 1 || lines.stream().map(Line::trait).distinct().count() != lines.size())
            throw new IllegalArgumentException("Invalid Riven layout");
    }

    public RivenRoll withDisposition(double value) {
        return new RivenRoll(weapon, category, polarity, value, lines, cycles);
    }

    public RivenRoll withCycles(int value) {
        return new RivenRoll(weapon, category, polarity, disposition, lines, value);
    }

    public int cycleCost() {
        return (int) Math.min(16L, (long) cycles + 1);
    }
}
