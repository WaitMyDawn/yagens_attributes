package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.registries.MobEffectRegistry;

public class FuryKillEffect extends MobEffect {

    public FuryKillEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        double value =
                ServerConfigs.MOD_RARE_BERSERKER_FURY.get() / 100
                        * (amplifier + 1);
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "fury_kill_killbonus");

        AttributeInstance instance = attributeMap.getInstance(Attributes.ATTACK_SPEED);
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
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "fury_kill_killbonus");

        AttributeInstance instance = attributeMap.getInstance(Attributes.ATTACK_SPEED);
        if (instance != null) {
            instance.removeModifier(resourceLocation);
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide()) return true;
        int left = pLivingEntity.getPersistentData().getInt("fury_kill_left");
        if (left <= 1) {
            if (pAmplifier > 0) {
                pLivingEntity.removeEffect(MobEffectRegistry.FURY_KILL);
                pLivingEntity.addEffect(new MobEffectInstance(
                        MobEffectRegistry.FURY_KILL,
                        ServerConfigs.MOD_RARE_BERSERKER_FURY_DURATION.get() * 20,
                        --pAmplifier,
                        false,
                        true,
                        true
                ));
            }
            pLivingEntity.getPersistentData().putInt("fury_kill_left", ServerConfigs.MOD_RARE_BERSERKER_FURY_DURATION.get() * 20);
        } else
            pLivingEntity.getPersistentData().putInt("fury_kill_left", left - 1);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
