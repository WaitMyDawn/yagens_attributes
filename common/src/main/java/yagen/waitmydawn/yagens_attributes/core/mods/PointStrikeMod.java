package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PointStrikeMod extends BuiltinMod {
    public PointStrikeMod() { super("point_strike"); }
    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.MADURAI, 4, 5, Set.of(Category.RANGED), "yagens_attributes:item/mods/point_strike",
                ModEffects.bonus(Stats.CRITICAL_CHANCE, .25)).withStyle(ModStyle.family("yagens_attributes:critical_choice"));
    }
}
