package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Ordered source provenance controls pairing; coefficients are still resolved in their original modifier zones. */
public final class ElementFusion {
    public static final List<DamageType> PRIMARY = List.of(DamageType.COLD, DamageType.HEAT, DamageType.ELECTRICITY, DamageType.TOXIN);
    private static final Map<String, DamageType> PRIMARY_STATS = PRIMARY.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(DamageType::statKey, type -> type));
    private ElementFusion() {}
    public static DamageType combine(DamageType a, DamageType b) {
        if (a == b || !PRIMARY.contains(a) || !PRIMARY.contains(b)) throw new IllegalArgumentException("Expected distinct primary elements");
        if (a == DamageType.COLD || b == DamageType.COLD) return switch (a == DamageType.COLD ? b : a) {
            case HEAT -> DamageType.BLAST; case ELECTRICITY -> DamageType.MAGNETIC; case TOXIN -> DamageType.VIRAL; default -> throw new IllegalArgumentException();
        };
        if (a == DamageType.HEAT || b == DamageType.HEAT) return a == DamageType.ELECTRICITY || b == DamageType.ELECTRICITY ? DamageType.RADIATION : DamageType.GAS;
        return DamageType.CORROSIVE;
    }
    public static Map<DamageType, Double> apply(Map<DamageType, Double> amounts, List<Map<String, Bonus>> sources) {
        var result = new EnumMap<DamageType, Double>(DamageType.class);
        result.putAll(amounts);
        combineInPlace(result, sources);
        return Collections.unmodifiableMap(result);
    }
    static void combineInPlace(EnumMap<DamageType, Double> amounts, List<Map<String, Bonus>> sources) {
        var order = new LinkedHashSet<DamageType>();
        for (var source : sources) {
            for (var entry : source.entrySet()) {
                var type = PRIMARY_STATS.get(entry.getKey());
                if (type != null && amounts.getOrDefault(type, 0D) > 0 && !entry.getValue().equals(Bonus.NONE)) order.add(type);
            }
        }
        for (var type : PRIMARY) if (amounts.getOrDefault(type, 0D) > 0) order.add(type);
        var elements = List.copyOf(order);
        for (int i = 0; i + 1 < elements.size(); i += 2) {
            var a = elements.get(i); var b = elements.get(i + 1); var combined = combine(a, b);
            amounts.merge(combined, amounts.get(a) + amounts.get(b), Double::sum);
            amounts.put(a, 0D);
            amounts.put(b, 0D);
        }
    }
}
