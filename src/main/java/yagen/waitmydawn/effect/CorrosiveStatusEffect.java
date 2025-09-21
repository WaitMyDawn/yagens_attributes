package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;

public class CorrosiveStatusEffect extends MobEffect {
    public CorrosiveStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor"),
                -0.1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor_toughness"),
                -0.1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }
}
