package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class RivenTests {
    private static int checks;
    private RivenTests() {}
    public static void run() {
        var catalog = BuiltinMods.catalog(); var rules = catalog.rivenRules(); var weapons = BuiltinCombat.weapons();
        check(rules.candidates(RivenRoll.Pool.MELEE, weapons).size() == 14);
        check(rules.candidates(RivenRoll.Pool.RANGED, weapons).equals(List.of("minecraft:bow", "minecraft:crossbow")));
        var source = new LinkedHashMap<>(weapons);
        source.put("odd:unrelated.alpha", weapons.get("minecraft:diamond_axe").withDisposition(0));
        source.put("odd:no_family_hint", weapons.get("minecraft:diamond_sword").withDisposition(.5));
        check(!rules.candidates(RivenRoll.Pool.MELEE, source).contains("odd:unrelated.alpha"));
        check(rules.candidates(RivenRoll.Pool.MELEE, source).contains("odd:no_family_hint"));
        for (var pool : RivenRoll.Pool.values()) {
            var candidates = rules.candidates(pool, source);
            for (int choice = 0; choice < candidates.size(); choice++) {
                int selected = choice; int[] calls = {0};
                var roll = rules.roll(pool, source, bound -> calls[0]++ == 0 ? selected : 0, () -> .5);
                check(roll.weapon().equals(candidates.get(choice)));
            }
        }
        var layouts = new HashSet<String>(); var polarities = new HashSet<ModInfo.Polarity>(); var random = new Random(9037);
        for (int i = 0; i < 128; i++) {
            var pool = i % 2 == 0 ? RivenRoll.Pool.MELEE : RivenRoll.Pool.RANGED;
            var roll = rules.roll(pool, weapons, random::nextInt, random::nextDouble);
            int negative = (int) roll.lines().stream().filter(RivenRoll.Line::negative).count();
            layouts.add((roll.lines().size() - negative) + "/" + negative); polarities.add(roll.polarity());
            var profile = weapons.get(roll.weapon()); var stats = new HashSet<String>();
            for (var line : roll.lines()) {
                var trait = rules.traits().get(line.trait());
                check(trait.accepts(profile) && stats.add(trait.stat()));
                check(!line.negative() || trait.negative());
            }
            var rank0 = catalog.rivenCard(roll, 0); var rank8 = catalog.rivenCard(roll, 8);
            check(rank0.info().capacityCost() == 10 && rank8.info().capacityCost() == 18 && rank8.info().details().maxLevel() == 8);
            check(rank0.info().riven().equals(rank8.info().riven()));
        }
        check(layouts.equals(Set.of("2/0", "3/0", "2/1", "3/1"))); check(polarities.equals(new HashSet<>(PolarityRules.FORMA_CHOICES)));
        var small = new LinkedHashMap<String, RivenRules.Trait>();
        small.put("a", rules.traits().get(Stats.CRITICAL_CHANCE)); small.put("b", rules.traits().get(Stats.STATUS_CHANCE));
        var duration = rules.traits().get(Stats.STATUS_DURATION);
        small.put("c", new RivenRules.Trait(duration.stat(), duration.operation(), duration.basis(), duration.applicability(), duration.meleeBase(), duration.rangedBase(), true, false, 0, duration.description()));
        var constrained = new RivenRules(10, 8, .9, 1.1, rules.layouts(), small, rules.texture());
        int[] calls = {0};
        var feasible = constrained.roll(RivenRoll.Pool.MELEE, weapons, bound -> calls[0]++ == 1 ? 2 : 0, () -> .5);
        check(feasible.lines().size() == 3 && feasible.lines().stream().anyMatch(line -> line.trait().equals("c")));
        var roll = new RivenRoll("odd:no_family_hint", ModInfo.Category.SWORD, ModInfo.Polarity.MADURAI, 1,
                List.of(new RivenRoll.Line(Stats.CRITICAL_CHANCE, .5, false), new RivenRoll.Line(Stats.STATUS_CHANCE, .5, false), new RivenRoll.Line(Stats.DAMAGE, 0, true)));
        var card = catalog.rivenCard(roll, 8);
        close(1.8 * 1.2375, card.bonuses().get(Stats.CRITICAL_CHANCE).addition());
        close(-1.647 * .495 * .9, card.bonuses().get(Stats.DAMAGE).addition());
        close(.15 * (1 + .2 + 1.8 * 1.2375), Stats.resolve(.15, Stats.CRITICAL_CHANCE, List.of(card.bonuses(), Map.of(Stats.CRITICAL_CHANCE, new Bonus(0, .2, List.of(), 0)))));
        close(card.bonuses().get(Stats.CRITICAL_CHANCE).addition() / 9, catalog.rivenCard(roll, 0).bonuses().get(Stats.CRITICAL_CHANCE).addition());
        var owner = UUID.randomUUID(); var weapon = UUID.randomUUID(); var id = UUID.randomUUID(); var ledger = new ModLedger();
        ledger.add(new ModLedger.Entry(id, owner, null, false, card.bonuses(), card.info(), -1));
        var slots = Collections.nCopies(8, ModInfo.Polarity.NONE);
        rejects(() -> ledger.install(owner, id, weapon, ModInfo.Category.SWORD, 8, 0, 30, false, slots, "odd:unrelated.alpha"));
        rejects(() -> ledger.install(owner, id, weapon, ModInfo.Category.SWORD, 8, 0, 30, false, slots));
        ledger.install(owner, id, weapon, ModInfo.Category.SWORD, 8, 0, 30, false, slots, "odd:no_family_hint");
        check(ledger.bonuses(weapon, "odd:no_family_hint").size() == 1 && ledger.bonuses(weapon, "odd:unrelated.alpha").isEmpty());
        var second = UUID.randomUUID(); ledger.add(new ModLedger.Entry(second, owner, null, false, card.bonuses(), card.info(), -1));
        rejects(() -> ledger.install(owner, second, weapon, ModInfo.Category.SWORD, 8, 1, 100, true, slots, "odd:no_family_hint"));
        for (double[] threshold : new double[][] {{0,0},{.5,1},{.699,1},{.7,2},{.899,2},{.9,3},{1.1,3},{1.1001,4},{1.3,4},{1.3001,5},{1.55,5}})
            check(RivenRules.stars(threshold[0]) == threshold[1]);
        rejects(() -> RivenRules.stars(Double.NaN)); rejects(() -> weapons.get("minecraft:bow").withDisposition(.4));
        rejects(() -> rules.roll(RivenRoll.Pool.MELEE, Map.of(), bound -> 0, () -> .5));
        rejects(() -> catalog.rivenCard(roll, 9));
        System.out.println("Passed " + checks + " Riven pools, layouts, exact specialization, stats and rank checks");
    }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-9); }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Riven rule mismatch"); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected Riven rejection"); }
}
