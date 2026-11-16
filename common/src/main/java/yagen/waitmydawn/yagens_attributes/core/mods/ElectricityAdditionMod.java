package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ElectricityAdditionMod extends BuiltinMod {
    public ElectricityAdditionMod() { super("electricity_addition"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.NARAMON, 6, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/electricity_status",
                ModEffects.element(DamageType.ELECTRICITY, 0.15));
    }
}
