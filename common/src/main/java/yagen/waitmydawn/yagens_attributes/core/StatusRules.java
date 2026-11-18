package yagen.waitmydawn.yagens_attributes.core;

public final class StatusRules {
    private StatusRules() {}
    public static int cap(DamageType type, boolean player) {
        return switch (type) {
            case IMPACT, PUNCTURE -> 5;
            case COLD -> player ? 4 : 10;
            case RADIATION, CORROSIVE, VIRAL, MAGNETIC, GAS, BLAST -> 10;
            default -> 1024;
        };
    }
    public static int duration(DamageType type, int weaponTicks) {
        double base = switch (type) { case RADIATION -> 12; case CORROSIVE -> 8; case BLAST -> 1.5; default -> 6; };
        return Math.max(1, (int) Math.min(144000, Math.ceil(weaponTicks * base / 6)));
    }
    public static double viral(int stacks) { return stacks <= 0 ? 1 : 2 + .25 * (Math.min(10, stacks) - 1); }
    public static double corrosive(int stacks) { return stacks <= 0 ? 0 : .26 + .06 * (Math.min(10, stacks) - 1); }
    public static double puncture(int stacks) { return stacks <= 0 ? 1 : 1 - (.4 + .1 * (Math.min(5, stacks) - 1)); }
    public static double coldSlow(int stacks, boolean player) {
        return stacks <= 0 ? 0 : player ? .1 * Math.min(4, stacks) : Math.min(.9, .5 + .05 * (stacks - 1));
    }
    public static double coldCritical(int stacks, boolean frozen) { return frozen ? 1 : stacks <= 0 ? 0 : .1 + .05 * (Math.min(9, stacks) - 1); }
    public static double radiation(int stacks) { return stacks <= 0 ? 1 : 2 + .5 * (Math.min(10, stacks) - 1); }
    public static double gasRadius(int stacks) { return stacks <= 0 ? 0 : 3 + (Math.min(10, stacks) - 1) / 3D; }
    public static double coefficient(DamageType type) { return type == DamageType.SLASH ? .35 : type == DamageType.BLAST ? .3 : .5; }
}
