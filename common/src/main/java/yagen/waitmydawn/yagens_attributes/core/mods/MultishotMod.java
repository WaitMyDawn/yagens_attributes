package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class MultishotMod extends BuiltinMod {
    public MultishotMod() { super("multishot"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 10, 5, Set.of(Category.RANGED, Category.TRIDENT),
                "yagens_attributes:item/mods/multishot",
                ModEffects.bonus(Stats.MULTISHOT, 0.15)).withStyle(ModStyle.family("yagens_attributes:split_chamber"));
    }
}
