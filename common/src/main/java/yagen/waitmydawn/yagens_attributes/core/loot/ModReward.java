package yagen.waitmydawn.yagens_attributes.core.loot;

import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;
import java.util.function.*;

/** Selection parameters are independent of loot contexts and physical item representation. */
public record ModReward(Method method, Map<ModInfo.Rarity, Double> qualityWeights, String mod, int minPercent, int maxPercent) {
    public enum Method { WEIGHTED, SPECIFIC, VEILED_RIVEN, GALVANIZED }
    public static final Set<ModInfo.Rarity> WEIGHTED_QUALITIES = Set.of(ModInfo.Rarity.COMMON, ModInfo.Rarity.UNCOMMON, ModInfo.Rarity.RARE, ModInfo.Rarity.LEGENDARY);
    public ModReward {
        Objects.requireNonNull(method); Objects.requireNonNull(mod); qualityWeights = Map.copyOf(qualityWeights);
        if (minPercent < 0 || maxPercent > 100 || minPercent > maxPercent) throw new IllegalArgumentException("Rank percentages must satisfy 0 <= min <= max <= 100");
        if (method == Method.SPECIFIC) ModDefinition.id(mod);
        else if (!mod.isEmpty()) throw new IllegalArgumentException("Only the specific method accepts a Mod ID");
        double total = 0;
        for (var entry : qualityWeights.entrySet()) {
            if (!WEIGHTED_QUALITIES.contains(entry.getKey()) || !Double.isFinite(entry.getValue()) || entry.getValue() < 0 || entry.getValue() > 1_000_000) throw new IllegalArgumentException("Invalid quality weight");
            total += entry.getValue();
        }
        if (method == Method.WEIGHTED ? total <= 0 : !qualityWeights.isEmpty()) throw new IllegalArgumentException("Quality weights are required only by the weighted method");
        if (method == Method.VEILED_RIVEN && (minPercent != 0 || maxPercent != 100)) throw new IllegalArgumentException("Veiled Rivens have no rank parameters");
    }
    public int rank(int maximum, double random) {
        if (maximum < 0 || !Double.isFinite(random) || random < 0 || random > 1) throw new IllegalArgumentException("Invalid rank sample");
        return (int)Math.max(0, Math.min(maximum, Math.round(maximum * (minPercent + random * (maxPercent - minPercent)) / 100D)));
    }
    public Optional<String> select(Map<ModInfo.Rarity, List<String>> pools, IntUnaryOperator randomInt, DoubleSupplier randomDouble) {
        if (method == Method.SPECIFIC) return Optional.of(mod);
        if (method == Method.VEILED_RIVEN) throw new IllegalStateException("Veiled Rivens use weapon-pool weights");
        if (method == Method.GALVANIZED) { var ids = pools.getOrDefault(ModInfo.Rarity.GALVANIZED, List.of()); return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(randomInt.applyAsInt(ids.size()))); }
        double total = qualityWeights.entrySet().stream().filter(e -> !pools.getOrDefault(e.getKey(), List.of()).isEmpty()).mapToDouble(Map.Entry::getValue).sum();
        if (total <= 0) return Optional.empty(); double roll = randomDouble.getAsDouble() * total;
        for (var rarity : ModInfo.Rarity.values()) {
            var ids = pools.getOrDefault(rarity, List.of()); double weight = ids.isEmpty() ? 0 : qualityWeights.getOrDefault(rarity, 0D);
            if (weight > 0) { if (roll < weight) return Optional.of(ids.get(randomInt.applyAsInt(ids.size()))); roll -= weight; }
        }
        return Optional.empty();
    }
    public static double meleeProbability(int melee, int ranged) {
        if (melee < 0 || ranged < 0) throw new IllegalArgumentException("Negative weapon-pool size");
        if (melee == 0 && ranged == 0) return Double.NaN;
        double m = melee <= 1 ? 0 : Math.log(melee), r = ranged <= 1 ? 0 : Math.log(ranged);
        if (m + r == 0) return melee == 0 ? 0 : ranged == 0 ? 1 : .5;
        return m / (m + r);
    }
}
