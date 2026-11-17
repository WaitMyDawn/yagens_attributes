package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class SpoiledStrikeMod extends BuiltinMod {
    public SpoiledStrikeMod() { super("spoiled_strike"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 3, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/spoiled_strike", ModEffects.bonus(Stats.DAMAGE, .25), ModEffects.bonus(Stats.ATTACK_SPEED, -.05));
    }
}
