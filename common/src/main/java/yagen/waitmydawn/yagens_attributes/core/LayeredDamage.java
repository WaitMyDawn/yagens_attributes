package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** A mixed hit spends shield proportionally, then applies life material only to the surviving life portion. */
public final class LayeredDamage {
    private LayeredDamage() {}
    public static ShieldRules.Hit resolve(double armored, double unarmored, Map<DamageType, Double> components,
                                         Map<DamageType, Double> life, Map<DamageType, Double> shieldTypes,
                                         double shield, double magnetic, double viral, boolean armed, boolean protectedHealth) {
        for (double number : new double[]{armored, unarmored, shield, magnetic, viral}) if (!Double.isFinite(number) || number < 0) throw new IllegalArgumentException("Invalid layered damage");
        double total = components.values().stream().mapToDouble(Double::doubleValue).sum();
        if (!Double.isFinite(total) || total <= 0) throw new IllegalArgumentException("Empty typed damage");
        double demand = 0, normalLife = 0, toxinLife = 0;
        for (var entry : components.entrySet()) {
            double weight = entry.getValue() / total;
            double healthFactor = life.getOrDefault(entry.getKey(), 1D), shieldFactor = shieldTypes.getOrDefault(entry.getKey(), 1D);
            if (!Double.isFinite(weight) || weight < 0 || !Double.isFinite(healthFactor) || healthFactor < 0 || !Double.isFinite(shieldFactor) || shieldFactor < 0) throw new IllegalArgumentException("Invalid material factor");
            double health = scaled(armored * weight, healthFactor);
            if (entry.getKey() == DamageType.TOXIN) toxinLife += health;
            else { demand += scaled(scaled(unarmored * weight, shieldFactor), magnetic); normalLife += health; }
        }
        if (protectedHealth) { demand = 0; normalLife = 0; }
        double spent = Math.min(shield, demand);
        boolean broken = shield > 0 && spent >= shield, gated = broken && armed;
        double overflow = shield <= 0 ? 1 : demand <= 0 ? 0 : Math.max(0, 1 - shield / demand);
        double health = (toxinLife + (gated || protectedHealth ? 0 : normalLife * overflow)) * viral;
        return new ShieldRules.Hit(spent, Math.min(Float.MAX_VALUE, health), broken, gated);
    }
    private static double scaled(double value, double factor) {
        if (value == 0 || factor == 0) return 0;
        return value > Float.MAX_VALUE / factor ? Float.MAX_VALUE : value * factor;
    }
}
