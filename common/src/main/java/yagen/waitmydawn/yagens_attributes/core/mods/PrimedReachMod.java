package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedReachMod extends BuiltinMod {
    public PrimedReachMod() { super("primed_reach"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.MADURAI, 4, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/reach", ModEffects.flat(Stats.MELEE_RANGE, 3D / 11))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of("yagens_attributes:reach")));
    }
}
