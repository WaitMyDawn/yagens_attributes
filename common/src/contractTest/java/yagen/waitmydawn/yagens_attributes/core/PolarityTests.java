package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PolarityTests {
    private static int checks;
    private PolarityTests() {}

    public static void run() {
        for (int base : List.of(0, 1, 2, 7, 10, 999_999, 1_000_000)) {
            var info = info(Polarity.MADURAI, base);
            equal((base + 1) / 2, PolarityRules.cost(info, Polarity.MADURAI));
            equal(base + (base + 3) / 4, PolarityRules.cost(info, Polarity.VAZARIN));
            equal(base, PolarityRules.cost(info, Polarity.NONE));
            equal(base, PolarityRules.cost(info(Polarity.NONE, base), Polarity.MADURAI));
            equal((base + 1) / 2, PolarityRules.cost(info, Polarity.OMNI));
        }
        equal(9, PolarityRules.cost(info(Polarity.UMBRA, 7), Polarity.OMNI));
        UUID actor = UUID.randomUUID(), other = UUID.randomUUID(), weapon = UUID.randomUUID(), first = UUID.randomUUID(), second = UUID.randomUUID();
        var slots = List.of(Polarity.MADURAI, Polarity.VAZARIN);
        var ledger = new ModLedger();
        ledger.add(new ModLedger.Entry(first, actor, null, false, Map.of(), info(Polarity.MADURAI, 7), -1));
        rejects(() -> ledger.install(actor, first, weapon, Category.SWORD, 2, 0, 3, false, slots));
        equal(null, ledger.entry(first).weapon());
        ledger.install(actor, first, weapon, Category.SWORD, 2, 0, 4, false, slots);
        equal(4, ledger.usedCapacity(weapon, slots));
        rejects(() -> ledger.move(actor, first, weapon, 1, 2, 4, false, slots));
        equal(0, ledger.entry(first).slot());
        ledger.move(actor, first, weapon, 1, 2, 4, true, slots); equal(9, ledger.usedCapacity(weapon, slots));
        ledger.move(actor, first, weapon, 0, 2, 1, false, slots); equal(4, ledger.usedCapacity(weapon, slots));
        ledger.add(new ModLedger.Entry(second, other, null, false, Map.of(), info(Polarity.VAZARIN, 7), -1));
        ledger.install(other, second, weapon, Category.SWORD, 2, 1, 30, false, slots);
        rejects(() -> ledger.move(actor, first, weapon, 1, 2, 30, true, slots));
        rejects(() -> ledger.clearForPolarization(actor, weapon)); equal(2, ledger.installed(weapon).size());
        ledger.share(other, second, true);
        rejects(() -> ledger.move(actor, first, weapon, 1, 2, 8, false, slots));
        equal(0, ledger.entry(first).slot()); equal(1, ledger.entry(second).slot());
        ledger.move(actor, first, weapon, 1, 2, 8, true, slots); equal(18, ledger.usedCapacity(weapon, slots));
        ledger.clearForPolarization(actor, weapon);
        equal(0, ledger.installed(weapon).size()); equal(other, ledger.entry(second).owner());
        equal(null, ledger.entry(second).weapon()); equal(List.of(), ledger.bonuses(weapon));
        var card = BuiltinMods.catalog().card("yagens_attributes:nameless_glove", 0);
        var next = BuiltinMods.catalog().card(card.id(), 1);
        ledger.add(new ModLedger.Entry(UUID.randomUUID(), actor, null, false, card.bonuses(), card.info(), -1));
        var mod = ledger.owned(actor).stream().filter(entry -> entry.info().defined()).findFirst().orElseThrow();
        ledger.install(actor, mod.id(), weapon, Category.SWORD, 2, 0, 2, false, slots);
        rejects(() -> ledger.upgrade(actor, mod.id(), next.info(), next.bonuses(), 2, false, weapon, slots));
        equal(0, ledger.entry(mod.id()).info().level());
        ledger.upgrade(actor, mod.id(), next.info(), next.bonuses(), 3, false, weapon, slots);
        equal(3, ledger.usedCapacity(weapon, slots));
        var max = new WeaponProgress(30, 0);
        rejects(() -> PolarityRules.validateForma(new WeaponProgress(29, 0), slots, 0, Polarity.NARAMON));
        rejects(() -> PolarityRules.validateForma(max, slots, 0, Polarity.MADURAI));
        rejects(() -> PolarityRules.validateForma(max, slots, 2, Polarity.NARAMON));
        for (var polarity : Polarity.values()) if (!PolarityRules.FORMA_CHOICES.contains(polarity))
            rejects(() -> PolarityRules.validateForma(max, slots, 0, polarity));
        for (var polarity : PolarityRules.FORMA_CHOICES) PolarityRules.validateForma(max, List.of(Polarity.NONE), 0, polarity);
        for (var polarity : PolarityRules.FORMA_CHOICES) {
            PolarityRules.validateForma(WeaponProgress.ZERO, List.of(Polarity.NONE), 0, polarity, true);
            checks++;
        }
        rejects(() -> PolarityRules.validateForma(WeaponProgress.ZERO, slots, 0, Polarity.MADURAI, true));
        rejects(() -> PolarityRules.validateForma(WeaponProgress.ZERO, slots, 0, Polarity.UMBRA, true));
        rejects(() -> PolarityRules.validateForma(WeaponProgress.ZERO, slots, 2, Polarity.NARAMON, true));
        equal(new WeaponProgress(1, 0), WeaponProgress.ZERO.gain(7).progress());
        equal(max, WeaponProgress.ZERO.gain(1080).progress());
        System.out.println("Passed " + checks + " polarity/capacity/transaction contract checks");
    }

    private static ModInfo info(Polarity polarity, int capacity) { return new ModInfo("Irregular test fixture", Category.WEAPON, Rarity.RARE, polarity, capacity); }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected validation failure"); }
}
