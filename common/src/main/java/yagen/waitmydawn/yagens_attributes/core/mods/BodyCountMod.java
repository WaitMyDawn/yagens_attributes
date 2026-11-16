package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;

import yagen.waitmydawn.yagens_attributes.core.*;

import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class BodyCountMod extends BuiltinMod {
    public BodyCountMod() {
        super("body_count");
    }

    @Override
    public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.NARAMON, 4, 5, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/body_count",
                ModEffects.flat(Stats.COMBO_DURATION, 2.0));
    }
}
