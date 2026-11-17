package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ConditionOverloadMod extends BuiltinMod {
    public ConditionOverloadMod() { super("condition_overload"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 10, 5, Set.of(Category.MELEE), "yagens_attributes:item/mods/condition_overload",
                ModEffects.conditional(Stats.DAMAGE, .8 / 6, ConditionalModifier.Condition.STATUS_TYPES));
    }
}
