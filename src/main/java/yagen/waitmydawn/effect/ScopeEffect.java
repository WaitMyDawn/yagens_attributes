package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ServerConfigs;

public class ScopeEffect extends MobEffect {

    public ScopeEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

//        this.addAttributeModifier(
//                YAttributes.CRITICAL_CHANCE,
//                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_effect"),
//                0.135,
//                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        double value =
                ServerConfigs.MOD_RARE_SCOPE.get() / 200
                        * (amplifier + 1);
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_effect");

        AttributeInstance instance = attributeMap.getInstance(YAttributes.CRITICAL_CHANCE);
        if (instance != null) {
            instance.removeModifier(resourceLocation);
            instance.addTransientModifier(
                    new AttributeModifier(
                            resourceLocation,
                            value,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            );
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_effect");

        AttributeInstance instance = attributeMap.getInstance(YAttributes.CRITICAL_CHANCE);
        if (instance != null) {
            instance.removeModifier(resourceLocation);
        }
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }
}
