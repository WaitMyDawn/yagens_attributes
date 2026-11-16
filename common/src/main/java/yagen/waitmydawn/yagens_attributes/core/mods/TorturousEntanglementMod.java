package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class TorturousEntanglementMod extends BuiltinMod {
    public TorturousEntanglementMod() { super("torturous_entanglement"); }

    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.NARAMON, 2, 10, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/torturous_entanglement",
                ModEffects.bonus(Stats.STATUS_DURATION, 0.1));
    }
}
