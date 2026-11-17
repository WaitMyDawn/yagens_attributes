package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class BerserkerFuryMod extends BuiltinMod {
    public BerserkerFuryMod() { super("berserker_fury"); }
    @Override public ModDefinition definition() {
        return define(Rarity.RARE, Polarity.MADURAI, 4, 5, Set.of(Category.MELEE), "yagens_attributes:item/mods/berserker_fury",
                ModEffects.conditional(Stats.ATTACK_SPEED, .35 / 6,
                        new ConditionalModifier.Condition(ConditionalModifier.Trigger.MELEE_KILLS, 10, 2, false, ConditionalModifier.Decay.INDEPENDENT)))
                .withStyle(ModStyle.family("yagens_attributes:fury"));
    }
}
