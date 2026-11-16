package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class FlameFeatherMod extends BuiltinMod {
    public FlameFeatherMod() { super("flame_feather"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.NARAMON, 6, 5, Set.of(Category.RANGED, Category.TRIDENT),
                "yagens_attributes:item/mods/flame_feather",
                ModEffects.bonus(Stats.FIRE_RATE, 0.15),
                ModEffects.bonus(Stats.DAMAGE, -0.025));
    }
}
