package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class BoneFingerBoxMod extends BuiltinMod {
    public BoneFingerBoxMod() { super("bone_finger_box"); }

    @Override public ModDefinition definition() {
        return define(Rarity.UNCOMMON, Polarity.MADURAI, 4, 5, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/bone_finger_box",
                ModEffects.bonus(Stats.CRITICAL_CHANCE, 0.2)).withStyle(ModStyle.family("yagens_attributes:critical_choice"));
    }
}
