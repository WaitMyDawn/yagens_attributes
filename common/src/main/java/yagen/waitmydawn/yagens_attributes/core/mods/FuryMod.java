package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class FuryMod extends BuiltinMod {
    public FuryMod() { super("fury"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 5, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/fury",
                ModEffects.bonus(Stats.ATTACK_SPEED, 0.05)).withStyle(ModStyle.family("yagens_attributes:fury"));
    }
}
