package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.client.StatText;
import yagen.waitmydawn.yagens_attributes.network.PanelPayload.ModifierView;
import yagen.waitmydawn.yagens_attributes.text.ModText;
import java.util.*;

public final class CycleContracts {
    private static int checks;
    private CycleContracts() {}
    public static void run() {
        var catalog = BuiltinMods.catalog(); var rules = catalog.rivenRules();
        double bow = rules.traits().get(Stats.FIRE_RATE).reference(BuiltinCombat.weapons().get("minecraft:bow"));
        check(bow == rules.traits().get(Stats.FIRE_RATE).reference(BuiltinCombat.weapons().get("minecraft:crossbow")));
        var random = new Random(314); var roll = rules.roll(RivenRoll.Pool.MELEE, BuiltinCombat.weapons(), random::nextInt, random::nextDouble);
        for (int i = 0; i < 20; i++) {
            var next = rules.cycle(roll, BuiltinCombat.weapons().get(roll.weapon()), random::nextInt, random::nextDouble);
            check(next.weapon().equals(roll.weapon()) && next.polarity() == roll.polarity() && next.category() == roll.category());
            check(next.cycles() == i + 1 && roll.cycleCost() == Math.min(16, i + 1)); roll = next;
        }
        check(RivenJson.roll(RivenJson.roll(roll)).equals(roll));
        var original = com.google.gson.JsonParser.parseString(RivenJson.roll(roll)).getAsJsonObject(); original.remove("cycles");
        check(RivenJson.roll(original).cycles() == 0);
        var lines = StatText.bonuses(Map.of(Stats.CRITICAL_CHANCE, ModifierView.of(new Bonus(0, -.5, List.of(), 0)), Stats.DAMAGE, ModifierView.of(new Bonus(3, .2, List.of(.5), -2))));
        boolean seenNegative = false;
        for (var line : lines) {
            String text = line.getString(); boolean negative = text.startsWith("-") || text.startsWith("x0.5");
            check(!seenNegative || negative); seenNegative |= negative;
        }
        check(seenNegative);
        check(ModifierView.of(new Bonus(0, 0, List.of(2D, .2), 0)).multiplyNegative());
        check(!ModifierView.of(new Bonus(0, 0, List.of(.5, 3D), 0)).multiplyNegative());
        check(ModText.metadata(catalog.rivenCard(roll, 0).info()).getString().contains("\uE109"));
        check(!ModText.metadata(catalog.rivenCard(roll.withCycles(0), 0).info()).getString().contains("\uE109"));
        System.out.println("Passed " + checks + " cycle pricing, identity, signs and metadata checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Cycle contract mismatch"); }
}
