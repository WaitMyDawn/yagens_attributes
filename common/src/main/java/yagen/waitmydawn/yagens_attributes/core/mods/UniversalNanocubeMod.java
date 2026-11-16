package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class UniversalNanocubeMod extends BuiltinMod {
    public UniversalNanocubeMod() { super("universal_nanocube"); }

    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.MADURAI, 2, 5, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/universal_nanocube",
                ModEffects.bonus(Stats.DAMAGE, 0.15)).withStyle(ModStyle.family("yagens_attributes:nanocube"));
    }
}
