package yagen.waitmydawn.yagens_attributes.core.loot;

import yagen.waitmydawn.yagens_attributes.core.ModDefinition;
import java.util.*;

/** Immutable authoring data; native serializers and registry access remain platform concerns. */
public record LootRule(String id, String sourceTable, String entity, boolean replace, List<Pool> pools) {
    public LootRule { ModDefinition.id(id); ModDefinition.id(sourceTable); if (!entity.isEmpty()) ModDefinition.id(entity); pools = List.copyOf(pools); }
    public record Range(int min, int max) {
        public Range { if (min < 0 || max < min || max > 4096) throw new IllegalArgumentException("Invalid loot range"); }
    }
    public sealed interface Condition permits PlayerKill, Chance, EnchantedChance {}
    public record PlayerKill() implements Condition {}
    public record Chance(double value) implements Condition { public Chance { probability(value); } }
    public record EnchantedChance(String enchantment, double unenchanted, double firstLevel, double perFurtherLevel) implements Condition {
        public EnchantedChance { ModDefinition.id(enchantment); probability(unenchanted); probability(firstLevel); probability(perFurtherLevel); }
    }
    public sealed interface Function permits Count, EnchantedCount {}
    public record Count(Range range) implements Function { public Count { Objects.requireNonNull(range); } }
    public record EnchantedCount(String enchantment, Range range, int limit) implements Function {
        public EnchantedCount { ModDefinition.id(enchantment); Objects.requireNonNull(range); if (limit < 0) throw new IllegalArgumentException("Negative count limit"); }
    }
    public sealed interface Output permits Item, Mod {}
    public record Item(String id) implements Output { public Item { ModDefinition.id(id); } }
    public record Mod(ModReward reward) implements Output { public Mod { Objects.requireNonNull(reward); } }
    public record Entry(Output output, Integer weight, List<Condition> conditions, List<Function> functions) {
        public Entry { Objects.requireNonNull(output); if (weight != null && weight < 0) throw new IllegalArgumentException("Negative weight"); conditions = List.copyOf(conditions); functions = List.copyOf(functions); }
        public Entry weight(int value) { return new Entry(output, value, conditions, functions); }
        public Entry when(Condition condition) { return new Entry(output, weight, append(conditions, condition), functions); }
        public Entry count(int min, int max) { return function(new Count(new Range(min, max))); }
        public Entry function(Function function) { return new Entry(output, weight, conditions, append(functions, function)); }
    }
    public record Pool(Range rolls, List<Entry> entries, List<Condition> conditions) {
        public Pool { Objects.requireNonNull(rolls); entries = List.copyOf(entries); conditions = List.copyOf(conditions); }
        public Pool rolls(int min, int max) { return new Pool(new Range(min, max), entries, conditions); }
        public Pool chance(double probability) { return new Pool(rolls, entries, append(conditions, new Chance(probability))); }
    }
    public static Entry item(String id) { return new Entry(new Item(id), null, List.of(), List.of()); }
    public static Entry mod(ModReward reward) { return new Entry(new Mod(reward), null, List.of(), List.of()); }
    public static Pool pool(Entry... entries) { return new Pool(new Range(1, 1), List.of(entries), List.of()); }
    private static <T> List<T> append(List<T> values, T value) { var result = new ArrayList<>(values); result.add(Objects.requireNonNull(value)); return List.copyOf(result); }
    private static void probability(double value) { if (!Double.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("Probability must be 0–1"); }
}
