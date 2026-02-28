package yagen.waitmydawn.api.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ServerConfigs;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ShieldHandler {
    private static final String SHIELD_REGEN_COOLDOWN = "yagens_attributes_shield_cooldown";
    private static final ResourceLocation VANILLA_ABSORPTION_MODIFIER =
            ResourceLocation.withDefaultNamespace("effect.absorption");
    private static final ResourceLocation ABSORPTION_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "absorption_modifier");

    @SubscribeEvent
    public static void onAbsorptionEffectAdded(MobEffectEvent.Added event) {
        if (ServerConfigs.BAN_SHIELD_MECHANISM.get()) return;
        if (event.getEntity().level().isClientSide) return;
        if (event.getEffectInstance().getEffect() == MobEffects.ABSORPTION) {
            LivingEntity entity = event.getEntity();

            float amountToAdd = 4.0F * (event.getEffectInstance().getAmplifier() + 1);
            addAbsorption(entity, amountToAdd);
        }
    }

    @SubscribeEvent
    public static void refreshShieldRegenCooldown(LivingDamageEvent.Post event) {
        if (ServerConfigs.BAN_SHIELD_MECHANISM.get()) return;
        if (event.getEntity().level().isClientSide) return;
        event.getEntity().getPersistentData().putInt(SHIELD_REGEN_COOLDOWN, 60);
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (ServerConfigs.BAN_SHIELD_MECHANISM.get()) return;
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide) return;

        AttributeInstance maxAbsAttr = entity.getAttribute(Attributes.MAX_ABSORPTION);
        AttributeInstance maxShieldAttr = entity.getAttribute(YAttributes.MAX_SHIELD);
        AttributeInstance shieldRegenAttr = entity.getAttribute(YAttributes.SHIELD_REGEN);

        if (maxAbsAttr != null && maxShieldAttr != null) {
            float currentAbsorption = entity.getAbsorptionAmount();
            double shieldCapacity = maxShieldAttr.getValue();
            double targetMax = Math.max(currentAbsorption, shieldCapacity);

            AttributeModifier existingMod = maxAbsAttr.getModifier(ABSORPTION_MODIFIER);
            boolean needsUpdate = true;

            if (existingMod != null) {
                if (Math.abs(existingMod.amount() - targetMax) < 0.01) {
                    needsUpdate = false;
                } else {
                    maxAbsAttr.removeModifier(ABSORPTION_MODIFIER);
                }
            }

            if (needsUpdate) {
                maxAbsAttr.addPermanentModifier(new AttributeModifier(
                        ABSORPTION_MODIFIER,
                        targetMax,
                        AttributeModifier.Operation.ADD_VALUE));
            }

            if (maxAbsAttr.getModifier(VANILLA_ABSORPTION_MODIFIER) != null) {
                maxAbsAttr.removeModifier(VANILLA_ABSORPTION_MODIFIER);
            }
        }

        if (maxShieldAttr != null && shieldRegenAttr != null && maxShieldAttr.getValue() > 0) {
            CompoundTag data = entity.getPersistentData();
            int cooldown = data.getInt(SHIELD_REGEN_COOLDOWN);

            if (cooldown > 0) {
                data.putInt(SHIELD_REGEN_COOLDOWN, cooldown - 1);
            } else {
                if (entity.getAbsorptionAmount() < maxShieldAttr.getValue()) {
                    float regenRate = (float) shieldRegenAttr.getValue();
                    entity.setAbsorptionAmount(Math.min((float) maxShieldAttr.getValue(), entity.getAbsorptionAmount() + regenRate));
                }
            }
        }
    }

    public static void addAbsorption(LivingEntity entity, float amount) {
        if (ServerConfigs.BAN_SHIELD_MECHANISM.get()) return;
        float currentAbsorption = entity.getAbsorptionAmount();
        float newTotalAbsorption = currentAbsorption + amount;

        AttributeInstance maxAbsAttr = entity.getAttribute(Attributes.MAX_ABSORPTION);
        AttributeInstance maxShieldAttr = entity.getAttribute(YAttributes.MAX_SHIELD);

        if (maxAbsAttr != null) {
            double requiredCap = newTotalAbsorption;
            if (maxShieldAttr != null) {
                requiredCap = Math.max(requiredCap, maxShieldAttr.getValue());
            }

            maxAbsAttr.removeModifier(ABSORPTION_MODIFIER);
            maxAbsAttr.addPermanentModifier(new AttributeModifier(
                    ABSORPTION_MODIFIER,
                    requiredCap,
                    AttributeModifier.Operation.ADD_VALUE));
        }

        entity.setAbsorptionAmount(newTotalAbsorption);
    }
}
