package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;

public record RivenRules(int baseCapacity, int maxLevel, double variationMin, double variationMax,
                         List<Factors> layouts, Map<String, Trait> traits, String texture) {
    public static final String TYPE = "yagens_attributes:riven";

    public record Factors(int positiveCount, int negativeCount, double positive, double negative) {
        public Factors {
            if (positiveCount < 2 || positiveCount > 3 || negativeCount < 0 || negativeCount > 1 || !Double.isFinite(positive)
                    || positive <= 0 || !Double.isFinite(negative) || negative > 0 || negativeCount == 0 && negative != 0 || negativeCount == 1 && negative >= 0)
                throw new IllegalArgumentException("Invalid Riven layout factors");
        }
    }

    public record Trait(String stat, ModDefinition.Operation operation, ModDefinition.Basis basis,
                        Set<ModInfo.Category> applicability,
                        double meleeBase, double rangedBase, boolean positive, boolean negative, double physicalMinimum,
                        String description) {
        public Trait {
            applicability = Set.copyOf(applicability);
            if (!Stats.KEYS.contains(stat) || operation != (stat.equals(Stats.COMBO_DURATION) ? ModDefinition.Operation.ADDIN : ModDefinition.Operation.ADDITION)
                    || !Double.isFinite(meleeBase) || meleeBase < 0 || !Double.isFinite(rangedBase) || rangedBase < 0
                    || applicability.isEmpty() || applicability.contains(ModInfo.Category.ALL) || !positive && !negative
                    || !Double.isFinite(physicalMinimum) || physicalMinimum < 0 || physicalMinimum > 1 || description == null || description.isBlank())
                throw new IllegalArgumentException("Invalid Riven trait");
            new ModDefinition.Effect(stat, operation, 0, basis, description);
        }

        public boolean accepts(WeaponProfile profile) {
            if (applicability.stream().noneMatch(category -> category.compatible(profile.category()))) return false;
            if (reference(profile) <= 0) return false;
            if (physicalMinimum == 0) return true;
            return profile.distribution().entrySet().stream().anyMatch(entry -> ("damage." + entry.getKey().key()).equals(stat) && entry.getValue() > physicalMinimum);
        }

        public double reference(WeaponProfile profile) {
            return reference(profile.category());
        }
        public double reference(ModInfo.Category category) { return ModInfo.Category.RANGED.contains(category) ? rangedBase : meleeBase; }
    }

    public RivenRules {
        layouts = List.copyOf(layouts);
        traits = Map.copyOf(traits);
        ModDefinition.id(texture);
        if (baseCapacity < 0 || maxLevel != 8 || baseCapacity > 1000 || !Double.isFinite(variationMin) || !Double.isFinite(variationMax)
                || variationMin <= 0 || variationMax < variationMin || variationMax > 2 || layouts.size() != 4 || traits.size() > 64 || traits.isEmpty()
                || layouts.stream().map(value -> value.positiveCount() + "/" + value.negativeCount()).distinct().count() != 4)
            throw new IllegalArgumentException("Invalid Riven rules");
        traits.forEach((id, trait) -> {
            if (id.isBlank() || id.length() > 80) throw new IllegalArgumentException("Invalid Riven trait ID");
        });
    }

    public static int stars(double disposition) {
        if (!Double.isFinite(disposition) || disposition != 0 && disposition < .5 || disposition > 1.55)
            throw new IllegalArgumentException("Invalid disposition");
        if (disposition <= 0) return 0;
        return disposition < .7 ? 1 : disposition < .9 ? 2 : disposition <= 1.1 ? 3 : disposition <= 1.3 ? 4 : 5;
    }

    public List<String> candidates(RivenRoll.Pool pool, Map<String, WeaponProfile> weapons) {
        return weapons.entrySet().stream().filter(entry -> entry.getValue().rivenDisposition() > 0 && pool.accepts(entry.getValue().category()))
                .map(Map.Entry::getKey).sorted().toList();
    }

    public RivenRoll roll(RivenRoll.Pool pool, Map<String, WeaponProfile> weapons, IntUnaryOperator randomInt, DoubleSupplier randomDouble) {
        var candidates = candidates(pool, weapons);
        if (candidates.isEmpty()) throw new IllegalArgumentException("item.yagens_attributes.unknown_riven.empty_pool");
        String weapon = candidates.get(randomInt.applyAsInt(candidates.size()));
        var profile = weapons.get(weapon);
        return rollWeapon(weapon, profile, randomInt, randomDouble);
    }

    public RivenRoll cycle(RivenRoll original, WeaponProfile profile, IntUnaryOperator randomInt, DoubleSupplier randomDouble) {
        var rolled = rollWeapon(original.weapon(), profile, randomInt, randomDouble);
        return new RivenRoll(original.weapon(), original.category(), original.polarity(), profile.rivenDisposition(), rolled.lines(), Math.addExact(original.cycles(), 1));
    }

    private RivenRoll rollWeapon(String weapon, WeaponProfile profile, IntUnaryOperator randomInt, DoubleSupplier randomDouble) {
        var factors = layouts.get(randomInt.applyAsInt(layouts.size()));
        var pool = new RivenPool(this, profile);
        var selected = new ArrayList<RivenRoll.Line>();
        long used = 0;
        for (int sign = 0; sign < 2; sign++) {
            boolean negative = sign == 1;
            int count = negative ? factors.negativeCount() : factors.positiveCount();
            for (int i = 0; i < count; i++) {
                var available = pool.choices(used, negative, negative ? 0 : count - i - 1, negative ? 0 : factors.negativeCount());
                if (available.isEmpty()) throw new IllegalArgumentException("Riven trait pool cannot fill layout");
                var chosen = available.get(randomInt.applyAsInt(available.size()));
                selected.add(new RivenRoll.Line(chosen.id(), randomDouble.getAsDouble(), negative));
                used |= chosen.stat();
            }
        }
        var polarities = PolarityRules.FORMA_CHOICES;
        return new RivenRoll(weapon, profile.category(), polarities.get(randomInt.applyAsInt(polarities.size())), profile.rivenDisposition(), selected);
    }

    double value(Trait trait, ModInfo.Category category, double disposition, Factors layout, boolean negative, int level, double quality) {
        double zeroRank = trait.reference(category) * disposition * (variationMin + (variationMax - variationMin) * quality)
                * (negative ? layout.negative() : layout.positive()) / (maxLevel + 1);
        return zeroRank * (level + 1);
    }

    public ModDefinition definition(RivenRoll roll) {
        int positive = (int) roll.lines().stream().filter(line -> !line.negative()).count(), negative = roll.lines().size() - positive;
        var factors = layouts.stream().filter(value -> value.positiveCount() == positive && value.negativeCount() == negative).findFirst().orElseThrow();
        var entries = new ArrayList<ModDefinition.Effect>();
        var used = new HashSet<String>();
        for (var line : roll.lines()) {
            var trait = traits.get(line.trait());
            if (trait == null || !used.add(trait.stat()) || trait.applicability().stream().noneMatch(category -> category.compatible(roll.category()))
                    || (line.negative() ? !trait.negative() : !trait.positive()))
                throw new IllegalArgumentException("Invalid rolled Riven trait");
            double amount = value(trait, roll.category(), roll.disposition(), factors, line.negative(), 0, line.quality());
            entries.add(new ModDefinition.Effect(trait.stat(), trait.operation(), amount, trait.basis(), trait.description()));
        }
        return new ModDefinition("mod.yagens_attributes.riven", ModInfo.Rarity.RIVEN, roll.polarity(), baseCapacity, 1, maxLevel,
                Set.of(roll.category()), texture, entries);
    }
}
