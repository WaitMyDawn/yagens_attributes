package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ToxinAdditionMod extends BuiltinMod {
    static final String FAMILY = "yagens_attributes:toxin_addition";

    public ToxinAdditionMod() { super("toxin_addition"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.NARAMON, 6, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/toxin_status",
                ModEffects.element(DamageType.TOXIN, 0.15)).withStyle(ModStyle.family(FAMILY));
    }
}
