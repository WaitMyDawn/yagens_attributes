package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class CoreTests {
    private static int checks;

    public static void main(String[] args) {
        formula(); damage(); probabilities(); ledger(); invalid(); arsenal(); progression(); classDefaults(); endo();
        System.out.println("Passed " + checks + " core contract checks");
        RangedTests.run();
        StatusTests.run();
        ModTests.run();
        HitProcTests.run();
        GalvanizedTests.run();
        ConditionalModTests.run();
        GalvanizedExpansionTests.run();
        ArsenalExpansionTests.run();
        RemoteReleaseTests.run();
        ModFilterTests.run();
        ComboTests.run();
        PolarityTests.run();
        ArmorTests.run();
        InteractionTests.run();
        HudLayoutTests.run();
        ExperienceTests.run();
        RivenTests.run();
        RivenAnalysisTests.run();
        ModRewardTests.run();
    }

    private static void formula() {
        close(0.432, Bonus.evaluate(0.15, List.of(new Bonus(0, .2, List.of(1.5), 0), new Bonus(0, .4, List.of(1.2), 0))));
        close(43, Bonus.evaluate(10, List.of(new Bonus(2, .5, List.of(2D), 7))));
        close(10, Bonus.evaluate(10, List.of()));
        close(0, Stats.resolve(10, "damage", List.of(Map.of("damage", new Bonus(0, -2, List.of(), 0)))));
    }

    private static WeaponProfile profile(double cc, double cd, double sc) {
        return new WeaponProfile(cc, cd, sc, 6, Map.of(DamageType.IMPACT, 1D, DamageType.SLASH, 3D));
    }

    private static void damage() {
        var weapon = profile(0, 2, 0);
        close(10, Combat.calculate(10, weapon, List.of(), Map.of(), () -> .5).damage());
        close(8.75, Combat.calculate(10, weapon, List.of(), Map.of(DamageType.IMPACT, 2D, DamageType.SLASH, .5), () -> .5).damage());
        var bonuses = Map.of("damage.cold", new Bonus(2, 0, List.of(), 0), "damage", new Bonus(0, .5, List.of(), 0));
        close(18, Combat.calculate(10, weapon, List.of(bonuses), Map.of(), () -> .5).damage());
        close(3, Combat.components(10, weapon, List.of(bonuses)).get(DamageType.COLD));
        close(11.25, Combat.components(10, weapon, List.of(bonuses)).get(DamageType.SLASH));
        close(18, Combat.components(10, weapon, List.of(bonuses)).values().stream().mapToDouble(Double::doubleValue).sum());
        close(0, Combat.calculate(0, weapon, List.of(), Map.of(), () -> .5).damage());
        close(40, Combat.calculate(10, profile(3, 2, 0), List.of(), Map.of(), () -> .5).damage());
    }

    private static void probabilities() {
        var result = Combat.calculate(10, profile(2.25, 2, 2.5), List.of(), Map.of(), () -> .1);
        equal(3, result.criticalTier()); equal(3, result.procs().size()); equal(DamageType.IMPACT, result.procs().get(0));
        result = Combat.calculate(10, profile(2.25, 2, 2.5), List.of(), Map.of(), () -> .9);
        equal(2, result.criticalTier()); equal(2, result.procs().size()); equal(DamageType.SLASH, result.procs().get(0));
        equal(0, Combat.calculate(0, profile(0, 2, 2), List.of(), Map.of(), () -> .5).procs().size());
        var boosts = Map.of("critical_chance", new Bonus(1000, 0, List.of(), 0), "status_chance", new Bonus(1000, 0, List.of(), 0));
        result = Combat.calculate(10, profile(0, 2, 0), List.of(boosts), Map.of(), () -> .5);
        equal(100, result.criticalTier()); equal(64, result.procs().size());
    }

    private static void ledger() {
        var ledger = new ModLedger();
        UUID owner = UUID.randomUUID(), other = UUID.randomUUID(), weapon = UUID.randomUUID(), second = UUID.randomUUID(), mod = UUID.randomUUID();
        ledger.add(new ModLedger.Entry(mod, owner, null, false, Map.of("critical_chance", new Bonus(0, .2, List.of(), 0))));
        ledger.install(owner, mod, weapon);
        equal(1, ledger.bonuses(weapon).size());
        rejects(() -> ledger.install(owner, mod, second));
        rejects(() -> ledger.uninstall(other, mod, weapon, true));
        rejects(() -> ledger.uninstall(owner, mod, second, true));
        rejects(() -> ledger.share(other, mod, true));
        ledger.share(owner, mod, true);
        ledger.uninstall(other, mod, weapon, true);
        equal(0, ledger.owned(owner).size()); equal(1, ledger.owned(other).size()); equal(0, ledger.bonuses(weapon).size());
        equal(null, ledger.owned(other).get(0).weapon());
        ledger.install(other, mod, second);
        equal(1, ledger.bonuses(second).size());
        ledger.uninstall(other, mod, second, false);
        equal(1, ledger.owned(other).size()); equal(0, ledger.installed(second).size());
        var restored = new ModLedger(); for (var entry : ledger.all()) restored.add(entry);
        equal(ledger.all(), restored.all());
        rejects(() -> restored.add(ledger.owned(other).get(0)));
        rejects(() -> restored.add(new ModLedger.Entry(UUID.randomUUID(), other, null, false, Map.of("unknown", Bonus.NONE))));
    }

    private static void invalid() {
        rejects(() -> new Bonus(Double.NaN, 0, List.of(), 0));
        rejects(() -> new Bonus(0, 0, List.of(Double.POSITIVE_INFINITY), 0));
        rejects(() -> new WeaponProfile(0, 2, 0, 6, Map.of()));
        rejects(() -> new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.COLD, -1D)));
        rejects(() -> Combat.calculate(10, profile(0, 2, 0), List.of(), Map.of(DamageType.IMPACT, -1D), () -> .5));
        rejects(() -> Bonus.evaluate(Double.MAX_VALUE, List.of(new Bonus(0, 0, List.of(2D), 0))));
    }

    private static void arsenal() {
        equal(true, ModInfo.Category.MELEE.compatible(ModInfo.Category.SWORD));
        equal(true, ModInfo.Category.MELEE.compatible(ModInfo.Category.AXE));
        equal(false, ModInfo.Category.SWORD.compatible(ModInfo.Category.AXE));
        equal(false, ModInfo.Category.WARFRAME.compatible(ModInfo.Category.BOW));
        UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID();
        var ledger = new ModLedger();
        var common = new ModInfo("夜曲 / Sonata", ModInfo.Category.MELEE, ModInfo.Rarity.COMMON, ModInfo.Polarity.NARAMON);
        var rare = new ModInfo("Alpha", ModInfo.Category.SWORD, ModInfo.Rarity.RARE, ModInfo.Polarity.MADURAI);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID(), third = UUID.randomUUID();
        for (UUID id : List.of(first, second, third)) ledger.add(new ModLedger.Entry(id, owner, null, false, Map.of("damage", Bonus.NONE), common, -1));
        UUID specialist = UUID.randomUUID(); ledger.add(new ModLedger.Entry(specialist, owner, null, false, Map.of("damage", Bonus.NONE), rare, -1));
        ledger.install(owner, first, weapon, ModInfo.Category.SWORD, 2, 1);
        equal(1, ledger.installed(weapon).get(0).slot());
        rejects(() -> ledger.install(owner, second, weapon, ModInfo.Category.SWORD, 2, 1));
        rejects(() -> ledger.install(owner, specialist, UUID.randomUUID(), ModInfo.Category.AXE, 8, 0));
        ledger.install(owner, second, weapon, ModInfo.Category.SWORD, 2, -1);
        rejects(() -> ledger.install(owner, third, weapon, ModInfo.Category.SWORD, 2, -1));
        var all = ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, null, entry -> entry.info().name());
        equal(4, all.size()); equal(specialist, all.get(0).representative().id());
        var byQuantity = ModInventory.browse(ledger.owned(owner), new ModInventory.Query("", ModInfo.Category.ALL, ModInventory.Order.QUANTITY), null, entry -> entry.info().name());
        equal(4, byQuantity.size()); equal(4, byQuantity.stream().mapToInt(ModInventory.Group::quantity).sum()); equal(2, byQuantity.stream().mapToInt(ModInventory.Group::available).sum());
        var compatible = ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, ModInfo.Category.SWORD, entry -> entry.info().name());
        equal(2, compatible.size()); equal(2, compatible.stream().mapToInt(ModInventory.Group::quantity).sum());
        var onlyAxes = ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, ModInfo.Category.AXE, entry -> entry.info().name());
        equal(1, onlyAxes.size()); equal(third, onlyAxes.get(0).representative().id());
        var search = ModInventory.browse(ledger.owned(owner), new ModInventory.Query("SONATA", ModInfo.Category.SWORD, ModInventory.Order.NAME), null, entry -> entry.info().name());
        equal(3, search.size()); equal(3, search.stream().mapToInt(ModInventory.Group::quantity).sum());
        equal(0, ModInventory.browse(ledger.owned(owner), new ModInventory.Query("", ModInfo.Category.WARFRAME, ModInventory.Order.NAME), null, entry -> entry.info().name()).size());
        ledger.metadata(third, new ModInfo("Unrelated bow-shaped name", ModInfo.Category.MELEE, ModInfo.Rarity.UNCOMMON, ModInfo.Polarity.NONE));
        equal(4, ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, null, entry -> entry.info().name()).size());
        var duplicate = new ModLedger.Entry(UUID.randomUUID(), owner, weapon, false, Map.of(), ModInfo.DEFAULT, 1);
        rejects(() -> ledger.add(duplicate));
        for (var scatter : List.of(yagen.waitmydawn.yagens_attributes.core.ui.DamageScatter.sample(0, .5, 0), yagen.waitmydawn.yagens_attributes.core.ui.DamageScatter.sample(1, .1, 1))) {
            double radius = Math.hypot(scatter.x(), scatter.y() + 8);
            equal(true, radius >= 27.99 && radius <= 52.01);
            equal(true, Math.hypot(scatter.velocityX(), scatter.velocityY()) > 0);
        }
        for (int i = 0; i < 128; i++) {
            var scatter = yagen.waitmydawn.yagens_attributes.core.ui.DamageScatter.sample(i, .23, .5).forTextWidth(200);
            if (Math.abs(scatter.x()) <= 100 && scatter.y() <= 0 && scatter.y() + 9 >= 0) throw new AssertionError("Wide label covers its anchor");
        }
        checks++;
        for (var cells : List.of(yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.storage(), yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.slots(), yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.compatible())) {
            for (int i = 0; i < cells.size(); i++) {
                var cell = cells.get(i);
                var art = yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.artwork(cell);
                if (cell.x() < 0 || cell.y() < 0 || cell.x() + cell.width() > yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.WIDTH || cell.y() + cell.height() > yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.HEIGHT) throw new AssertionError("Cell outside canvas");
                equal(cell.width(), cell.height()); equal(art.width(), art.height()); equal(0, art.width() % 32);
                equal(true, cell.contains(art.x(), art.y()) && cell.contains(art.x() + art.width() - 1, art.y() + art.height() - 1));
                for (int j = i + 1; j < cells.size(); j++) if (cell.contains(cells.get(j).x(), cells.get(j).y())) throw new AssertionError("Overlapping cells");
            }
            checks++;
        }
    }

    private static class Tool {}
    private static class OddName extends Tool {}
    private static class Descendant extends OddName {}
    private static class SwordNamedObject {}

    private static void classDefaults() {
        var defaults = new ClassDefaults<>(Map.of(Tool.class, "general", OddName.class, "specific"));
        equal(Optional.of("specific"), defaults.match(Descendant.class));
        equal(Optional.of("specific"), defaults.match(Descendant.class));
        equal(Optional.of("general"), defaults.match(Tool.class));
        equal(Optional.empty(), defaults.match(SwordNamedObject.class));
        equal(Optional.empty(), defaults.match(String.class));
        equal(Optional.empty(), defaults.match(Runnable.class));
        var other = new ClassDefaults<>(Map.of(Tool.class, "other"));
        equal(Optional.of("other"), other.match(Descendant.class));
        equal(18, yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.storage().size());
        equal(8, yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.slots().size());
        equal(8, yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.compatible().size());
        equal(128, yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.slots().get(0).width());
        equal(true, yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.slots().stream().noneMatch(cell -> cell.contains(yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.DETAILS.x(), yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.DETAILS.y())));
    }

    private static void endo() {
        equal(64L, EndoCurrency.credit(0, 64)); equal(Long.MAX_VALUE, EndoCurrency.credit(Long.MAX_VALUE - 1, 1));
        rejects(() -> EndoCurrency.credit(Long.MAX_VALUE, 1)); rejects(() -> EndoCurrency.credit(0, 0));
        rejects(() -> EndoCurrency.credit(1, -1)); rejects(() -> EndoCurrency.credit(-1, 1));
        for (String invalid : List.of("", "0", "-1", "+1", "1.5", "1e3", " 1", "1 ", "１２", "9223372036854775808", "00000000000000000001", "100"))
            equal(OptionalLong.empty(), EndoCurrency.parse(invalid, 99));
        equal(OptionalLong.of(99), EndoCurrency.parse("99", 99)); equal(OptionalLong.of(1), EndoCurrency.parse("001", 99));
        equal(OptionalLong.of(Long.MAX_VALUE), EndoCurrency.parse("9223372036854775807", Long.MAX_VALUE));
        equal(OptionalLong.empty(), EndoCurrency.parse("1", 0));
        var room = List.of(0, 4, 64, 64);
        var plan = EndoCurrency.withdraw(1000, 70, room);
        equal(930L, plan.balance()); equal(List.of(0, 4, 64, 2), plan.additions()); equal(List.of(0, 4, 64, 64), room);
        equal(70, plan.additions().stream().mapToInt(Integer::intValue).sum());
        rejects(() -> EndoCurrency.withdraw(1000, 133, room)); rejects(() -> EndoCurrency.withdraw(10, 11, room));
        rejects(() -> EndoCurrency.withdraw(10, -1, room)); rejects(() -> EndoCurrency.withdraw(10, 0, room));
        rejects(() -> EndoCurrency.withdraw(Long.MAX_VALUE, Long.MAX_VALUE, room));
        rejects(() -> EndoCurrency.withdraw(1, 1, List.of())); rejects(() -> EndoCurrency.withdraw(1, 1, List.of(-1)));
        equal(0L, EndoCurrency.withdraw(64, 64, List.of(64)).balance());
        for (int amount = 1; amount <= 132; amount++) {
            var allocation = EndoCurrency.withdraw(Long.MAX_VALUE, amount, room);
            equal(Long.MAX_VALUE - amount, allocation.balance());
            equal(amount, allocation.additions().stream().mapToInt(Integer::intValue).sum());
        }
    }

    private static void progression() {
        equal(7, WeaponProgress.ZERO.nextLevelExperience()); close(0, WeaponProgress.ZERO.fraction());
        for (int level = 1; level <= 30; level++) {
            int threshold = level * level + 6 * level;
            equal(level - 1, WeaponProgress.ZERO.gain(threshold - 1).progress().level());
            equal(level, WeaponProgress.ZERO.gain(threshold).progress().level());
        }
        var max = WeaponProgress.ZERO.gain(Integer.MAX_VALUE);
        equal(1080, max.consumed()); equal(30, max.progress().level()); equal(0, max.progress().experience());
        equal(0, max.progress().gain(100).consumed()); close(1, max.progress().fraction());
        var split = WeaponProgress.distribute(1, List.of(WeaponProgress.ZERO, WeaponProgress.ZERO));
        equal(1, split.gains().get(0).consumed()); equal(0, split.gains().get(1).consumed()); equal(0, split.remaining());
        var almostMaxed = new WeaponProgress(29, 64);
        var mainOverflow = WeaponProgress.distribute(11, List.of(almostMaxed, WeaponProgress.ZERO));
        equal(List.of(almostMaxed.gain(1), WeaponProgress.ZERO.gain(10)), mainOverflow.gains()); equal(0, mainOverflow.remaining());
        var offOverflow = WeaponProgress.distribute(11, List.of(WeaponProgress.ZERO, almostMaxed));
        equal(List.of(WeaponProgress.ZERO.gain(10), almostMaxed.gain(1)), offOverflow.gains()); equal(0, offOverflow.remaining());
        var surplus = WeaponProgress.distribute(2167, List.of(WeaponProgress.ZERO, WeaponProgress.ZERO));
        equal(7, surplus.remaining()); equal(1080, surplus.gains().get(0).consumed()); equal(1080, surplus.gains().get(1).consumed());
        rejects(() -> new WeaponProgress(31, 0)); rejects(() -> new WeaponProgress(0, 7)); rejects(() -> new WeaponProgress(30, 1));
        var ledger = new ModLedger(); UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(), mod = UUID.randomUUID();
        var info = new ModInfo("Capacity test", ModInfo.Category.MELEE, ModInfo.Rarity.RARE, ModInfo.Polarity.NONE, 5);
        ledger.add(new ModLedger.Entry(mod, owner, null, false, Map.of(), info, -1));
        rejects(() -> ledger.install(owner, mod, weapon, ModInfo.Category.SWORD, 8, 0, 4, false));
        equal(null, ledger.entry(mod).weapon()); equal(0, ledger.usedCapacity(weapon));
        ledger.install(owner, mod, weapon, ModInfo.Category.SWORD, 8, 0, 0, true); equal(5, ledger.usedCapacity(weapon));
        UUID second = UUID.randomUUID(); ledger.add(new ModLedger.Entry(second, owner, null, false, Map.of(), info, -1));
        rejects(() -> ledger.install(UUID.randomUUID(), second, weapon, ModInfo.Category.SWORD, 8, 1, 0, true));
        rejects(() -> ledger.install(owner, second, weapon, ModInfo.Category.AXE, 8, 0, 0, true));
        rejects(() -> ledger.install(owner, second, weapon, ModInfo.Category.BOW, 8, 1, 0, true));
        rejects(() -> ledger.install(owner, second, weapon, ModInfo.Category.SWORD, 8, 1, 5, false));
        ledger.uninstall(owner, mod, weapon, false); ledger.install(owner, second, weapon, ModInfo.Category.SWORD, 8, 0, 5, false); equal(5, ledger.usedCapacity(weapon));
        equal(13, DamageType.DISPLAY_ORDER.size()); equal(13, new HashSet<>(DamageType.DISPLAY_ORDER).size());
        equal(Set.of(DamageType.BLAST, DamageType.RADIATION, DamageType.CORROSIVE, DamageType.VIRAL, DamageType.GAS, DamageType.MAGNETIC), Set.copyOf(DamageType.DISPLAY_ORDER.subList(0, 6)));
        equal(Set.of(DamageType.COLD, DamageType.HEAT, DamageType.ELECTRICITY, DamageType.TOXIN), Set.copyOf(DamageType.DISPLAY_ORDER.subList(6, 10)));
        equal(Set.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH), Set.copyOf(DamageType.DISPLAY_ORDER.subList(10, 13)));
    }

    private static void close(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-9) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    private static void rejects(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Expected validation failure");
    }
}
