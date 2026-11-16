package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ModTests {
    private static int checks;
    private ModTests() {}
    public static void run() {
        var catalog = BuiltinMods.catalog(); check(catalog.definitions().size() == 36); check(catalog.creative().size() == 236);
        for (int side : List.of(96, 124, 128)) {
            var rectangle = new yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.Rect(17, 31, side, side);
            var art = yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.artwork(rectangle);
            check(art.y() >= rectangle.y() + 24);
            check(art.y() + art.height() <= rectangle.y() + side - 34);
        }
        var weights = new LinkedHashMap<DamageType, Double>();
        weights.put(DamageType.SLASH, .7); weights.put(DamageType.PUNCTURE, .1); weights.put(DamageType.IMPACT, .2);
        var reordered = new WeaponProfile(0, 2, 0, 6, weights);
        weights.clear(); weights.put(DamageType.IMPACT, .2); weights.put(DamageType.SLASH, .7); weights.put(DamageType.PUNCTURE, .1);
        check(reordered.equals(new WeaponProfile(0, 2, 0, 6, weights)));
        check(catalog.qualities().size() == 6); check(catalog.polarities().size() == 9);
        for (var polarity : List.of(Polarity.MADURAI, Polarity.VAZARIN, Polarity.NARAMON, Polarity.ZENURIK, Polarity.UNAIRU, Polarity.PENJAGA, Polarity.OMNI)) check(Polarity.OMNI.matches(polarity));
        check(!Polarity.OMNI.matches(Polarity.UMBRA)); check(!Polarity.OMNI.matches(Polarity.NONE));
        check(!Polarity.MADURAI.matches(Polarity.OMNI)); check(Polarity.UMBRA.matches(Polarity.UMBRA));
        check(Polarity.OMNI.ordinal() == 8); check(Polarity.OMNI.symbol().charAt(0) == 0xE108);
        var qualities = new EnumMap<Rarity, ModCatalog.Quality>(Rarity.class); qualities.putAll(catalog.qualities());
        qualities.put(Rarity.COMMON, new ModCatalog.Quality(Rarity.COMMON, "Custom quality", 0x123456, 13, 91, "Custom upgrade base."));
        var polarities = new EnumMap<Polarity, ModCatalog.Polarity>(Polarity.class); polarities.putAll(catalog.polarities());
        polarities.put(Polarity.MADURAI, new ModCatalog.Polarity(Polarity.MADURAI, "Custom polarity", "@"));
        var customized = new ModCatalog(catalog.definitions(), qualities, polarities);
        check(customized.qualityName(Rarity.COMMON).equals("Custom quality"));
        check(customized.polarityName(Polarity.MADURAI).equals("Custom polarity"));
        check(customized.polaritySymbol(Polarity.MADURAI).equals("@"));
        check(customized.card("yagens_attributes:universal_nanocube", 0).info().color() == 0x123456);
        check(customized.card("yagens_attributes:universal_nanocube", 0).info().details().upgradeCost() == 13);
        for (var card : catalog.creative()) {
            var definition = catalog.definitions().get(card.id());
            check(card.info().defined()); check(card.info().level() == card.level());
            check(card.info().capacityCost() == definition.baseCapacity() + card.level());
            check(card.info().details().upgradeCost() == (card.level() == definition.maxLevel() ? 0 : catalog.qualities().get(definition.rarity()).baseEndo() * (1L << card.level())));
        }
        for (int i = 1; i < catalog.creative().size(); i++) {
            var a = catalog.creative().get(i - 1); var b = catalog.creative().get(i);
            check(a.info().qualityOrder() < b.info().qualityOrder() || a.info().qualityOrder() == b.info().qualityOrder()
                    && (a.id().compareTo(b.id()) < 0 || a.id().equals(b.id()) && b.level() == a.level() + 1));
        }
        var shortDefinition = new ModDefinition("Short", Rarity.COMMON, Polarity.NONE, 1, 1, 1, Set.of(Category.WEAPON), "test:item/square",
                List.of(new ModDefinition.Effect(Stats.DAMAGE, ModDefinition.Operation.ADDITION, .2, ModDefinition.Basis.FLAT, "Damage bonus.")));
        var longerDefinition = new ModDefinition("Long", Rarity.COMMON, Polarity.NONE, 1, 1, 2, Set.of(Category.WEAPON), "test:item/square", shortDefinition.entries());
        var grouped = new ModCatalog(Map.of("odd:zebra", shortDefinition, "another:irregular", longerDefinition), catalog.qualities(), catalog.polarities());
        check(grouped.creative().stream().map(card -> card.id() + "/" + card.level()).toList().equals(List.of("another:irregular/0", "another:irregular/1", "another:irregular/2", "odd:zebra/0", "odd:zebra/1")));
        for (var definition : catalog.definitions().values()) for (var entry : definition.entries()) check(entry.operation() == (Set.of(Stats.COMBO_DURATION, Stats.MELEE_RANGE).contains(entry.stat()) ? ModDefinition.Operation.ADDIN : ModDefinition.Operation.ADDITION));
        close(.15, catalog.card("yagens_attributes:bone_finger_box", 0).bonuses().get(Stats.CRITICAL_CHANCE).addition());
        close(.9, catalog.card("yagens_attributes:bone_finger_box", 5).bonuses().get(Stats.CRITICAL_CHANCE).addition());
        close(1.1, catalog.card("yagens_attributes:torturous_entanglement", 10).bonuses().get(Stats.STATUS_DURATION).addition());
        close(-.15, catalog.card("yagens_attributes:flame_feather", 5).bonuses().get(Stats.DAMAGE).addition());
        close(1.75, Stats.resolve(1, Stats.DAMAGE, List.of(catalog.card("yagens_attributes:flame_feather", 5).bonuses(), catalog.card("yagens_attributes:universal_nanocube", 5).bonuses())));
        close(1.55, Stats.resolve(1, Stats.DAMAGE, List.of(catalog.card("yagens_attributes:universal_nanocube", 0).bonuses(), Map.of(Stats.DAMAGE, new Bonus(0, .4, List.of(), 0)))));
        var element = catalog.card("yagens_attributes:heat_addition", 0);
        close(.15, element.bonuses().get("damage.heat").inputAddition()); close(0, element.bonuses().get("damage.heat").inputAddin());
        var impact = new WeaponProfile(0, 2, 1, 6, Map.of(DamageType.IMPACT, 1D));
        close(15, Combat.components(100, impact, List.of(element.bonuses())).get(DamageType.HEAT));
        close(115, Combat.calculate(100, impact, List.of(element.bonuses()), Map.of(), () -> .99).damage());
        check(Combat.calculate(100, impact, List.of(element.bonuses()), Map.of(), () -> .99).procs().equals(List.of(DamageType.HEAT)));
        close(1.5, Combat.components(10, impact, List.of(element.bonuses())).get(DamageType.HEAT));
        close(0, Combat.components(100, impact, List.of(catalog.card("yagens_attributes:slash_boost", 5).bonuses())).get(DamageType.SLASH));
        close(361, Combat.components(100, impact, List.of(catalog.card("yagens_attributes:heat_addition", 5).bonuses(), catalog.card("yagens_attributes:universal_nanocube", 5).bonuses())).values().stream().mapToDouble(Double::doubleValue).sum());
        close(10.3, Bonus.evaluate(0, List.of(new Bonus(2, .2, List.of(1.5), 4, .15)), 10));
        close(9.85, Bonus.evaluate(0, List.of(new Bonus(2, .2, List.of(1.5), 4, 0, .15)), 10));
        close(35, Combat.components(100, impact, List.of(element.bonuses(), Map.of("damage.heat", new Bonus(0, 0, List.of(), 0, 0, .2)))).get(DamageType.HEAT));
        var innateHeat = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.HEAT, .5, DamageType.IMPACT, .5));
        close(65, Combat.components(100, innateHeat, List.of(element.bonuses())).get(DamageType.HEAT));
        var multi = catalog.card("yagens_attributes:multishot", 0);
        check(multi.info().compatible(Category.TRIDENT)); check(multi.info().compatible(Category.BOW)); check(multi.info().compatible(Category.CROSSBOW)); check(!multi.info().compatible(Category.SWORD));
        check(multi.info().matches(Category.MELEE)); check(multi.info().matches(Category.RANGED)); check(!multi.info().matches(Category.AXE));
        var unrelated = new ModDefinition("unrelated", Rarity.RARE, Polarity.NONE, 1, 1, 2, Set.of(Category.RANGED, Category.TRIDENT), "test:item/square",
                List.of(new ModDefinition.Effect(Stats.FIRE_RATE, ModDefinition.Operation.ADDITION, .23, ModDefinition.Basis.FLAT, "Fractional fire-rate increment.")));
        var other = new ModCatalog(Map.of("test:completely_irregular", unrelated), catalog.qualities(), catalog.polarities()).card("test:completely_irregular", 2);
        close(.69, other.bonuses().get(Stats.FIRE_RATE).addition()); check(other.info().compatible(Category.TRIDENT)); check(!other.bonuses().containsKey("damage.heat"));
        long total = 0; for (int i = 0; i < 5; i++) total += catalog.card("yagens_attributes:multishot", i).info().details().upgradeCost(); close(93, total);
        UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(), id = UUID.randomUUID(), second = UUID.randomUUID();
        var ledger = new ModLedger();
        ledger.add(new ModLedger.Entry(id, owner, null, false, multi.bonuses(), multi.info(), -1));
        ledger.install(owner, id, weapon, Category.TRIDENT, 8, 0, 30, false);
        ledger.add(new ModLedger.Entry(second, owner, null, false, multi.bonuses(), multi.info(), -1));
        rejects(() -> ledger.install(owner, second, weapon, Category.TRIDENT, 8, 1, 30, false));
        var next = catalog.card(multi.id(), 1);
        rejects(() -> ledger.upgrade(owner, id, next.info(), next.bonuses(), 10, false, weapon));
        check(ledger.entry(id).info().level() == 0); check(ledger.usedCapacity(weapon) == 10);
        ledger.upgrade(owner, id, next.info(), next.bonuses(), 11, false, weapon); check(ledger.usedCapacity(weapon) == 11); check(ledger.entry(id).info().level() == 1);
        var following = catalog.card(multi.id(), 2); UUID stranger = UUID.randomUUID();
        rejects(() -> ledger.upgrade(stranger, id, following.info(), following.bonuses(), 30, false, weapon));
        ledger.share(owner, id, true);
        rejects(() -> ledger.upgrade(stranger, id, following.info(), following.bonuses(), 30, false, UUID.randomUUID()));
        ledger.upgrade(stranger, id, following.info(), following.bonuses(), 30, false, weapon);
        check(ledger.entry(id).owner().equals(owner)); check(ledger.entry(id).info().level() == 2);
        rejects(() -> ledger.withdraw(owner, id)); rejects(() -> ledger.withdraw(UUID.randomUUID(), second));
        ledger.withdraw(owner, second); check(ledger.owned(owner).size() == 1);
        rejects(() -> catalog.card(multi.id(), 6)); rejects(() -> catalog.card("test:missing", 0));
        rejects(() -> new ModDefinition.Effect(Stats.CRITICAL_CHANCE, ModDefinition.Operation.ADDIN, .15, ModDefinition.Basis.ATTACK_DAMAGE, "Invalid damage basis."));
        System.out.println("Passed " + checks + " Mod definition/rank/capacity/element contract checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Mod contract failed at " + checks); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-8); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException exception) { return; } throw new AssertionError("Expected rejection"); }
}
