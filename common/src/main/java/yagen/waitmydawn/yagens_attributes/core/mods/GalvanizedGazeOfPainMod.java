package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class GalvanizedGazeOfPainMod extends BuiltinMod {
    public GalvanizedGazeOfPainMod() { super("galvanized_gaze_of_pain"); }
    @Override public ModDefinition definition() {
        return define(Rarity.GALVANIZED, Polarity.VAZARIN, 2, 10, Set.of(Category.RANGED),
                "yagens_attributes:item/mods/gaze_of_pain", ModEffects.bonus(Stats.STATUS_CHANCE, .8 / 11),
                ModEffects.conditional(Stats.DIRECT_DAMAGE, .4 / 11,
                        new ConditionalModifier.Condition(ConditionalModifier.Trigger.RANGED_KILLS, 20, 2,
                                true, ConditionalModifier.Decay.ONE_AT_A_TIME)))
                .withStyle(new ModStyle(ModStyle.Visual.GALVANIZED, Set.of("yagens_attributes:status_chance_choice")));
    }
}
