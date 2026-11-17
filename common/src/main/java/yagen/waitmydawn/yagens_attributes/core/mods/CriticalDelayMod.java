package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class CriticalDelayMod extends BuiltinMod {
    public CriticalDelayMod() { super("critical_delay"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.NARAMON, 4, 5, Set.of(Category.RANGED),
                "yagens_attributes:item/mods/critical_delay", ModEffects.bonus(Stats.CRITICAL_CHANCE, 2D / 6), ModEffects.bonus(Stats.FIRE_RATE, -.2 / 6))
                .withStyle(ModStyle.family("yagens_attributes:critical_choice"));
    }
}
