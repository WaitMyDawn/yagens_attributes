package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PunctureBoostMod extends BuiltinMod {
    public PunctureBoostMod() { super("puncture_boost"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.NARAMON, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/puncture_status",
                ModEffects.bonus("damage.puncture", 0.15));
    }
}
