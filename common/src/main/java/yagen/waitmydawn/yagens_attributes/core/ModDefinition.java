package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public record ModDefinition(String name, ModInfo.Rarity rarity, ModInfo.Polarity polarity, int baseCapacity,
                            int capacityPerLevel, int maxLevel, Set<ModInfo.Category> applicability,
                            String texture, List<Effect> entries, ModStyle style) {
    public ModDefinition(String name, ModInfo.Rarity rarity, ModInfo.Polarity polarity, int baseCapacity,
                         int capacityPerLevel, int maxLevel, Set<ModInfo.Category> applicability, String texture, List<Effect> entries) {
        this(name, rarity, polarity, baseCapacity, capacityPerLevel, maxLevel, applicability, texture, entries, ModStyle.DEFAULT);
    }
    public ModDefinition withStyle(ModStyle value) { return new ModDefinition(name, rarity, polarity, baseCapacity, capacityPerLevel, maxLevel, applicability, texture, entries, value); }
    public enum Operation {ADDIN, ADDITION, MULTIPLY, ADDOUT}

    public enum Basis {FLAT, ATTACK_DAMAGE}

    public record Effect(String stat, Operation operation, double value, Basis basis, String description, ConditionalModifier.Condition condition) {
        public Effect(String stat, Operation operation, double value, Basis basis, String description) {
            this(stat, operation, value, basis, description, ConditionalModifier.Condition.ALWAYS);
        }
        public Effect {
            Objects.requireNonNull(condition);
            Objects.requireNonNull(operation);
            Objects.requireNonNull(basis);
            Objects.requireNonNull(description);
            if (!Stats.KEYS.contains(stat) || !Double.isFinite(value) || description.isBlank() || description.length() > 1024)
                throw new IllegalArgumentException("Invalid Mod effect");
            if (basis == Basis.ATTACK_DAMAGE && ((operation != Operation.ADDIN && operation != Operation.ADDITION) || !Stats.DAMAGE_COMPONENTS.contains(stat)))
                throw new IllegalArgumentException("Attack-damage basis requires a component addin or addition");
            if (condition.trigger() != ConditionalModifier.Trigger.ALWAYS && (operation != Operation.ADDITION || basis != Basis.FLAT))
                throw new IllegalArgumentException("Conditional effects require flat-basis Addition");
        }
    }

    public ModDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(rarity);
        Objects.requireNonNull(polarity);
        Objects.requireNonNull(style);
        applicability = Set.copyOf(applicability);
        entries = List.copyOf(entries);
        id(texture);
        if (name.isBlank() || name.length() > 80 || baseCapacity < 0 || capacityPerLevel < 0 || maxLevel < 0 || maxLevel > 60
                || (long) baseCapacity + (long) capacityPerLevel * maxLevel > 1_000_000 || applicability.isEmpty()
                || applicability.contains(ModInfo.Category.ALL) || entries.isEmpty() || entries.size() > 64)
            throw new IllegalArgumentException("Invalid Mod definition");
    }

    public static String id(String value) {
        if (value == null || value.length() > 256 || !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Invalid resource ID");
        return value;
    }

    public Map<String, Bonus> bonuses(int level) {
        if (level < 0 || level > maxLevel) throw new IllegalArgumentException("Invalid Mod level");
        var result = new LinkedHashMap<String, Bonus>();
        for (var entry : entries) {
            if (entry.condition().trigger() != ConditionalModifier.Trigger.ALWAYS) continue;
            double value = entry.value * (level + 1);
            var old = result.getOrDefault(entry.stat, Bonus.NONE);
            double in = old.addin(), addition = old.addition(), out = old.addout(), reference = old.inputAddin(), relativeAddition = old.inputAddition();
            var multiply = new ArrayList<>(old.multiply());
            switch (entry.operation) {
                case ADDIN -> {
                    if (entry.basis == Basis.ATTACK_DAMAGE) reference += value;
                    else in += value;
                }
                case ADDITION -> {
                    if (entry.basis == Basis.ATTACK_DAMAGE) relativeAddition += value;
                    else addition += value;
                }
                case MULTIPLY -> {
                    if (1 + value < 0) throw new IllegalArgumentException("Negative independent multiplier");
                    multiply.add(1 + value);
                }
                case ADDOUT -> out += value;
            }
            result.put(entry.stat, new Bonus(in, addition, multiply, out, reference, relativeAddition));
        }
        return Collections.unmodifiableMap(result);
    }

    public List<ConditionalModifier> conditional(int level) {
        if (level < 0 || level > maxLevel) throw new IllegalArgumentException("Invalid Mod level");
        return entries.stream().filter(entry -> entry.condition().trigger() != ConditionalModifier.Trigger.ALWAYS)
                .map(entry -> new ConditionalModifier(entry.stat(), entry.value() * (level + 1), entry.condition())).toList();
    }
}
