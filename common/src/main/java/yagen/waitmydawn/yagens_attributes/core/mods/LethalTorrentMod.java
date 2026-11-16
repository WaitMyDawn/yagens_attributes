package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class LethalTorrentMod extends BuiltinMod {
    public LethalTorrentMod() { super("lethal_torrent"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 6, 5, Set.of(Category.RANGED, Category.TRIDENT), "yagens_attributes:item/mod",
                ModEffects.bonus(Stats.FIRE_RATE, .1), ModEffects.bonus(Stats.MULTISHOT, .1));
    }
}
