package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class HeatAdditionMod extends BuiltinMod {
    public HeatAdditionMod() { super("heat_addition"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.NARAMON, 6, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/heat_status",
                ModEffects.element(DamageType.HEAT, 0.15));
    }
}
