package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class HunterMunitionsMod extends BuiltinMod {
    public HunterMunitionsMod() { super("hunter_munitions"); }
    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 5, Set.of(Category.RANGED), "yagens_attributes:item/mods/hunter_munitions",
                ModEffects.procChance(Stats.CRITICAL_SLASH_CHANCE, .05));
    }
}
