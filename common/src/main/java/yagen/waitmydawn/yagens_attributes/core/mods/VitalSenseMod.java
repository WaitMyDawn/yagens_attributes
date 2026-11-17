package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class VitalSenseMod extends BuiltinMod {
    public VitalSenseMod() { super("vital_sense"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.RANGED), "yagens_attributes:item/mods/vital_sense",
                ModEffects.bonus(Stats.CRITICAL_DAMAGE, .2));
    }
}
