package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;
import yagen.waitmydawn.yagens_attributes.core.ModDefinition;
import yagen.waitmydawn.yagens_attributes.core.ModStyle;
import yagen.waitmydawn.yagens_attributes.core.Stats;

import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ArgonScopeMod extends BuiltinMod {
    static final String TEXTURE = "yagens_attributes:item/mods/argon_scope";
    static final String FAMILY = "yagens_attributes:argon_scope";

    public ArgonScopeMod() { super("argon_scope"); }

    @Override public ModDefinition definition() {
        var trigger = new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS, 1.5, 1,
                false, ConditionalModifier.Decay.ALL_AT_ONCE, true);
        var criticalChance = new ModDefinition.Effect(Stats.CRITICAL_CHANCE, ModDefinition.Operation.ADDITION, .225,
                ModDefinition.Basis.FLAT, "Fractional critical chance Addition at rank 0. The bonus applies to shots fired during the buff and scales with (rank + 1).", trigger);
        return define(Rarity.RARE, Polarity.MADURAI, 2, 5, Set.of(Category.RANGED), TEXTURE, criticalChance)
                .withStyle(ModStyle.family(FAMILY));
    }
}
