package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Ordered source provenance controls pairing; coefficients are still resolved in their original modifier zones. */
public final class ElementFusion {
    public static final List<DamageType> PRIMARY = List.of(DamageType.COLD, DamageType.HEAT, DamageType.ELECTRICITY, DamageType.TOXIN);
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
        var order = new LinkedHashSet<DamageType>();
        for (var source : sources) for (String key : source.keySet()) for (var type : PRIMARY)
            if (key.equals("damage." + type.key()) && amounts.getOrDefault(type, 0D) > 0 && !source.get(key).equals(Bonus.NONE)) order.add(type);
        for (var type : PRIMARY) if (amounts.getOrDefault(type, 0D) > 0) order.add(type);
        var result = new EnumMap<DamageType, Double>(DamageType.class); result.putAll(amounts);
        var elements = List.copyOf(order);
        for (int i = 0; i + 1 < elements.size(); i += 2) {
            var a = elements.get(i); var b = elements.get(i + 1); var combined = combine(a, b);
            result.merge(combined, result.get(a) + result.get(b), Double::sum); result.put(a, 0D); result.put(b, 0D);
        }
        return Collections.unmodifiableMap(result);
    }
}
