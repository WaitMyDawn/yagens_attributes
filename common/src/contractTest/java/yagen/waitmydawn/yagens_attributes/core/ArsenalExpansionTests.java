package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

final class ArsenalExpansionTests {
    private static int checks;
    static void run() {
        elements(); recycling(); layers();
        check(Combo.gains(1.2, () -> .1) == 2); check(Combo.gains(1.2, () -> .3) == 1);
        check(Combo.gains(2, () -> { throw new AssertionError(); }) == 2);
        check(Combo.gains(0, () -> .1) == 0); check(Combo.gains(10000, () -> .1) == 64);
        var counter = Combo.Counter.EMPTY.hit(1, 100, Combo.Rules.DEFAULT, 2); check(counter.count(2, Combo.Rules.DEFAULT) == 2);
        var catalog = BuiltinMods.catalog();
        close(.6, catalog.card("yagens_attributes:lethal_torrent", 5).bonuses().get(Stats.FIRE_RATE).addition());
        close(.6, catalog.card("yagens_attributes:lethal_torrent", 5).bonuses().get(Stats.MULTISHOT).addition());
        var galv = catalog.card("yagens_attributes:galvanized_chamber", 10);
        check(galv.info().conflicts(catalog.card("yagens_attributes:multishot", 5).info()));
        close(3.3, Stats.resolve(1, Stats.MULTISHOT, List.of(galv.bonuses(), galv.info().details().conditional().get(0).source(5))));
        close(.2, catalog.card("yagens_attributes:quickening", 3).bonuses().get(Stats.COMBO_COUNT_CHANCE).addition());
        System.out.println("Passed " + checks + " elemental fusion, recycling, layered material and new Mod checks");
    }
    private static Map<String, Bonus> element(DamageType type, double value) { return Map.of("damage." + type.key(), new Bonus(0, 0, List.of(), 0, 0, value)); }
    private static WeaponProfile weapon() { return new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.IMPACT, 1D)); }
    private static void elements() {
        var expected = Map.of(Set.of(DamageType.COLD, DamageType.HEAT), DamageType.BLAST, Set.of(DamageType.COLD, DamageType.ELECTRICITY), DamageType.MAGNETIC,
                Set.of(DamageType.COLD, DamageType.TOXIN), DamageType.VIRAL, Set.of(DamageType.HEAT, DamageType.ELECTRICITY), DamageType.RADIATION,
                Set.of(DamageType.HEAT, DamageType.TOXIN), DamageType.GAS, Set.of(DamageType.ELECTRICITY, DamageType.TOXIN), DamageType.CORROSIVE);
        for (var a : ElementFusion.PRIMARY) for (var b : ElementFusion.PRIMARY) if (a != b) {
            check(ElementFusion.combine(a, b) == expected.get(Set.of(a, b)));
            var amounts = Combat.components(100, weapon(), List.of(element(a, .3), element(b, .6)));
            close(90, amounts.get(expected.get(Set.of(a, b)))); close(0, amounts.get(a)); close(0, amounts.get(b));
            close(190, amounts.values().stream().mapToDouble(Double::doubleValue).sum());
        }
        var sources = List.of(element(DamageType.HEAT, .3), element(DamageType.COLD, .6), element(DamageType.HEAT, .2), element(DamageType.ELECTRICITY, .9));
        var amounts = Combat.components(100, weapon(), sources); close(110, amounts.get(DamageType.BLAST)); close(90, amounts.get(DamageType.ELECTRICITY));
        var nativeToxin = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.TOXIN, 1D));
        var combined = Combat.components(100, nativeToxin, List.of(element(DamageType.HEAT, .3), element(DamageType.COLD, .6), element(DamageType.ELECTRICITY, .9)));
        close(90, combined.get(DamageType.BLAST)); close(190, combined.get(DamageType.CORROSIVE)); close(0, combined.get(DamageType.TOXIN));
        var innate = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.VIRAL, 1D));
        close(100, Combat.components(100, innate, sources).get(DamageType.VIRAL));
        close(100, Combat.components(100, weapon(), List.of(element(DamageType.HEAT, -.5))).get(DamageType.IMPACT));
        UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(); var ledger = new ModLedger();
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()); int slot = 0;
        for (var type : List.of(DamageType.HEAT, DamageType.COLD, DamageType.TOXIN)) ledger.add(new ModLedger.Entry(ids.get(slot), owner, weapon, false, element(type, 1), ModInfo.DEFAULT, slot++));
        close(200, Combat.components(100, weapon(), ledger.bonuses(weapon)).get(DamageType.BLAST));
        ledger.move(owner, ids.get(0), weapon, 2, 8);
        close(200, Combat.components(100, weapon(), ledger.bonuses(weapon)).get(DamageType.VIRAL));
        close(100, Combat.components(100, weapon(), ledger.bonuses(weapon)).get(DamageType.HEAT));
        var orderA = new LinkedHashMap<String, Bonus>(); orderA.putAll(element(DamageType.HEAT, 1)); orderA.putAll(element(DamageType.COLD, 1)); orderA.putAll(element(DamageType.TOXIN, 1));
        var orderB = new LinkedHashMap<String, Bonus>(); orderB.putAll(element(DamageType.COLD, 1)); orderB.putAll(element(DamageType.TOXIN, 1)); orderB.putAll(element(DamageType.HEAT, 1));
        var a = new ModLedger.Entry(UUID.randomUUID(), owner, null, false, orderA); var b = new ModLedger.Entry(UUID.randomUUID(), owner, null, false, orderB);
        check(!ModInventory.sameVariant(a, b));
        check(ModInventory.browse(List.of(a, b), ModInventory.Query.DEFAULT, null, entry -> "same name").size() == 2);
    }
    private static void recycling() {
        close(1, ModRecycling.value(1, 0)); close(16, ModRecycling.value(1, 5)); close(33, ModRecycling.value(2, 5));
        close(50, ModRecycling.value(3, 5)); close(70, ModRecycling.value(4, 5)); close(653, ModRecycling.value(5, 8));
        check(ModRecycling.value(1, 1) == 1); check(ModRecycling.value(5, 60) > 0);
        rejects(() -> ModRecycling.value(0, 1)); rejects(() -> ModRecycling.value(5, 61)); rejects(() -> ModRecycling.value(63, 60));
    }
    private static void layers() {
        var hit = LayeredDamage.resolve(10, 10, Map.of(DamageType.IMPACT, 5D, DamageType.TOXIN, 5D), Map.of(DamageType.IMPACT, 2D, DamageType.TOXIN, 3D), Map.of(DamageType.IMPACT, 2D), 5, 1, 1, false, false);
        close(5, hit.shieldSpent()); close(20, hit.healthDamage());
        var heat = Map.of(DamageType.HEAT, 1D);
        hit = LayeredDamage.resolve(8, 10, heat, Map.of(DamageType.HEAT, 2D), Map.of(DamageType.HEAT, .5), 3, 1, 1, false, false);
        close(3, hit.shieldSpent()); close(6.4, hit.healthDamage());
        var resistant = LayeredDamage.resolve(8, 10, heat, Map.of(DamageType.HEAT, .25), Map.of(DamageType.HEAT, .5), 3, 1, 1, false, false);
        close(hit.shieldSpent(), resistant.shieldSpent()); close(.8, resistant.healthDamage());
        hit = LayeredDamage.resolve(8, 10, heat, Map.of(DamageType.HEAT, 2D), Map.of(DamageType.HEAT, 0D), 3, 1, 1, false, false); close(0, hit.healthDamage()); close(0, hit.shieldSpent());
        hit = LayeredDamage.resolve(8, 10, heat, Map.of(DamageType.HEAT, 2D), Map.of(DamageType.HEAT, 0D), 0, 1, 1, false, false); close(16, hit.healthDamage());
        hit = LayeredDamage.resolve(10, 10, Map.of(DamageType.IMPACT, 1D, DamageType.TOXIN, 1D), Map.of(), Map.of(), 1, 1, 1, true, false); check(hit.gated()); close(5, hit.healthDamage());
        rejects(() -> LayeredDamage.resolve(10, 10, heat, Map.of(DamageType.HEAT, Double.NaN), Map.of(), 0, 1, 1, false, false));
        var assigned = new HashSet<String>();
        for (var material : BuiltinMaterials.life().values()) for (var entity : material.entities()) check(assigned.add(entity));
        check(BuiltinMaterials.life().size() == 8); check(BuiltinMaterials.shield().size() == 13);
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Arsenal expansion check " + checks); }
    private static void close(double a, double b) { check(Math.abs(a - b) < 1e-8); }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
