package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.Map;
import java.util.WeakHashMap;


public class CorrosiveStatusEffect extends MobEffect {
    public CorrosiveStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    private final Map<LivingEntity, Integer> lastAmplifier = new WeakHashMap<>();

    private void updateModifiers(LivingEntity pLivingEntity, int pAmplifier) {
        AttributeInstance armor = pLivingEntity.getAttribute(Attributes.ARMOR);
        AttributeInstance toughness = pLivingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armor == null || toughness == null) return;
        armor.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor"));
        toughness.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor_toughness"));

        if (pAmplifier != -1) {
            double value = -0.4;
            if (pAmplifier > 0 && pAmplifier < 3) {
                value = value - pAmplifier * 0.1;
            } else if (pAmplifier >= 3 && pAmplifier <= 8) {
                value = Math.max(-0.9, value - 0.1 - pAmplifier * 0.05);
            }
            armor.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor"),
                    value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            toughness.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "corrosive_armor_toughness"),
                    value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;

        MobEffectInstance instance = pLivingEntity.getEffect(MobEffectRegistry.CORROSIVE_STATUS);
        if (instance == null) return true;
        int duration = instance.getDuration();

        int prev = lastAmplifier.getOrDefault(pLivingEntity, -1);
        if (prev != pAmplifier) {
            updateModifiers(pLivingEntity, pAmplifier);
            lastAmplifier.put(pLivingEntity, pAmplifier);
        }

        if (duration <= 1) {
            updateModifiers(pLivingEntity, -1);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
