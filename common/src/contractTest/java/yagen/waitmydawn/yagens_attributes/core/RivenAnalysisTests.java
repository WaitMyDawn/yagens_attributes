package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class RivenAnalysisTests {
    private static int checks;
    private RivenAnalysisTests() {}

    public static void run() {
        var defaults = BuiltinRivens.rules();
        for (var profile : BuiltinCombat.weapons().values()) for (var layout : defaults.layouts()) {
            var result = RivenAnalysis.analyze(defaults, profile, layout, 8);
            check(result.possible()); close(.25, result.layoutChance()); conservation(result, layout);
            for (var outcome : result.outcomes()) {
                var trait = defaults.traits().get(outcome.id());
                check(trait.accepts(profile));
                check(outcome.positiveChance() >= 0 && outcome.positiveChance() <= 1);
                check(outcome.negativeChance() >= 0 && outcome.negativeChance() <= 1);
                if (!trait.negative() || layout.negativeCount() == 0) close(0, outcome.negativeChance());
            }
        }
        var profile = BuiltinCombat.weapons().get("minecraft:diamond_sword");
        var reservedTraits = new LinkedHashMap<String, RivenRules.Trait>();
        reservedTraits.put("unrelated.first", trait(Stats.DAMAGE, true, false));
        reservedTraits.put("irregular-alias", trait(Stats.DAMAGE, true, false));
        reservedTraits.put("only.remaining", trait(Stats.STATUS_CHANCE, true, false));
        reservedTraits.put("q", trait(Stats.CRITICAL_CHANCE, true, true));
        var constrained = rules(reservedTraits);
        var layout = constrained.layouts().get(2);
        var result = RivenAnalysis.analyze(constrained, profile, layout, 0);
        close(.5, outcome(result, "unrelated.first").positiveChance());
        close(.5, outcome(result, "irregular-alias").positiveChance());
        close(1, outcome(result, "only.remaining").positiveChance());
        close(0, outcome(result, "q").positiveChance()); close(1, outcome(result, "q").negativeChance());
        check(!RivenAnalysis.analyze(constrained, profile, constrained.layouts().get(3), 0).possible());
        var mixed = new LinkedHashMap<>(reservedTraits);
        mixed.put("negative-only", trait(Stats.STATUS_DURATION, false, true));
        mixed.put("dual", trait(Stats.COMBO_DURATION, true, true));
        mixed.put("neg-alias", trait(Stats.CRITICAL_CHANCE, false, true));
        compareReference(constrained, profile); compareReference(rules(mixed), profile);
        var empty = rules(Map.of("scoped", new RivenRules.Trait(Stats.FIRE_RATE, ModDefinition.Operation.ADDITION, ModDefinition.Basis.FLAT,
                Set.of(ModInfo.Category.RANGED), 1, 1, true, true, 0, "Ranged fixture")));
        check(!RivenAnalysis.analyze(empty, profile, empty.layouts().get(0), 0).possible());
        var thresholdTraits = new LinkedHashMap<>(reservedTraits);
        thresholdTraits.put("exact-quarter", physical(DamageType.IMPACT));
        thresholdTraits.put("above-quarter", physical(DamageType.PUNCTURE));
        var threshold = rules(thresholdTraits);
        var quarter = new WeaponProfile(.15, 2, .15, 6, Map.of(DamageType.IMPACT, .25, DamageType.PUNCTURE, .75), 7,
                ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE), 1, 1, 1.35);
        result = RivenAnalysis.analyze(threshold, quarter, threshold.layouts().get(0), 8);
        check(result.outcomes().stream().noneMatch(row -> row.id().equals("exact-quarter")));
        check(result.outcomes().stream().anyMatch(row -> row.id().equals("above-quarter")));
        compareReference(threshold, quarter);
        var aliases = new LinkedHashMap<String, RivenRules.Trait>();
        var stats = List.of(Stats.DAMAGE, Stats.CRITICAL_CHANCE, Stats.STATUS_CHANCE, Stats.STATUS_DURATION);
        for (int i = 0; i < 64; i++) aliases.put("arbitrary." + i, trait(stats.get(i % 4), true, true));
        var aliasRules = rules(aliases);
        result = RivenAnalysis.analyze(aliasRules, profile, aliasRules.layouts().get(3), 8);
        conservation(result, aliasRules.layouts().get(3));
        for (var row : result.outcomes()) { close(3D / 64, row.positiveChance()); close(1D / 64, row.negativeChance()); }
        for (int i = 0; i < 96; i++) {
            var oldRandom = new Random(i); var newRandom = new Random(i);
            var oldRoll = referenceRoll(rules(mixed), profile, oldRandom);
            var original = new RivenRoll("irregular:without_weapon_hint", profile.category(), ModInfo.Polarity.MADURAI, profile.rivenDisposition(),
                    List.of(new RivenRoll.Line("unrelated.first", 0, false), new RivenRoll.Line("only.remaining", 0, false)));
            var newRoll = rules(mixed).cycle(original, profile, newRandom::nextInt, newRandom::nextDouble);
            check(oldRoll.lines().equals(newRoll.lines())); check(original.polarity() == newRoll.polarity());
        }
        endpoints(defaults);
        rejects(() -> RivenAnalysis.analyze(defaults, profile, defaults.layouts().get(0), 9));
        rejects(() -> RivenAnalysis.analyze(defaults, profile, new RivenRules.Factors(2, 0, .8, 0), 0));
        System.out.println("Passed " + checks + " exact Riven probability, aliases, reservation, sampling parity and range checks");
    }

    private static RivenRules rules(Map<String, RivenRules.Trait> traits) {
        var defaults = BuiltinRivens.rules();
        return new RivenRules(10, 8, .9, 1.1, defaults.layouts(), traits, defaults.texture());
    }
    private static RivenRules.Trait trait(String stat, boolean positive, boolean negative) {
        return new RivenRules.Trait(stat, stat.equals(Stats.COMBO_DURATION) ? ModDefinition.Operation.ADDIN : ModDefinition.Operation.ADDITION,
                ModDefinition.Basis.FLAT, Set.of(ModInfo.Category.WEAPON), 1, 1, positive, negative, 0, "Independent analysis fixture");
    }
    private static RivenRules.Trait physical(DamageType type) {
        return new RivenRules.Trait("damage." + type.key(), ModDefinition.Operation.ADDITION, ModDefinition.Basis.FLAT,
                Set.of(ModInfo.Category.WEAPON), 1, 1, true, true, .25, "Strict physical-weight fixture");
    }
    private static RivenAnalysis.Outcome outcome(RivenAnalysis.Result result, String id) {
        return result.outcomes().stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow();
    }
    private static void conservation(RivenAnalysis.Result result, RivenRules.Factors layout) {
        close(layout.positiveCount(), result.outcomes().stream().mapToDouble(RivenAnalysis.Outcome::positiveChance).sum());
        close(layout.negativeCount(), result.outcomes().stream().mapToDouble(RivenAnalysis.Outcome::negativeChance).sum());
    }

    private static boolean completes(RivenRules rules, WeaponProfile profile, Set<String> used, int positive, int negative) {
        var p = new HashSet<String>(); var n = new HashSet<String>();
        for (var trait : rules.traits().values()) if (trait.accepts(profile) && !used.contains(trait.stat())) {
            if (trait.positive()) p.add(trait.stat()); if (trait.negative()) n.add(trait.stat());
        }
        return negative == 0 ? p.size() >= positive : n.stream().anyMatch(stat -> p.size() - (p.contains(stat) ? 1 : 0) >= positive);
    }
    private static List<Map.Entry<String, RivenRules.Trait>> choices(RivenRules rules, WeaponProfile profile, Set<String> used, boolean negative, int p, int n) {
        return rules.traits().entrySet().stream().filter(entry -> entry.getValue().accepts(profile)
                && (negative ? entry.getValue().negative() : entry.getValue().positive()) && !used.contains(entry.getValue().stat()))
                .filter(entry -> { var reserved = new HashSet<>(used); reserved.add(entry.getValue().stat()); return completes(rules, profile, reserved, p, n); })
                .sorted(Map.Entry.comparingByKey()).toList();
    }
    private static void compareReference(RivenRules rules, WeaponProfile profile) {
        for (var layout : rules.layouts()) {
            var result = RivenAnalysis.analyze(rules, profile, layout, 8);
            if (!completes(rules, profile, Set.of(), layout.positiveCount(), layout.negativeCount())) { check(!result.possible()); continue; }
            var positive = new HashMap<String, Double>(); var negative = new HashMap<String, Double>();
            enumerate(rules, profile, layout, new ArrayList<>(), new HashSet<>(), 1, positive, negative);
            check(result.possible()); conservation(result, layout);
            for (var row : result.outcomes()) { close(positive.getOrDefault(row.id(), 0D), row.positiveChance()); close(negative.getOrDefault(row.id(), 0D), row.negativeChance()); }
            check(result.outcomes().size() == java.util.stream.Stream.concat(positive.keySet().stream(), negative.keySet().stream()).distinct().count());
        }
    }
    private static void enumerate(RivenRules rules, WeaponProfile profile, RivenRules.Factors layout, List<RivenRoll.Line> lines,
                                  Set<String> used, double probability, Map<String, Double> positive, Map<String, Double> negative) {
        if (lines.size() == layout.positiveCount() + layout.negativeCount()) {
            for (var line : lines) (line.negative() ? negative : positive).merge(line.trait(), probability, Double::sum);
            return;
        }
        boolean sign = lines.size() >= layout.positiveCount();
        var choices = choices(rules, profile, used, sign, sign ? 0 : layout.positiveCount() - lines.size() - 1, sign ? 0 : layout.negativeCount());
        for (var entry : choices) {
            var next = new ArrayList<>(lines); next.add(new RivenRoll.Line(entry.getKey(), 0, sign));
            var reserved = new HashSet<>(used); reserved.add(entry.getValue().stat());
            enumerate(rules, profile, layout, next, reserved, probability / choices.size(), positive, negative);
        }
    }
    private static RivenRoll referenceRoll(RivenRules rules, WeaponProfile profile, Random random) {
        var layout = rules.layouts().get(random.nextInt(rules.layouts().size()));
        var lines = new ArrayList<RivenRoll.Line>(); var used = new HashSet<String>();
        for (int sign = 0; sign < 2; sign++) {
            boolean negative = sign == 1; int count = negative ? layout.negativeCount() : layout.positiveCount();
            for (int i = 0; i < count; i++) {
                var pool = choices(rules, profile, used, negative, negative ? 0 : count - i - 1, negative ? 0 : layout.negativeCount());
                var chosen = pool.get(random.nextInt(pool.size()));
                lines.add(new RivenRoll.Line(chosen.getKey(), random.nextDouble(), negative)); used.add(chosen.getValue().stat());
            }
        }
        return new RivenRoll("irregular:without_weapon_hint", profile.category(), PolarityRules.FORMA_CHOICES.get(random.nextInt(3)), profile.rivenDisposition(), lines);
    }
    private static void endpoints(RivenRules rules) {
        for (var id : List.of("minecraft:diamond_sword", "minecraft:bow", "minecraft:trident")) {
            var profile = BuiltinCombat.weapons().get(id);
            for (int index = 0; index < rules.layouts().size(); index++) {
                int layout = index; int[] calls = {0};
                var original = new RivenRoll(id, profile.category(), ModInfo.Polarity.MADURAI, profile.rivenDisposition(),
                        List.of(new RivenRoll.Line(Stats.DAMAGE, 0, false), new RivenRoll.Line(Stats.CRITICAL_CHANCE, 0, false)));
                var sample = rules.cycle(original, profile, bound -> calls[0]++ == 0 ? layout : 0, () -> .5);
                for (int rank : List.of(0, 8)) for (double quality : List.of(0D, 1D)) {
                    var endpoint = new RivenRoll(id, profile.category(), sample.polarity(), profile.rivenDisposition(),
                            sample.lines().stream().map(line -> new RivenRoll.Line(line.trait(), quality, line.negative())).toList());
                    var compiled = rules.definition(endpoint).bonuses(rank);
                    var analysis = RivenAnalysis.analyze(rules, profile, rules.layouts().get(index), rank);
                    for (var line : endpoint.lines()) {
                        var row = outcome(analysis, line.trait()); var trait = rules.traits().get(line.trait()); var bonus = compiled.get(trait.stat());
                        double actual = trait.operation() == ModDefinition.Operation.ADDIN ? bonus.addin() : trait.basis() == ModDefinition.Basis.ATTACK_DAMAGE ? bonus.inputAddition() : bonus.addition();
                        var range = line.negative() ? row.negativeRange() : row.positiveRange();
                        close(quality == 0 ^ line.negative() ? range.minimum() : range.maximum(), actual);
                    }
                }
            }
        }
    }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-9); }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Riven analysis check " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected analysis rejection"); }
}
