package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class ArmorTests {
    private static int checks;
    private ArmorTests() {}
    public static void run() {
        close(8D / 108, remaining(0, 100)); close(1, remaining(-10, -10));
        close(15D / 22, remaining(7, 0)); close(15D / 26, remaining(11, 0));
        close(15D / 27, remaining(12, 0)); close(.5, remaining(15, 0));
        close(3D / 14, remaining(20, 8)); close(6D / 35, remaining(20, 12));
        for (double armor : List.of(0D, 1e-12, .5, 15D, 20D, 100D, 2_147_483_647D, Double.MAX_VALUE)) {
            double previous = 1;
            for (double toughness : List.of(0D, 1D, 8D, 100D, 2_147_483_647D, Double.MAX_VALUE)) {
                double value = remaining(armor, toughness);
                check(Double.isFinite(value) && value >= 0 && value <= 1); check(value <= previous); previous = value;
                double whole = 100 * value, split = 30 * value + 70 * value;
                close(whole, split);
                for (double damage : List.of(0D, 1e-12, 1D, 1e6, (double) Float.MAX_VALUE))
                    check(Double.isFinite(damage * value) && damage * value >= 0 && damage * value <= damage);
            }
        }
        check(remaining(2_147_483_647D, 2_147_483_647D) > 0);
        close(1D / 3, ArmorRules.remaining(20, 4, 40, 4));
        close(5D / 28, ArmorRules.improvedRemaining(10, 20, 8)); close(3D / 14, ArmorRules.improvedRemaining(20, 20, 8));
        close(2D / 15, ArmorRules.improvedRemaining(10, 20, 12)); close(7D / 45, ArmorRules.improvedRemaining(20, 20, 12));
        check(ArmorRules.improvedRemaining(10, 1e6, 1e6) > 0);
        var penetration = new ArmorPenetration(10, .25, 5, .2);
        close(65, penetration.armor(100)); close(35, penetration.toughness(50));
        close(0, penetration.plus(new ArmorPenetration(0, .75, 0, .8)).armor(100));
        for (var mode : ArmorRules.Mode.values()) check(ArmorRules.Mode.parse(mode.key()) == mode);
        rejects(() -> ArmorRules.Mode.parse("other")); rejects(() -> new ArmorPenetration(-1, 0, 0, 0));
        rejects(() -> ArmorRules.improvedRemaining(Double.NaN, 20, 8));
        rejects(() -> remaining(Double.NaN, 0)); rejects(() -> remaining(0, Double.POSITIVE_INFINITY));
        rejects(() -> ArmorRules.remaining(20, 8, 0, 4)); rejects(() -> ArmorRules.remaining(20, 8, 10, Double.NaN));
        var profile = new WeaponProfile(1, 2, 1, 6, Map.of(DamageType.IMPACT, 1D));
        var sources = List.of(Map.of(Stats.DAMAGE, new Bonus(0, 1, List.of(), 0), "damage.heat", new Bonus(0, 0, List.of(), 0, 0, .9), "damage.slash", new Bonus(0, 10, List.of(), 0)));
        var snapshot = Combat.statusBases(100, profile, sources, 1, 0, .75);
        close(300, snapshot.get(DamageType.SLASH)); close(570, snapshot.get(DamageType.HEAT)); close(300, snapshot.get(DamageType.GAS));
        close(105, snapshot.get(DamageType.SLASH) * .35); close(285, snapshot.get(DamageType.HEAT) * .5);
        var strike = Combat.calculate(100, profile, sources, Map.of(DamageType.HEAT, .5), () -> .99);
        close(580, strike.damage());
        close(570, snapshot.get(DamageType.HEAT));
        System.out.println("Passed " + checks + " fixed armor, high-range precision and pre-defense DOT checks");
    }
    private static double remaining(double armor, double toughness) { return ArmorRules.remaining(armor, toughness, 15, 8); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-9); }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Armor contract failed at " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
