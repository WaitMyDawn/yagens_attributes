package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class SlashBoostMod extends BuiltinMod {
    public SlashBoostMod() { super("slash_boost"); }

    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:status/slash_status",
                ModEffects.bonus("damage.slash", 0.15));
    }
}
