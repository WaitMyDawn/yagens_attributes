package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GalvanizedBoneFingerBoxMod extends BuiltinMod {
    public GalvanizedBoneFingerBoxMod() { super("galvanized_bone_finger_box"); }
    @Override public ModDefinition definition() {
        return define(Rarity.GALVANIZED, Polarity.MADURAI, 2, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/bone_finger_box", ModEffects.bonus(Stats.CRITICAL_CHANCE, .1),
                ModEffects.conditional(Stats.CRITICAL_DAMAGE, .3 / 11,
                        new ConditionalModifier.Condition(ConditionalModifier.Trigger.MELEE_KILLS, 20, 4,
                                false, ConditionalModifier.Decay.ONE_AT_A_TIME)))
                .withStyle(new ModStyle(ModStyle.Visual.GALVANIZED, Set.of("yagens_attributes:critical_choice")));
    }
}
