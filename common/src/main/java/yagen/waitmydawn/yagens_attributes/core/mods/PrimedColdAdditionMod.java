package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedColdAdditionMod extends BuiltinMod {
    public PrimedColdAdditionMod() { super("primed_cold_addition"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.VAZARIN, 6, 10, Set.of(Category.RANGED),
                "yagens_attributes:status/cold_status", ModEffects.element(DamageType.COLD, .15))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of("yagens_attributes:winter_talisman")));
    }
}
