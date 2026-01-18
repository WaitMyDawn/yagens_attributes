package yagen.waitmydawn.compat;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

import static yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod.ATTRIBUTE_SET;

public class ISSAttributeSet {
    public static void addAttributeSet() {
        ATTRIBUTE_SET.put(AttributeRegistry.MAX_MANA.value(), 150.0);
        ATTRIBUTE_SET.put(AttributeRegistry.SPELL_POWER.value(), 0.15);
        ATTRIBUTE_SET.put(AttributeRegistry.COOLDOWN_REDUCTION.value(), 0.2);
        ATTRIBUTE_SET.put(AttributeRegistry.CAST_TIME_REDUCTION.value(), 0.2);
        ATTRIBUTE_SET.put(AttributeRegistry.NATURE_SPELL_POWER.value(), 0.2);
    }
}
