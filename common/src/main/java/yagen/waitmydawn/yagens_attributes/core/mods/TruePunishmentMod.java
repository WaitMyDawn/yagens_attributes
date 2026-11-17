package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class TruePunishmentMod extends BuiltinMod {
    public TruePunishmentMod() { super("true_punishment"); }
    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.ZENURIK, 4, 3, Set.of(Category.MELEE), "yagens_attributes:item/mods/true_punishment",
                ModEffects.bonus(Stats.COMBO_COUNT_CHANCE, .25), ModEffects.bonus(Stats.COMBO_DURATION, -.125));
    }
}
