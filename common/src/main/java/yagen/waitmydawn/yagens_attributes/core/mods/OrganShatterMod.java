package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class OrganShatterMod extends BuiltinMod {
    public OrganShatterMod() { super("nameless_glove"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/nameless_glove",
                ModEffects.bonus(Stats.CRITICAL_DAMAGE, 0.15));
    }
}
