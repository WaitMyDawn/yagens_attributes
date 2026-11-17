package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedShatteredVenomDollMod extends BuiltinMod {
    public PrimedShatteredVenomDollMod() { super("primed_shattered_venom_doll"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.NARAMON, 6, 10, Set.of(Category.MELEE),
                "yagens_attributes:item/mods/shattered_venom_doll", ModEffects.element(DamageType.TOXIN, .15))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of("yagens_attributes:shattered_venom_doll")));
    }
}
