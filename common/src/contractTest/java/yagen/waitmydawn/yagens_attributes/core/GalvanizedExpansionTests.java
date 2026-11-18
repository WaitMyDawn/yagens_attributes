package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GalvanizedExpansionTests {
    private static int checks;
    private GalvanizedExpansionTests() {}
    public static void run() {
        var catalog = BuiltinMods.catalog();
        for (var id : List.of("primed_toxin_addition", "primed_cold_addition", "galvanized_bone_finger_box",
                "galvanized_gaze_of_pain", "galvanized_argon_scope")) {
            var definition = catalog.definitions().get("yagens_attributes:" + id);
            check(definition.maxLevel() == 10);
            for (int rank = 0; rank <= 10; rank++) {
                var card = catalog.card("yagens_attributes:" + id, rank);
                check(card.info().capacityCost() == definition.baseCapacity() + rank);
                check(card.info().details().style().visual() != ModStyle.Visual.NONE);
            }
        }
        var toxin = catalog.card("yagens_attributes:primed_toxin_addition", 10);
        var cold = catalog.card("yagens_attributes:primed_cold_addition", 10);
        check(catalog.definitions().get("yagens_attributes:primed_cold_addition").texture().equals("yagens_attributes:status/cold_status"));
        close(1.65, toxin.bonuses().get("damage.toxin").inputAddition());
        close(1.65, cold.bonuses().get("damage.cold").inputAddition());
        check(toxin.bonuses().size() == 1 && cold.bonuses().size() == 1);
        check(toxin.info().compatible(Category.TRIDENT) && !toxin.info().compatible(Category.BOW));
        check(cold.info().compatible(Category.BOW) && cold.info().compatible(Category.CROSSBOW) && !cold.info().compatible(Category.TRIDENT));
        for (var id : List.of("shattered_venom_doll", "winter_talisman")) {
            var ordinary = catalog.card("yagens_attributes:" + id, 5);
            check(ordinary.info().compatible(Category.BOW) && ordinary.info().compatible(Category.SWORD));
            close(.6, ordinary.bonuses().get(Stats.STATUS_CHANCE).addition());
        }
        check(!toxin.info().conflicts(catalog.card("yagens_attributes:shattered_venom_doll", 5).info(), Category.SWORD));
        check(!cold.info().conflicts(catalog.card("yagens_attributes:winter_talisman", 5).info(), Category.BOW));
        check(toxin.info().conflicts(catalog.card("yagens_attributes:toxin_addition", 5).info(), Category.SWORD));
        check(cold.info().conflicts(catalog.card("yagens_attributes:cold_addition", 5).info(), Category.BOW));
        close(1.2, catalog.card("yagens_attributes:bone_finger_box", 5).bonuses().get(Stats.CRITICAL_CHANCE).addition());
        close(.9, catalog.card("yagens_attributes:gaze_of_pain", 5).bonuses().get(Stats.STATUS_CHANCE).addition());
        var steel = catalog.card("yagens_attributes:galvanized_bone_finger_box", 10);
        close(1.1, steel.bonuses().get(Stats.CRITICAL_CHANCE).addition());
        var critical = steel.info().details().conditional().get(0);
        close(4.4, Stats.resolve(2, Stats.CRITICAL_DAMAGE, List.of(critical.source(4))));
        check(steel.info().compatible(Category.TRIDENT) && !steel.info().compatible(Category.BOW));
        check(steel.info().conflicts(catalog.card("yagens_attributes:bone_finger_box", 5).info()));
        check(!steel.info().conflicts(catalog.card("yagens_attributes:blood_rush", 10).info()));
        var aptitude = catalog.card("yagens_attributes:galvanized_gaze_of_pain", 10);
        close(.8, aptitude.bonuses().get(Stats.STATUS_CHANCE).addition());
        check(aptitude.info().compatible(Category.BOW) && !aptitude.info().compatible(Category.TRIDENT));
        check(aptitude.info().conflicts(catalog.card("yagens_attributes:gaze_of_pain", 5).info()));
        var scope = catalog.card("yagens_attributes:galvanized_argon_scope", 10);
        check(scope.bonuses().isEmpty());
        check(scope.info().compatible(Category.BOW) && scope.info().compatible(Category.CROSSBOW) && !scope.info().compatible(Category.TRIDENT));
        check(scope.info().conflicts(catalog.card("yagens_attributes:argon_scope", 5).info()));
        var scopeEffects = scope.info().details().conditional();
        var scopeHit = scopeEffects.get(0);
        var scopeKill = scopeEffects.get(1);
        check(scopeHit.condition().trigger() == ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS && scopeHit.condition().maxStacks() == 1);
        check(scopeKill.condition().trigger() == ConditionalModifier.Trigger.RANGED_WEAK_POINT_KILLS && scopeKill.condition().maxStacks() == 5);
        check(scopeHit.condition().durationTicks() == 240 && scopeKill.condition().durationTicks() == 240);
        close(4.2, Stats.resolve(1, Stats.CRITICAL_CHANCE, List.of(scopeHit.source(1), scopeKill.source(5))));
        var weakPointDecay = scopeKill.condition();
        check(weakPointDecay.decay() == ConditionalModifier.Decay.ONE_AT_A_TIME);
        var weakPointStacks = ConditionalModifier.Counter.EMPTY.hit(100, weakPointDecay).hit(120, weakPointDecay);
        check(weakPointStacks.count(359, weakPointDecay) == 2 && weakPointStacks.count(360, weakPointDecay) == 1
                && weakPointStacks.count(599, weakPointDecay) == 1 && weakPointStacks.count(600, weakPointDecay) == 0);
        var direct = aptitude.info().details().conditional().get(0);
        check(direct.source(0, 3).isEmpty() && direct.source(2, 0).isEmpty());
        close(2.4, direct.source(2, 3).get(Stats.DIRECT_DAMAGE).addition());
        var profile = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.IMPACT, 1D));
        var sources = List.of(catalog.card("yagens_attributes:universal_nanocube", 5).bonuses(), direct.source(2, 3));
        close(43, Combat.calculate(10, profile, sources, Map.of(), () -> .99).damage());
        close(32.25, Combat.calculate(10, profile, sources, Map.of(), () -> .99).damage() * .75);
        close(14.25, Combat.statusBases(10, profile, sources, 0, 0, .75).get(DamageType.SLASH));
        var rule = critical.condition(); var counter = ConditionalModifier.Counter.EMPTY;
        for (int i = 0; i < 6; i++) counter = counter.hit(100, rule);
        check(counter.count(499, rule) == 4 && counter.count(500, rule) == 3);
        check(counter.count(900, rule) == 2 && counter.count(1300, rule) == 1 && counter.count(1700, rule) == 0);
        counter = counter.hit(901, rule);
        check(counter.count(1300, rule) == 3 && counter.count(1301, rule) == 2);
        var legacy = new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_KILLS, 20, 4);
        check(new ConditionalModifier.Counter(4, 500).count(500, legacy) == 0);
        for (var definition : catalog.definitions().values()) if (definition.rarity() == Rarity.GALVANIZED) {
            for (var effect : definition.entries()) if (effect.condition().trigger().timed() && effect.condition().maxStacks() > 1) {
                var decay = effect.condition(); check(decay.decay() == ConditionalModifier.Decay.ONE_AT_A_TIME);
                var full = new ConditionalModifier.Counter(decay.maxStacks(), 100);
                check(full.count(99, decay) == decay.maxStacks());
                for (int lost = 1; lost <= decay.maxStacks(); lost++) {
                    long expiry = 100L + (long) (lost - 1) * decay.durationTicks();
                    check(full.count(expiry, decay) == decay.maxStacks() - lost);
                }
                var refreshed = full.hit(101, decay);
                check(refreshed.count(100 + decay.durationTicks(), decay) == decay.maxStacks());
            }
        }
        var independentlyTimed = catalog.definitions().entrySet().stream()
                .filter(entry -> entry.getValue().entries().stream().anyMatch(effect -> effect.condition().decay() == ConditionalModifier.Decay.INDEPENDENT))
                .map(Map.Entry::getKey).toList();
        check(independentlyTimed.equals(List.of("yagens_attributes:berserker_fury")));
        check(AttackKind.MELEE.credits(ConditionalModifier.Trigger.MELEE_KILLS));
        check(!AttackKind.MELEE.credits(ConditionalModifier.Trigger.RANGED_KILLS));
        check(AttackKind.RANGED.credits(ConditionalModifier.Trigger.RANGED_KILLS));
        check(!AttackKind.RANGED.credits(ConditionalModifier.Trigger.MELEE_KILLS));
        for (var trigger : ConditionalModifier.Trigger.values()) check(!AttackKind.UNKNOWN.credits(trigger));
        AttackKind.UNKNOWN.validateWeapon(null); AttackKind.MELEE.validateWeapon(UUID.randomUUID());
        rejects(() -> AttackKind.MELEE.validateWeapon(null));
        rejects(() -> AttackKind.UNKNOWN.validateWeapon(UUID.randomUUID()));
        var irregular = new ConditionalModifier(Stats.DIRECT_DAMAGE, .07,
                new ConditionalModifier.Condition(ConditionalModifier.Trigger.MELEE_KILLS, 7, 3, true, ConditionalModifier.Decay.ALL_AT_ONCE));
        close(.42, irregular.source(2, 3).get(Stats.DIRECT_DAMAGE).addition());
        rejects(() -> direct.source(3, 1)); rejects(() -> direct.source(1, 14));
        rejects(() -> new ConditionalModifier.Condition(ConditionalModifier.Trigger.TARGET_STATUS_TYPES, 0, 0, true, ConditionalModifier.Decay.ONE_AT_A_TIME));
        System.out.println("Passed " + checks + " Prime/Galvanized scope, direct-damage isolation and decay checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Galvanized expansion check " + checks); }
    private static void close(double expected, double actual) { checks++; if (Math.abs(expected - actual) > 1e-9) throw new AssertionError(expected + " != " + actual); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
