package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.network.ColdPacket;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.Map;
import java.util.WeakHashMap;

public class ThermalSunderStatusEffect extends MobEffect {
    public ThermalSunderStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    public static void updateModifiers(LivingEntity pLivingEntity, double value) {
        AttributeInstance armor = pLivingEntity.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        armor.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder_armor"));
        if (value != -2) {
            armor.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder_armor"),
                    value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;

        MobEffectInstance instance = pLivingEntity.getEffect(MobEffectRegistry.THERMAL_SUNDER);
        if (instance == null) return true;
        int duration = instance.getDuration();
        if (duration <= 1) {
            updateModifiers(pLivingEntity, -2);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

}
