package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class DriftingContactMod extends BuiltinMod {
    public DriftingContactMod() { super("drifting_contact"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.VAZARIN, 2, 3, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/drifting_contact",
                ModEffects.flat(Stats.COMBO_DURATION, 2.5),
                ModEffects.bonus(Stats.STATUS_CHANCE, 0.1));
    }
}
