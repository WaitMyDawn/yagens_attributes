package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GalvanizedChamberMod extends BuiltinMod {
    public GalvanizedChamberMod() { super("galvanized_chamber"); }
    @Override public ModDefinition definition() {
        return define(Rarity.GALVANIZED, Polarity.MADURAI, 6, 10, Set.of(Category.RANGED, Category.TRIDENT), "yagens_attributes:item/mods/multishot",
                ModEffects.bonus(Stats.MULTISHOT, .8 / 11),
                ModEffects.conditional(Stats.MULTISHOT, .3 / 11, new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_KILLS, 20, 5)))
                .withStyle(ModStyle.family("yagens_attributes:split_chamber"));
    }
}
