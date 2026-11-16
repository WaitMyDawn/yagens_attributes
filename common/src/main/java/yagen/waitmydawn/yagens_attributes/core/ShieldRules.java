package yagen.waitmydawn.yagens_attributes.core;

/**
 * Absorption is the single shield resource; health-only damage never spends it.
 */
public final class ShieldRules {
    public static final double MAX = 2_147_483_647D;

    private ShieldRules() {
    }

    public static double capacity(double natural) {
        return Math.min(MAX, Math.max(0, natural) * 4);
    }

    public static double gateSeconds(double before) {
        double shield = Math.max(0, Math.min(MAX, before));
        double extra = shield <= 20 ? .15 * shield / 20
                : .15 + .1 * ((shield - 20) / (shield + 0D)) / ((MAX - 20) / MAX);
        return .25 + extra;
    }

    public static int gateTicks(double before) {
        return Math.max(5, Math.min(10, (int) Math.ceil(gateSeconds(before) * 20 - 1e-12)));
    }

    public record Recharge(float shield, double carry) {
    }

    public static Recharge recharge(float shield, double maximum, double perSecond, double carry) {
        double available = Math.max(0, Math.min(maximum - shield, carry + perSecond / 20));
        float next = (float) (shield + available);
        if (next - shield > available + 1e-12) next = Math.nextDown(next);
        next = Math.max(shield, next);
        return new Recharge(next, available >= maximum - shield ? 0 : Math.max(0, available - (next - shield)));
    }

    public record Hit(double shieldSpent, double healthDamage, boolean broken, boolean gated) {
    }

    public static Hit resolve(double normal, double healthOnly, double shield, double shieldMultiplier,
                              double healthMultiplier, boolean armed, boolean protectedHealth) {
        return resolve(normal, normal, healthOnly, shield, shieldMultiplier, healthMultiplier, armed, protectedHealth);
    }

    public static Hit resolve(double normal, double shieldInput, double healthOnly, double shield, double shieldMultiplier,
                              double healthMultiplier, boolean armed, boolean protectedHealth) {
        if (!Double.isFinite(normal) || !Double.isFinite(healthOnly) || normal < 0 || healthOnly < 0
                || !Double.isFinite(shieldInput) || shieldInput < 0
                || !Double.isFinite(shield) || shield < 0 || !Double.isFinite(shieldMultiplier) || shieldMultiplier < 1
                || !Double.isFinite(healthMultiplier) || healthMultiplier < 1)
            throw new IllegalArgumentException("Invalid shield hit");
        double spent = Math.min(shield, shieldInput * shieldMultiplier);
        boolean broken = shield > 0 && spent >= shield;
        boolean gated = broken && armed;
        double overflow = shieldInput <= 0 ? normal : normal * Math.max(0, 1 - shield / (shieldInput * shieldMultiplier));
        double health = (healthOnly + (gated || protectedHealth ? 0 : overflow)) * healthMultiplier;
        return new Hit(spent, Math.min(Float.MAX_VALUE, health), broken, gated);
    }
}
