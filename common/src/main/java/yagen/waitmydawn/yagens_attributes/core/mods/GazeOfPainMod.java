package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GazeOfPainMod extends BuiltinMod {
    public GazeOfPainMod() { super("gaze_of_pain"); }

    @Override public ModDefinition definition() {
        return define(Rarity.COMMON, Polarity.VAZARIN, 4, 5, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/gaze_of_pain",
                ModEffects.bonus(Stats.STATUS_CHANCE, 0.15));
    }
}
