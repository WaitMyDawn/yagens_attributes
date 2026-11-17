package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Detached display data; deadlines use the authoritative overworld clock. */
public record WeaponBuff(UUID mod, int index, String type, int rank, String stat, double addition,
                         boolean perTargetStatus, int duration, int maximum, ConditionalModifier.Decay decay, List<Long> deadlines) {
    public WeaponBuff {
        Objects.requireNonNull(mod); Objects.requireNonNull(decay); ModDefinition.id(type);
        deadlines = List.copyOf(deadlines);
        if (index < 0 || index >= 128 || rank < 0 || rank > 60 || !Stats.KEYS.contains(stat) || !Double.isFinite(addition)
                || duration < 1 || duration > 72000 || maximum < 1 || maximum > 64 || deadlines.isEmpty() || deadlines.size() > maximum
                || deadlines.stream().anyMatch(value -> value < 0)) throw new IllegalArgumentException("Invalid weapon buff");
    }
    public int count(long now) { return (int) deadlines.stream().filter(value -> value > now).count(); }
    public long next(long now) { return deadlines.stream().filter(value -> value > now).mapToLong(Long::longValue).min().orElse(now); }
    public float fraction(long now) { return (float) Math.max(0, Math.min(1, (next(now) - now) / (double) duration)); }
}
