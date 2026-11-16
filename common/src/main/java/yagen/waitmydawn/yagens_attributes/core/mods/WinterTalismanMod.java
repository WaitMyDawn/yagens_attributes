package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class WinterTalismanMod extends BuiltinMod {
    public WinterTalismanMod() { super("winter_talisman"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/winter_talisman",
                ModEffects.element(DamageType.COLD, 0.1),
                ModEffects.bonus(Stats.STATUS_CHANCE, 0.1));
    }
}
