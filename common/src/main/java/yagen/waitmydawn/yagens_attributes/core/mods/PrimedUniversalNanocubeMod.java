package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

public final class PrimedUniversalNanocubeMod extends BuiltinMod {
    public PrimedUniversalNanocubeMod() { super("primed_universal_nanocube"); }
    @Override public ModDefinition definition() {
        return define(Rarity.LEGENDARY, Polarity.MADURAI, 4, 10, Set.of(Category.WEAPON),
                "yagens_attributes:item/mods/universal_nanocube", ModEffects.bonus(Stats.DAMAGE, .15))
                .withStyle(new ModStyle(ModStyle.Visual.OROKIN, Set.of("yagens_attributes:nanocube")));
    }
}
