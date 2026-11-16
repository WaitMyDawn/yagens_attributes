package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import java.util.Map;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class BloodRushMod extends BuiltinMod {
    public BloodRushMod() { super("blood_rush"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/blood_rush",
                ModEffects.combo(Stats.COMBO_CRITICAL_CHANCE, 0.4 / 11))
                .withStyle(new ModStyle(ModStyle.Visual.NONE, Set.of(), Map.of(Category.TRIDENT, Set.of("yagens_attributes:trident_combo_critical_choice"))));
    }
}
