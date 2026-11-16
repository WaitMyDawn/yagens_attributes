package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class QuickeningMod extends BuiltinMod {
    public QuickeningMod() { super("quickening"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 6, 3, Set.of(Category.MELEE), "yagens_attributes:item/mod",
                ModEffects.bonus(Stats.ATTACK_SPEED, .1), ModEffects.bonus(Stats.COMBO_COUNT_CHANCE, .05));
    }
}
