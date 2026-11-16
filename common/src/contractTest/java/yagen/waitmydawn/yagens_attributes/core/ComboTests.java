package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class ComboTests {
    private static int checks;
    private ComboTests() {}
    public static void run() {
        var rules = Combo.Rules.DEFAULT; var counter = Combo.Counter.EMPTY;
        for (int hit = 1; hit <= 260; hit++) {
            counter = counter.hit(hit, 100, rules);
            equal(Math.min(hit, 220), counter.count(hit, rules));
            equal(Math.min(12, 1 + hit / 20), rules.tier(counter.count(hit, rules)));
        }
        equal(220, counter.count(359, rules)); equal(0, counter.count(360, rules));
        equal(1, counter.hit(360, 100, rules).count(360, rules));
        var custom = new Combo.Rules(7, 4); counter = Combo.Counter.EMPTY;
        for (int i = 0; i < 100; i++) counter = counter.hit(i, 10, custom);
        equal(21, counter.count(99, custom)); equal(4, custom.tier(21));
        equal(Combo.Counter.EMPTY, counter.hit(100, 0, custom));
        rejects(() -> new Combo.Rules(0, 12)); rejects(() -> new Combo.Rules(20, 1));
        rejects(() -> Combo.Counter.EMPTY.hit(-1, 100, rules));
        var catalog = BuiltinMods.catalog();
        var body = catalog.card("yagens_attributes:body_count", 5).bonuses();
        var contact = catalog.card("yagens_attributes:drifting_contact", 3).bonuses();
        equal(340, Combo.duration(5, List.of(body))); equal(300, Combo.duration(5, List.of(contact)));
        equal(540, Combo.duration(5, List.of(body, contact))); equal(0, Combo.duration(0, List.of()));
        var rush = catalog.card("yagens_attributes:blood_rush", 10).bonuses();
        var wounds = catalog.card("yagens_attributes:weeping_wounds", 5).bonuses();
        var cc = catalog.card("yagens_attributes:bone_finger_box", 5).bonuses();
        close(.15, Stats.resolve(.15, Stats.CRITICAL_CHANCE, Combo.sources(List.of(rush), 1)));
        close(.21, Stats.resolve(.15, Stats.CRITICAL_CHANCE, Combo.sources(List.of(rush), 2)));
        close(.945, Stats.resolve(.15, Stats.CRITICAL_CHANCE, Combo.sources(List.of(rush, cc), 12)));
        close(.81, Stats.resolve(.15, Stats.STATUS_CHANCE, Combo.sources(List.of(wounds), 12)));
        var profile = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.IMPACT, 1D));
        close(10, Combat.calculate(10, profile, Combo.sources(List.of(), 12), Map.of(), () -> .99).damage());
        for (String id : List.of("cloudpiercing_flame", "shattered_venom_doll", "winter_talisman", "charge_cage")) {
            var card = catalog.card("yagens_attributes:" + id, 5);
            close(.6, card.bonuses().get(Stats.STATUS_CHANCE).addition());
            close(.6, card.bonuses().values().stream().mapToDouble(Bonus::inputAddition).sum());
        }
        close(1.3, Stats.resolve(1, Stats.ATTACK_SPEED, List.of(catalog.card("yagens_attributes:fury", 5).bonuses())));
        close(1.5, Stats.resolve(0, Stats.MELEE_RANGE, List.of(catalog.card("yagens_attributes:reach", 3).bonuses())));
        for (String id : List.of("body_count", "blood_rush", "weeping_wounds", "drifting_contact", "fury", "reach")) {
            var info = catalog.card("yagens_attributes:" + id, 0).info();
            equal(true, info.compatible(ModInfo.Category.SWORD)); equal(true, info.compatible(ModInfo.Category.TRIDENT)); equal(false, info.compatible(ModInfo.Category.BOW));
        }
        System.out.println("Passed " + checks + " combo thresholds/expiry/conditional stat and batch-two checks");
    }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
    private static void close(double expected, double actual) { checks++; if (Math.abs(expected - actual) > 1e-8) throw new AssertionError(expected + " != " + actual); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
