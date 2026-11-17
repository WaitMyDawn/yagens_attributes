package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class BloodRushMod extends BuiltinMod {
    public BloodRushMod() { super("blood_rush"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/blood_rush",
                ModEffects.combo(Stats.COMBO_CRITICAL_CHANCE, 0.4 / 11));
    }
}
