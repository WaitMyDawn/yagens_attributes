package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.ShieldRules;

public final class ShieldAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(Registries.ATTRIBUTE, YagensAttributes.MODID);
    public static final DeferredHolder<Attribute, Attribute> MAX_SHIELD = REGISTER.register("max_shield", () ->
            new RangedAttribute("attribute.yagens_attributes.max_shield", 0, 0, ShieldRules.MAX).setSyncable(true));
    /** Recharge points per twenty game ticks. */
    public static final DeferredHolder<Attribute, Attribute> SHIELD_REGEN = REGISTER.register("shield_regen", () ->
            new RangedAttribute("attribute.yagens_attributes.shield_regen", 2, 0, 20480).setSyncable(true));
    private ShieldAttributes() {}
    public static void attributes(EntityAttributeModificationEvent event) {
        for (var type : event.getTypes()) { event.add(type, MAX_SHIELD); event.add(type, SHIELD_REGEN); }
    }
}
