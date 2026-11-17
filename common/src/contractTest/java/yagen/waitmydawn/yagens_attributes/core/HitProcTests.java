package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.Category;

final class HitProcTests {
    private static int checks;
    private HitProcTests() {}
    static void run() {
        var catalog = BuiltinMods.catalog();
        for (String id : List.of("point_strike", "vital_sense", "hunter_munitions", "critical_delay")) {
            var info = catalog.card("yagens_attributes:" + id, 0).info();
            check(info.compatible(Category.BOW)); check(info.compatible(Category.CROSSBOW));
            check(!info.compatible(Category.TRIDENT)); check(!info.compatible(Category.SWORD));
        }
        for (String id : List.of("bone_finger_box", "nameless_glove", "true_punishment")) {
            var info = catalog.card("yagens_attributes:" + id, 0).info();
            check(info.compatible(Category.TRIDENT)); check(info.compatible(Category.SWORD));
            check(!info.compatible(Category.BOW));
        }
        check(catalog.card("yagens_attributes:point_strike", 5).info().conflicts(catalog.card("yagens_attributes:critical_delay", 5).info(), Category.BOW));
        var punishment = catalog.card("yagens_attributes:true_punishment", 3).bonuses();
        close(2, Stats.resolve(1, Stats.COMBO_COUNT_CHANCE, List.of(punishment)));
        close(2.5, Stats.resolve(5, Stats.COMBO_DURATION, List.of(punishment)));
        close(8.5, Stats.resolve(5, Stats.COMBO_DURATION, List.of(punishment, catalog.card("yagens_attributes:body_count", 5).bonuses())));
        var hunter = catalog.card("yagens_attributes:hunter_munitions", 5).bonuses();
        close(.3, Stats.resolve(0, Stats.CRITICAL_SLASH_CHANCE, List.of(hunter), 1));
        var crit = new WeaponProfile(1, 2, 0, 6, Map.of(DamageType.IMPACT, 1D));
        check(Combat.calculate(10, crit, List.of(hunter), Map.of(), () -> .299).procs().equals(List.of(DamageType.SLASH)));
        check(Combat.calculate(10, crit, List.of(hunter), Map.of(), () -> .301).procs().isEmpty());
        var noncrit = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.IMPACT, 1D));
        check(Combat.calculate(10, noncrit, List.of(hunter), Map.of(), () -> { throw new AssertionError("Noncritical hit rolled an extra proc"); }).procs().isEmpty());
        check(Combat.calculate(0, crit, List.of(hunter), Map.of(), () -> { throw new AssertionError("Zero damage rolled an extra proc"); }).procs().isEmpty());
        var slash = new WeaponProfile(3, 2, 1, 6, Map.of(DamageType.SLASH, 1D));
        check(Combat.calculate(10, slash, List.of(hunter), Map.of(), () -> .1).procs().equals(List.of(DamageType.SLASH, DamageType.SLASH)));
        close(80, Combat.statusBases(10, slash, List.of(Map.of(Stats.DAMAGE, new Bonus(0, 1, List.of(), 0))), 3, 0, 1).get(DamageType.SLASH));
        int[] rolls = {0}; var procs = new ArrayList<DamageType>();
        HitProcs.append(procs, 3, List.of(hunter), () -> { rolls[0]++; return .1; }); check(rolls[0] == 1 && procs.size() == 1);
        var guaranteed = new ArrayList<DamageType>();
        HitProcs.append(guaranteed, 1, Collections.nCopies(8, hunter), () -> { throw new AssertionError("Guaranteed chance consumed randomness"); });
        check(guaranteed.equals(List.of(DamageType.SLASH)));
        UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(), id = UUID.randomUUID();
        var ledger = new ModLedger(); var bone = catalog.card("yagens_attributes:bone_finger_box", 5);
        ledger.add(new ModLedger.Entry(id, owner, weapon, false, bone.bonuses(), bone.info(), 0));
        check(ledger.bonuses(weapon, "odd:irregular", Category.BOW).isEmpty());
        check(ledger.bonuses(weapon, "odd:irregular", Category.SWORD).size() == 1);
        check(ledger.bonuses(weapon, "odd:irregular", Category.BOW).isEmpty());
        System.out.println("Passed " + checks + " scoped Mod, unit-probability and critical-hit proc checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Hit proc check " + checks); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-9); }
}
