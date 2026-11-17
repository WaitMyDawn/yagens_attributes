package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ShatteredVenomDollMod extends BuiltinMod {
    public ShatteredVenomDollMod() { super("shattered_venom_doll"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/shattered_venom_doll",
                ModEffects.element(DamageType.TOXIN, 0.1),
                ModEffects.bonus(Stats.STATUS_CHANCE, 0.1)).withStyle(ModStyle.family("yagens_attributes:shattered_venom_doll"));
    }
}
