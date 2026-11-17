package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class RangedTests {
    private static int checks;
    private RangedTests() {}
    public static void run() {
        var single = RangedStats.volley(1.5, () -> .9); equal(1, single.count()); close(1.5, single.damagePerProjectile());
        var doubleShot = RangedStats.volley(1.5, () -> .1); equal(2, doubleShot.count()); close(.75, doubleShot.damagePerProjectile());
        int extra = 0;
        for (int i = 0; i < 100; i++) {
            double sample = i / 100D;
            var volley = RangedStats.volley(1.5, () -> sample); if (volley.count() == 2) extra++;
            close(1.5, volley.count() * volley.damagePerProjectile());
        }
        equal(50, extra);
        equal(0, RangedStats.volley(0, () -> { throw new AssertionError("Unexpected RNG"); }).count());
        close(.25, RangedStats.volley(.25, () -> { throw new AssertionError("Unexpected RNG"); }).damagePerProjectile());
        equal(64, RangedStats.volley(64, () -> { throw new AssertionError("Unexpected RNG"); }).count());
        rejects(() -> RangedStats.volley(Double.NaN, () -> 0)); rejects(() -> RangedStats.volley(-1, () -> 0)); rejects(() -> RangedStats.volley(65, () -> 0));
        for (double rate : List.of(.1, .5, 1D, 1.5, 2D, 3D, 64D)) {
            for (int required : List.of(10, 20, 25)) {
                var clock = new RangedStats.ChargeClock(rate);
                int remaining = 72000, ticks = 0;
                while (72000 - remaining < required && ticks < 1000) { remaining = clock.beforeTick(remaining) - 1; ticks++; }
                equal((int) Math.ceil(required / rate), ticks);
            }
        }
        var stopped = new RangedStats.ChargeClock(0); int remaining = 72000;
        for (int i = 0; i < 40; i++) remaining = stopped.beforeTick(remaining) - 1;
        equal(72000, remaining);
        var first = new RangedStats.ChargeClock(.5); var second = new RangedStats.ChargeClock(.5);
        equal(first.beforeTick(72000), second.beforeTick(72000));
        var corrected = new RangedStats.ChargeClock(1); remaining = 72000;
        for (int i = 0; i < 5; i++) remaining = corrected.beforeTick(remaining) - 1;
        corrected.rate(2); remaining = corrected.beforeTick(remaining) - 1; equal(12, 72000 - remaining);
        corrected.rate(.5); remaining = corrected.beforeTick(remaining) - 1; equal(3, 72000 - remaining);
        rejects(() -> new RangedStats.ChargeClock(Double.POSITIVE_INFINITY)); rejects(() -> new RangedStats.ChargeClock(-1));
        var sources = List.of(Map.of(Stats.MULTISHOT, new Bonus(0, .2, List.of(1.5), 0)), Map.of(Stats.MULTISHOT, new Bonus(0, .4, List.of(1.2), 0)));
        close(2.88, RangedStats.value(1, Stats.MULTISHOT, sources));
        close(64, RangedStats.value(1, Stats.FIRE_RATE, List.of(Map.of(Stats.FIRE_RATE, new Bonus(1000, 0, List.of(), 0)))));
        for (double rate : List.of(0D, .1, .5, 1D, 1.5, 2D, 64D)) {
            var cadence = new RangedStats.IntervalClock(); int emitted = 0;
            for (int tick = 1; tick <= 1000; tick++) {
                emitted += cadence.advance(rate, 2);
                equal((int) Math.floor(Math.nextUp(tick * rate / 2)), emitted);
            }
        }
        var cadence = new RangedStats.IntervalClock();
        equal(0, cadence.advance(.5, 2)); equal(0, cadence.advance(0, 2));
        equal(1, cadence.advance(1.5, 2)); equal(0, cadence.advance(1, 2)); equal(1, cadence.advance(1, 2));
        rejects(() -> cadence.advance(Double.NaN, 2)); rejects(() -> cadence.advance(-1, 2));
        rejects(() -> cadence.advance(65, 2)); rejects(() -> cadence.advance(1, 0));
        System.out.println("Passed " + checks + " ranged math/charge contract checks");
    }
    private static void equal(Object a, Object b) { if (!Objects.equals(a, b)) throw new AssertionError(a + " != " + b); checks++; }
    private static void close(double a, double b) { if (Math.abs(a - b) > 1e-9) throw new AssertionError(a + " != " + b); checks++; }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
