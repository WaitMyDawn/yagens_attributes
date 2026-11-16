package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class ImpactBoostMod extends BuiltinMod {
    public ImpactBoostMod() { super("impact_boost"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.VAZARIN, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/impact_status",
                ModEffects.bonus("damage.impact", 0.15));
    }
}
