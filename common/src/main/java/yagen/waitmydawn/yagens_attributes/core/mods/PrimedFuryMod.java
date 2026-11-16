package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedFuryMod extends BuiltinMod {
    public PrimedFuryMod() { super("primed_fury"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.MADURAI, 4, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/fury", ModEffects.bonus(Stats.ATTACK_SPEED, .05))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of("yagens_attributes:fury")));
    }
}
