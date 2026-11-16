package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class WeepingWoundsMod extends BuiltinMod {
    public WeepingWoundsMod() { super("weeping_wounds"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 5, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/weeping_wounds",
                ModEffects.combo(Stats.COMBO_STATUS_CHANCE, 0.4 / 6));
    }
}
