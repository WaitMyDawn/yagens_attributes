package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yagen.waitmydawn.YagensAttributes;

public class ImpactStatusEffect extends MobEffect {
    public ImpactStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_move"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_atk"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

}
