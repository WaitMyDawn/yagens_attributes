package yagen.waitmydawn.yagens_attributes.core;

public final class StatusTests {
    private static int checks;
    private StatusTests() {}
    public static void run() {
        close(16, ShieldRules.capacity(4)); close(0, ShieldRules.capacity(0)); close(ShieldRules.MAX, ShieldRules.capacity(ShieldRules.MAX));
        close(.25, ShieldRules.gateSeconds(0)); close(.4, ShieldRules.gateSeconds(20)); close(.5, ShieldRules.gateSeconds(ShieldRules.MAX));
        double previous = 0;
        for (double shield : new double[]{0, .1, 1, 4, 10, 20, 21, 30, 100, 1000, 100000, ShieldRules.MAX}) {
            double duration = ShieldRules.gateSeconds(shield);
            check(duration >= previous && duration >= .25 && duration <= .5);
            check(ShieldRules.gateTicks(shield) >= 5 && ShieldRules.gateTicks(shield) <= 10); previous = duration;
        }
        var hit = ShieldRules.resolve(10, 0, 4, 1, 4.25, false, false);
        close(4, hit.shieldSpent()); close(25.5, hit.healthDamage()); check(hit.broken() && !hit.gated());
        hit = ShieldRules.resolve(10, 0, 4, 1, 4.25, true, false);
        close(0, hit.healthDamage()); check(hit.gated());
        hit = ShieldRules.resolve(2, 0, 4, 4.25, 4.25, false, false);
        close(4, hit.shieldSpent()); close(4.5, hit.healthDamage());
        hit = ShieldRules.resolve(1, 3, 4, 4.25, 2, true, false);
        close(4, hit.shieldSpent()); close(6, hit.healthDamage()); check(hit.gated());
        hit = ShieldRules.resolve(0, 3, 4, 4.25, 2, false, true);
        close(0, hit.shieldSpent()); close(6, hit.healthDamage()); check(!hit.broken());
        hit = ShieldRules.resolve(3, 0, 0, 1, 1, false, true); close(0, hit.healthDamage());
        hit = ShieldRules.resolve(2, 10, 0, 4, 1, 1, false, false);
        close(4, hit.shieldSpent()); close(1.2, hit.healthDamage());
        hit = ShieldRules.resolve(2, 10, 0, 4, 4.25, 4.25, false, false);
        close(4, hit.shieldSpent()); close(7.7, hit.healthDamage());
        var charge = new ShieldRules.Recharge(16_777_216F, 0);
        for (int i = 0; i < 40; i++) charge = ShieldRules.recharge(charge.shield(), 20_000_000, 2, charge.carry());
        close(16_777_220, charge.shield());
        charge = new ShieldRules.Recharge(0, 0);
        for (int i = 0; i < 100; i++) charge = ShieldRules.recharge(charge.shield(), 4, 2, charge.carry());
        close(4, charge.shield()); close(0, charge.carry());
        close(.26, StatusRules.corrosive(1)); close(.8, StatusRules.corrosive(10));
        close(.1, (1 - StatusRules.corrosive(10)) * (1 - .5));
        close(.2, StatusRules.puncture(5)); close(.6, StatusRules.puncture(1));
        close(2, StatusRules.viral(1)); close(4.25, StatusRules.viral(10)); close(1, StatusRules.viral(0));
        close(.5, StatusRules.coldSlow(1, false)); close(.9, StatusRules.coldSlow(9, false)); close(.4, StatusRules.coldSlow(10, true));
        close(.5, StatusRules.coldCritical(9, false)); close(1, StatusRules.coldCritical(10, true));
        close(6.5, StatusRules.radiation(10)); close(.35, StatusRules.coefficient(DamageType.SLASH));
        close(.5, StatusRules.coefficient(DamageType.TOXIN)); close(.3, StatusRules.coefficient(DamageType.BLAST));
        check(StatusRules.duration(DamageType.BLAST, 120) == 30); check(StatusRules.duration(DamageType.CORROSIVE, 120) == 160);
        check(StatusRules.duration(DamageType.RADIATION, 120) == 240); check(StatusRules.duration(DamageType.SLASH, 240) == 240);
        check(StatusRules.cap(DamageType.COLD, true) == 4); check(StatusRules.cap(DamageType.COLD, false) == 10);
        var profile = new WeaponProfile(.1, 2, 0, 6, java.util.Map.of(DamageType.IMPACT, 1D));
        var result = Combat.calculate(10, profile, java.util.List.of(), java.util.Map.of(), () -> .2, .15, 1);
        check(result.criticalTier() == 1); close(30, result.damage());
        System.out.println("Passed " + checks + " shield/status rule checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Status/shield rule failed"); }
    private static void close(double expected, double actual) { checks++; if (Math.abs(expected - actual) > Math.max(1e-8, Math.abs(expected) * 1e-12)) throw new AssertionError(expected + " != " + actual); }
}
