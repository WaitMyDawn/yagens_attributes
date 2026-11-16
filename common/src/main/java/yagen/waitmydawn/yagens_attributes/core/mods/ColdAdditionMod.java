package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ColdAdditionMod extends BuiltinMod {
    public ColdAdditionMod() { super("cold_addition"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.VAZARIN, 6, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/cold_status",
                ModEffects.element(DamageType.COLD, 0.15));
    }
}
