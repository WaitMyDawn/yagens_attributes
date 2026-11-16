package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ReachMod extends BuiltinMod {
    public ReachMod() { super("reach"); }

    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.MADURAI, 4, 3, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/reach",
                ModEffects.flat(Stats.MELEE_RANGE, 0.375)).withStyle(ModStyle.family("yagens_attributes:reach"));
    }
}
