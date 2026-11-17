package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class SpeedTriggerMod extends BuiltinMod {
    public SpeedTriggerMod() { super("speed_trigger"); }
    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 5, Set.of(Category.RANGED, Category.TRIDENT),
                "yagens_attributes:item/mods/speed_trigger", ModEffects.bonus(Stats.FIRE_RATE, .1));
    }
}
