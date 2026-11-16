package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ChargeCageMod extends BuiltinMod {
    public ChargeCageMod() { super("charge_cage"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/charge_cage",
                ModEffects.element(DamageType.ELECTRICITY, 0.1),
                ModEffects.bonus(Stats.STATUS_CHANCE, 0.1));
    }
}
