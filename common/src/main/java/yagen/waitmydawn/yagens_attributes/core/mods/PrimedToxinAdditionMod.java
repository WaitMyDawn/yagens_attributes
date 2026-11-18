package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedToxinAdditionMod extends BuiltinMod {
    public PrimedToxinAdditionMod() { super("primed_toxin_addition"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.NARAMON, 6, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/shattered_venom_doll", ModEffects.element(DamageType.TOXIN, .15))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of(ToxinAdditionMod.FAMILY)));
    }
}
