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

public class ImpactStatusEffect extends MobEffect {
    public ImpactStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final Map<LivingEntity, Integer> lastAmplifier = new WeakHashMap<>();

    private void updateModifiers(LivingEntity pLivingEntity, int pAmplifier) {
        AttributeInstance move = pLivingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attack_speed = pLivingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (move == null || attack_speed == null) return;
        move.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_move"));
        attack_speed.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_atk"));

        if (pAmplifier != -1) {
            double value = -0.95;
            move.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_move"),
                    value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attack_speed.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "impact_atk"),
                    value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;

        MobEffectInstance instance = pLivingEntity.getEffect(MobEffectRegistry.IMPACT_STATUS);
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
