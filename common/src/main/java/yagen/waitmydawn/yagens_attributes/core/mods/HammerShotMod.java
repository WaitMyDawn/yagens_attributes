package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class HammerShotMod extends BuiltinMod {
    public HammerShotMod() { super("hammer_shot"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.VAZARIN, 6, 3, Set.of(Category.RANGED, Category.TRIDENT),
                "yagens_attributes:item/mods/hammer_shot", ModEffects.bonus(Stats.CRITICAL_DAMAGE, .15), ModEffects.bonus(Stats.STATUS_CHANCE, .2));
    }
}
