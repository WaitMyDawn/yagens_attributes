package yagen.waitmydawn.yagens_attributes.core;

import java.util.List;
import java.util.Objects;

public final class PolarityRules {
    public static final List<ModInfo.Polarity> FORMA_CHOICES = List.of(ModInfo.Polarity.MADURAI, ModInfo.Polarity.VAZARIN, ModInfo.Polarity.NARAMON);

    private PolarityRules() {}

    public static ModInfo.Polarity slot(List<ModInfo.Polarity> slots, int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : ModInfo.Polarity.NONE;
    }

    public static int cost(ModInfo info, ModInfo.Polarity slot) {
        Objects.requireNonNull(slot);
        int base = info.capacityCost();
        if (slot == ModInfo.Polarity.NONE || info.polarity() == ModInfo.Polarity.NONE) return base;
        return slot.matches(info.polarity()) ? (base + 1) / 2 : base + (base + 3) / 4;
    }

    public static int movedCapacity(int used, ModInfo source, int from, ModInfo target, int to, List<ModInfo.Polarity> slots) {
        int next = used - cost(source, slot(slots, from)) + cost(source, slot(slots, to));
        if (target != null) next += cost(target, slot(slots, from)) - cost(target, slot(slots, to));
        return next;
    }

    public static void validateForma(WeaponProgress progress, List<ModInfo.Polarity> slots, int slot, ModInfo.Polarity polarity) {
        validateForma(progress, slots, slot, polarity, false);
    }

    public static void validateForma(WeaponProgress progress, List<ModInfo.Polarity> slots, int slot, ModInfo.Polarity polarity, boolean creative) {
        if (!creative && progress.level() != WeaponProgress.MAX_LEVEL) throw new IllegalArgumentException("gui.yagens_attributes.polarize_requires_max");
        if (slot < 0 || slot >= slots.size() || !FORMA_CHOICES.contains(polarity)) throw new IllegalArgumentException("gui.yagens_attributes.polarize_invalid");
        if (slots.get(slot) == polarity) throw new IllegalArgumentException("gui.yagens_attributes.polarize_unchanged");
    }
}
