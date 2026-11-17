package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import yagen.waitmydawn.yagens_attributes.core.ui.OrokinPixels;

final class ConditionalModTests {
    private static int checks;
    static void run() {
        var catalog = BuiltinMods.catalog();
        var damage = catalog.card("yagens_attributes:universal_nanocube", 5);
        var conditional = catalog.card("yagens_attributes:condition_overload", 5);
        check(conditional.bonuses().isEmpty());
        var perType = conditional.info().details().conditional().get(0);
        close(1, Stats.resolve(1, Stats.DAMAGE, List.of(perType.source(0))));
        close(3.5, Stats.resolve(1, Stats.DAMAGE, List.of(damage.bonuses(), perType.source(2))));
        close(11.4, Stats.resolve(1, Stats.DAMAGE, List.of(perType.source(13))));
        close(.8 / 6, catalog.card(conditional.id(), 0).info().details().conditional().get(0).addition());
        rejects(() -> perType.source(14));
        var kill = catalog.card("yagens_attributes:berserker_fury", 5).info().details().conditional().get(0);
        var counter = ConditionalModifier.Counter.EMPTY.hit(100, kill.condition());
        close(1.35, Stats.resolve(1, Stats.ATTACK_SPEED, List.of(kill.source(counter.count(100)))));
        counter = counter.hit(150, kill.condition()).hit(160, kill.condition());
        check(counter.count(349) == 2); check(counter.count(350) == 1); check(counter.count(359) == 1); check(counter.count(360) == 0);
        close(1.7, Stats.resolve(1, Stats.ATTACK_SPEED, List.of(kill.source(counter.count(160)))));
        check(counter.hit(400, kill.condition()).count(400) == 1);
        var separate = new ConditionalModifier.Counter(2, 60, List.of(60L, 100L)).hit(0, kill.condition());
        check(separate.deadlines().equals(List.of(100L, 200L)));
        check(separate.count(99, kill.condition()) == 2 && separate.count(100, kill.condition()) == 1);
        separate = separate.hit(120, kill.condition());
        check(separate.deadlines().equals(List.of(200L, 320L)));
        for (int i = 121; i < 500; i++) {
            separate = separate.hit(i, kill.condition()); check(separate.deadlines().size() == 2);
        }
        check(separate.count(700, kill.condition()) == 0);
        var irregularClock = new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_KILLS, 3.5, 3, false, ConditionalModifier.Decay.INDEPENDENT);
        var unrelated = ConditionalModifier.Counter.EMPTY.hit(7, irregularClock).hit(20, irregularClock).hit(30, irregularClock).hit(40, irregularClock);
        check(unrelated.deadlines().equals(List.of(90L, 100L, 110L)));
        check(unrelated.expirations(100, irregularClock).equals(List.of(110L)));
        var prime = catalog.card("yagens_attributes:primed_universal_nanocube", 10);
        close(1.65, prime.bonuses().get(Stats.DAMAGE).addition());
        close(3, catalog.card("yagens_attributes:primed_reach", 10).bonuses().get(Stats.MELEE_RANGE).addin());
        check(prime.info().conflicts(damage.info())); check(damage.info().conflicts(prime.info()));
        check(!prime.info().conflicts(catalog.card("yagens_attributes:primed_reach", 10).info()));
        var ledger = new ModLedger(); UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(), ordinary = UUID.randomUUID(), upgraded = UUID.randomUUID();
        ledger.add(new ModLedger.Entry(ordinary, owner, null, false, damage.bonuses(), damage.info(), -1));
        ledger.add(new ModLedger.Entry(upgraded, owner, null, false, prime.bonuses(), prime.info(), -1));
        ledger.install(owner, ordinary, weapon, ModInfo.Category.SWORD, 8, 0, 30, true);
        rejects(() -> ledger.install(owner, upgraded, weapon, ModInfo.Category.SWORD, 8, 1, 30, true));
        ledger.uninstall(owner, ordinary, weapon, false); ledger.install(owner, upgraded, weapon, ModInfo.Category.SWORD, 8, 0, 30, true);
        rejects(() -> ledger.install(owner, ordinary, weapon, ModInfo.Category.SWORD, 8, 1, 30, true));
        var a = new ModInfo("A", ModInfo.Category.WEAPON, ModInfo.Rarity.RARE, ModInfo.Polarity.NONE, 1,
                new ModInfo.Details("odd:flute", 0, 0, Set.of(ModInfo.Category.WEAPON), "odd:item/7", "", 0, 0, 0, null, ModStyle.family("shared:no_pattern"), List.of()));
        var b = new ModInfo("B", ModInfo.Category.WEAPON, ModInfo.Rarity.RARE, ModInfo.Polarity.NONE, 1,
                new ModInfo.Details("unrelated:melon", 0, 0, Set.of(ModInfo.Category.WEAPON), "odd:item/7", "", 0, 0, 0, null, ModStyle.family("shared:no_pattern"), List.of()));
        check(a.conflicts(b)); check(!a.conflicts(ModInfo.DEFAULT));
        conflicts(catalog);
        rejects(() -> new ConditionalModifier.Condition(ConditionalModifier.Trigger.MELEE_KILLS, 0, 2));
        rejects(() -> new ModDefinition.Effect(Stats.DAMAGE, ModDefinition.Operation.MULTIPLY, .2, ModDefinition.Basis.FLAT, "Invalid conditional operation.", kill.condition()));
        int[] pixels = new int[1024]; pixels[16 * 32 + 16] = 0xFF154474;
        int[] first = OrokinPixels.frame(pixels, 32, 32, 0), sweep = OrokinPixels.frame(pixels, 32, 32, 24);
        check(pixels[16 * 32 + 16] == 0xFF154474); check(first[0] == 0);
        check((first[16 * 32 + 15] >>> 24) > 0); check(!Arrays.equals(first, sweep));
        check(Arrays.equals(first, OrokinPixels.frame(pixels, 32, 32, 48)));
        System.out.println("Passed " + checks + " conditional modifier, exclusion family and animated pixel checks");
    }
    private static void conflicts(ModCatalog catalog) {
        var delay = catalog.card("yagens_attributes:critical_delay", 0).info();
        var bone = catalog.card("yagens_attributes:bone_finger_box", 5).info();
        var rush = catalog.card("yagens_attributes:blood_rush", 10).info();
        for (var category : ModInfo.Category.values()) {
            check(delay.conflicts(bone, category)); check(bone.conflicts(delay, category));
            check(!delay.conflicts(rush, category));
            check(!rush.conflicts(delay, category));
            check(!bone.conflicts(rush, category));
        }
        var fury = List.of(catalog.card("yagens_attributes:fury", 5).info(), catalog.card("yagens_attributes:berserker_fury", 5).info(), catalog.card("yagens_attributes:primed_fury", 10).info());
        for (var one : fury) for (var other : fury) check(one.conflicts(other, ModInfo.Category.SWORD));
        close(.55, catalog.card("yagens_attributes:primed_fury", 10).bonuses().get(Stats.ATTACK_SPEED).addition());
        var irregular = new ModStyle(ModStyle.Visual.NONE, Set.of(), Map.of(ModInfo.Category.MELEE, Set.of("odd:shared")));
        var unrelated = ModStyle.family("odd:shared");
        check(irregular.conflicts(unrelated, ModInfo.Category.AXE)); check(irregular.conflicts(unrelated, ModInfo.Category.SWORD));
        check(!irregular.conflicts(unrelated, ModInfo.Category.BOW)); check(!irregular.conflicts(unrelated));
        rejects(() -> new ModStyle(ModStyle.Visual.NONE, Set.of(), Map.of(ModInfo.Category.WARFRAME, Set.of("odd:shared"))));
        var flash = new yagen.waitmydawn.yagens_attributes.core.ui.ConflictFlash();
        UUID weapon = UUID.randomUUID(), incoming = UUID.randomUUID(), installed = UUID.randomUUID();
        flash.start(weapon, List.of(incoming, installed), 1000);
        for (long now : new long[]{1000, 1159, 1320, 1479}) { check(flash.red(weapon, incoming, now)); check(flash.red(weapon, installed, now)); }
        for (long now : new long[]{999, 1160, 1319, 1480, 1639, 1640, 10000}) check(!flash.red(weapon, incoming, now));
        check(!flash.red(UUID.randomUUID(), incoming, 1000)); check(!flash.red(weapon, UUID.randomUUID(), 1000));
        check(!flash.active(weapon, 1640));
        UUID owner = UUID.randomUUID(); var ledger = new ModLedger(); UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var firstCard = catalog.card("yagens_attributes:critical_delay", 0); var secondCard = catalog.card("yagens_attributes:blood_rush", 0);
        ledger.add(new ModLedger.Entry(first, owner, null, false, firstCard.bonuses(), firstCard.info(), -1));
        ledger.add(new ModLedger.Entry(second, owner, null, false, secondCard.bonuses(), secondCard.info(), -1));
        rejects(() -> ledger.install(owner, first, weapon, ModInfo.Category.TRIDENT, 8, 0, 30, true));
        ledger.install(owner, second, weapon, ModInfo.Category.TRIDENT, 8, 1, 30, true);
        check(ledger.entry(first).weapon() == null && ledger.installed(weapon).size() == 1);
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Conditional Mod check " + checks); }
    private static void close(double a, double b) { check(Math.abs(a - b) < 1e-9); }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
