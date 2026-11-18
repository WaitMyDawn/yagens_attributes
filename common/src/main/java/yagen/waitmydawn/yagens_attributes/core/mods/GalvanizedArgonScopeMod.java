package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;
import yagen.waitmydawn.yagens_attributes.core.ModDefinition;
import yagen.waitmydawn.yagens_attributes.core.ModStyle;
import yagen.waitmydawn.yagens_attributes.core.Stats;

import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GalvanizedArgonScopeMod extends BuiltinMod {
    public GalvanizedArgonScopeMod() { super("galvanized_argon_scope"); }

    @Override public ModDefinition definition() {
        var hit = new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS, 12, 1);
        var kill = new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_WEAK_POINT_KILLS, 12, 5,
                false, ConditionalModifier.Decay.ONE_AT_A_TIME);
        return define(Rarity.GALVANIZED, Polarity.MADURAI, 2, 10, Set.of(Category.RANGED), ArgonScopeMod.TEXTURE,
                new ModDefinition.Effect(Stats.CRITICAL_CHANCE, ModDefinition.Operation.ADDITION, 1.2 / 11,
                        ModDefinition.Basis.FLAT, "Fractional critical chance Addition at rank 0 while the weak point hit buff is active. Scales with (rank + 1).", hit),
                new ModDefinition.Effect(Stats.CRITICAL_CHANCE, ModDefinition.Operation.ADDITION, .4 / 11,
                        ModDefinition.Basis.FLAT, "Fractional critical chance Addition at rank 0 per active weak point kill stack. Scales with (rank + 1).", kill))
                .withStyle(new ModStyle(ModStyle.Visual.GALVANIZED, Set.of(ArgonScopeMod.FAMILY)));
    }
}
