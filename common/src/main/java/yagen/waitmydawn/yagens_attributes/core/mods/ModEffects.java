package yagen.waitmydawn.yagens_attributes.core.mods;

import yagen.waitmydawn.yagens_attributes.core.*;
import static yagen.waitmydawn.yagens_attributes.core.ModDefinition.*;

public final class ModEffects {
    private ModEffects() {}
    public static Effect conditional(String stat, double value, ConditionalModifier.Condition condition) {
        return new Effect(stat, Operation.ADDITION, value, Basis.FLAT,
                "Fractional Addition per qualifying target status type or active qualifying-kill stack at level 0. Scales with (level + 1); duration_seconds and max_stacks do not scale with rank.", condition);
    }
    public static Effect bonus(String stat, double value) {
        return new Effect(stat, Operation.ADDITION, value, Basis.FLAT,
                "Fractional additive bonus at level 0. A value of 1.0 means +100%; scales with (level + 1).");
    }
    public static Effect flat(String stat, double value) {
        return new Effect(stat, Operation.ADDIN, value, Basis.FLAT,
                "Flat initial addition at level 0, before multipliers. Seconds for combo duration, blocks for melee range; scales with (level + 1).");
    }
    public static Effect element(DamageType type, double value) {
        return new Effect("damage." + type.key(), Operation.ADDITION, value, Basis.ATTACK_DAMAGE,
                "Additive elemental coefficient relative to total original attack damage, including when this element is absent. A value of 1.0 means 100%; scales with (level + 1).");
    }
    public static Effect combo(String stat, double value) {
        return new Effect(stat, Operation.ADDITION, value, Basis.FLAT,
                "Fractional additive chance bonus per combo tier above 1x at level 0. Multiplied by (level + 1) and (combo_multiplier - 1); contributes zero at 1x.");
    }
}
