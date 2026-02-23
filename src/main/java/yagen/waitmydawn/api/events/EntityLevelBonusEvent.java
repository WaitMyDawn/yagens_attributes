package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.entity.others.DarkDoppelgangerEntity;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

import static yagen.waitmydawn.api.mission.MissionHandler.isBoss;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class EntityLevelBonusEvent {
    @SubscribeEvent
    public static void entityJoinBonusEvent(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!ServerConfigs.IF_LEVEL_BONUS.get()) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!(livingEntity instanceof Enemy)) return;
        setBaseLevel(livingEntity);
        addBossLevel(livingEntity);
        levelBonusAttributes(livingEntity);
    }

    private static void setBaseLevel(LivingEntity entity) {
        AttributeInstance entityLevel = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;

        double LEVEL_PER_BLOCKS = 100;
        double dx = entity.getX();
        double dz = entity.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);
        double level = Math.min(Math.round(distance / LEVEL_PER_BLOCKS), 9999.0D);
        if (level == 0) return;
//        entityLevel.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "base_level"));
        if (entityLevel.getModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "base_level")) == null) {
            entityLevel.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "base_level"),
                    level,
                    AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void addBossLevel(LivingEntity entity) {
        AttributeInstance entityLevel = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;

        EntityType<?> type = entity.getType();
        if (isBoss(type) || type.is(Tags.EntityTypes.BOSSES)) {
            if (entityLevel.getModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "boss_level")) == null) {
                entityLevel.addPermanentModifier(new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "boss_level"),
                        50,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static void modifierEntityLevel(LivingEntity entity, AttributeModifier.Operation operation, double value, String path) {
        AttributeInstance entityLevel = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;
        entityLevel.addPermanentModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, path),
                value,
                operation));
        DataAttachmentRegistry.setShouldHeal(entity, true);
        levelBonusAttributes(entity);
    }

    public static void levelBonusAttributes(LivingEntity entity) {
        AttributeInstance entityLevel = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        AttributeInstance entityMaxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance entityArmor = entity.getAttribute(Attributes.ARMOR);
        AttributeInstance entityArmorToughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        AttributeInstance entityAttackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (entityLevel == null)
            return;
        double level = entityLevel.getValue();
        if (level == 1) return;

        double healthFactor;
        double armorAdd;
        double armorFactor;
        double armorToughnessFactor;
        double attackDamageFactor;

        if (level <= 100) {
            healthFactor = 0.5 * Math.exp(level / 25) + 0.48;
            armorAdd = level / 10;
            armorFactor = 1;
            attackDamageFactor = 1 + 0.01 * level;
        } else {
            healthFactor = 28 + 0.1 * Math.pow(level / 20, 2);
            armorAdd = 10;
            armorFactor = level / 100;
            attackDamageFactor = -8.6 * Math.exp(-level / 1500) + 10;
        }

        if (entityMaxHealth != null) {
            entityMaxHealth.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_max_health"));
            entityMaxHealth.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_max_health"),
                    healthFactor - 1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (entityArmor != null) {
            entityArmor.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_armor_add"));
            entityArmor.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_armor_multiply"));
            entityArmor.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_armor_add"),
                    armorAdd,
                    AttributeModifier.Operation.ADD_VALUE));
            entityArmor.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_armor_multiply"),
                    armorFactor - 1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (entityAttackDamage != null) {
            entityAttackDamage.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_attack_damage"));
            entityAttackDamage.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "level_bonus_attack_damage"),
                    attackDamageFactor - 1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (DataAttachmentRegistry.getShouldHeal(entity)) {
            entity.setHealth(entity.getMaxHealth());
            DataAttachmentRegistry.setShouldHeal(entity, false);
        }
    }
}
