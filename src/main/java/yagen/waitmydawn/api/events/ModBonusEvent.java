package yagen.waitmydawn.api.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod;
import yagen.waitmydawn.network.SyncComboPacket;
import yagen.waitmydawn.network.SyncPreShootCountPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.List;

import static yagen.waitmydawn.api.events.BowShootEvent.isHeadShot;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModBonusEvent {
    @SubscribeEvent
    public static void killBonusEvent(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack itemStack = player.getMainHandItem();
        if (!IModContainer.isModContainer(itemStack)) return;
        var container = IModContainer.get(itemStack);
        for (ModSlot slot : container.getActiveMods()) {
            if (slot.getMod().getModName().equals("multishot_galvanized_tool_mod")) {
                MultishotGalvanizedToolModBonus(player);
            } else if (slot.getMod().getModName().equals("scope_galvanized_tool_mod")) {
                if (source.getDirectEntity() instanceof AbstractArrow arrow)
                    if (isHeadShot(livingEntity, arrow, player))
                        ScopeGalvanizedToolModBonus(player);
            } else if (slot.getMod().getModName().equals("fury_kill_tool_mod")) {
                FuryKillModBonus(player, slot.getLevel());
            }

        }
    }

    @SubscribeEvent
    public static void damageBonusEvent(LivingDamageEvent.Post event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.level().isClientSide) return;
        if (!(arrow.getOwner() instanceof Player player)) return;

        if (isHeadShot(livingEntity, arrow, player)) {
            ItemStack itemStack = player.getMainHandItem();
            if (!IModContainer.isModContainer(itemStack)) return;
            var container = IModContainer.get(itemStack);
            for (ModSlot slot : container.getActiveMods()) {
                if (slot.getMod().getModName().equals("scope_tool_mod")) {
                    ScopeToolModBonus(player, slot.getLevel(), 2);
                    player.removeEffect(MobEffectRegistry.SCOPE_GALVANIZED);
                    return;
                } else if (slot.getMod().getModName().equals("scope_galvanized_tool_mod")) {
                    ScopeToolModBonus(player, slot.getLevel(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void comboBonusEvent(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        double comboBonusCC = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_CC.get())).getValue();
        double comboBonusSC = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_SC.get())).getValue();

        DataAttachmentRegistry.Combo old = player.getData(DataAttachmentRegistry.COMBO.get());
        if (old.leftDuration() > 0) {
            int comboLevel = old.getComboLevel();
            updateCCModifier(player, comboBonusCC * comboLevel);
            updateSCModifier(player, comboBonusSC * comboLevel);
            DataAttachmentRegistry.Combo updated = old.decrement();
            if (updated != old) {
                player.setData(DataAttachmentRegistry.COMBO.get(), updated);
                if (!player.level().isClientSide)
                    PacketDistributor.sendToPlayer((ServerPlayer) player,
                            new SyncComboPacket(updated));
            }
        } else {
            updateCCModifier(player, 0);
            updateSCModifier(player, 0);
            DataAttachmentRegistry.Combo updated = old.withCount(0);
            if (updated != old) {
                player.setData(DataAttachmentRegistry.COMBO.get(), updated);
//                if (!player.level().isClientSide)
                PacketDistributor.sendToPlayer((ServerPlayer) player,
                        new SyncComboPacket(updated));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void gatherPreShootCount(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        if (!(player.getOffhandItem().getItem() instanceof BowItem)) return;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ModCompat.isValidWarframeAbility(chest)) return;
        boolean isPreShoot = false;
        for (ModSlot slot : IModContainer.get(chest).getActiveMods()) {
            if (slot.getMod().getModName().equals("pre_shoot_armor_mod")) {
                isPreShoot = true;
                break;
            }
        }
        if (!isPreShoot) return;
        DataAttachmentRegistry.PreShoot preShoot = player.getData(DataAttachmentRegistry.PRE_SHOOT_COUNT.get());
        DataAttachmentRegistry.PreShoot updated = preShoot.modifierCount(2);
        player.setData(DataAttachmentRegistry.PRE_SHOOT_COUNT.get(), updated);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncPreShootCountPacket(updated));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void PreShootCountModifier(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance criticalChance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_CHANCE.get()));
        if (armor == null || criticalChance == null) return;
        armor.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pre_shoot_pain_armor"));
        criticalChance.removeModifier(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pre_shoot_pain_cc"));

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ModCompat.isValidWarframeAbility(chest)) return;
        boolean isPreShoot = false;
        boolean isCollaborative = false;
        for (ModSlot slot : IModContainer.get(chest).getActiveMods()) {
            if (slot.getMod().getModName().equals("pre_shoot_armor_mod")) {
                isPreShoot = true;
                break;
            }
        }
        for (ModSlot slot : IModContainer.get(chest).getActiveMods()) {
            if (slot.getMod().getModName().equals("collaborative_proficiency_armor_mod")) {
                isCollaborative = true;
                break;
            }
        }
        if (!isPreShoot) return;

        DataAttachmentRegistry.PreShoot preShoot = player.getData(DataAttachmentRegistry.PRE_SHOOT_COUNT.get()).setMorePainLevel(0);
        if (isCollaborative) preShoot = preShoot.modifierMorePainLevel(1);
        if (player.getOffhandItem().getItem() instanceof BowItem
                && player.getMainHandItem().getItem() instanceof SwordItem)
            preShoot = preShoot.modifierMorePainLevel(1);

        int painLevel = preShoot.getPainLevel();
        if (painLevel == 0) return;
        armor.addPermanentModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pre_shoot_pain_armor"),
                -0.25 * painLevel, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (painLevel >= 3 && isCollaborative) {
            DataAttachmentRegistry.Combo combo = player.getData(DataAttachmentRegistry.COMBO.get());
            criticalChance.addPermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pre_shoot_pain_cc"),
                    0.04 * (painLevel - 2) * (combo.getComboLevel() + 1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @SubscribeEvent
    public static void graceBonusEvent(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.tickCount % 4 != 0) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "grace_bonus");
        ResourceLocation MODIFIER_ID_SPEED = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "grace_overflow");
        AttributeInstance attributeInstanceSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstanceSpeed == null) return;
        if (chest == ItemStack.EMPTY) {
            removeGraceBonus(player, MODIFIER_ID);
            if (attributeInstanceSpeed.getModifier(MODIFIER_ID_SPEED) != null)
                attributeInstanceSpeed.removeModifier(MODIFIER_ID_SPEED);
            return;
        }
        Attribute attribute = GraceArmorMod.getGraceAbility(chest);
        AttributeInstance attributeInstance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
        if (attributeInstance == null) return;
        if (attributeInstance == Attributes.MOVEMENT_SPEED) {
            removeGraceBonus(player, MODIFIER_ID);
            return;
        } else if (attributeInstance.getModifier(MODIFIER_ID) != null)
            attributeInstance.removeModifier(MODIFIER_ID);
        if (attributeInstanceSpeed.getModifier(MODIFIER_ID_SPEED) != null)
            attributeInstanceSpeed.removeModifier(MODIFIER_ID_SPEED);

        if (!IModContainer.isModContainer(chest)) return;
        for (ModSlot slot : IModContainer.get(chest).getActiveMods()) {
            if (slot.getMod().getModName().equals("grace_armor_mod")) {
                if (attribute == Attributes.MOVEMENT_SPEED.value()) return;

                double limit = 0.1 + ServerConfigs.MOD_RARE_GRACEFULLY_SERPENTINE.get() * slot.getLevel();
                double overflow = player.getSpeed() - limit;
                if (overflow <= 0) return;
                double bonus = GraceArmorMod.getBonus(attribute, overflow);

                attributeInstanceSpeed.addPermanentModifier(
                        new AttributeModifier(MODIFIER_ID_SPEED, limit / player.getSpeed() - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                if (bonus != 0)
                    attributeInstance.addPermanentModifier(
                            new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));

                return;
            }
        }
    }

    private static void cleanUpReservoirBuffs(LivingEntity entity, Holder<MobEffect> targetEffect) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) return;

        DataAttachmentRegistry.ReservoirBuffs data = player.getData(DataAttachmentRegistry.RESERVOIR_BUFFS);
        List<DataAttachmentRegistry.ReservoirBuffs.ModifierData> modifiersToRemove = data.getModifiers(targetEffect);
        if (!modifiersToRemove.isEmpty()) {
            for (DataAttachmentRegistry.ReservoirBuffs.ModifierData modData : modifiersToRemove) {
                AttributeInstance attr = player.getAttribute(modData.attribute());
                if (attr != null) {
                    attr.removeModifier(modData.id());
                }
            }
            player.setData(DataAttachmentRegistry.RESERVOIR_BUFFS, data.remove(targetEffect));
        }
    }

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) return;
        cleanUpReservoirBuffs(event.getEntity(), event.getEffectInstance().getEffect());
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() == null) return;
        cleanUpReservoirBuffs(event.getEntity(), event.getEffectInstance().getEffect());
    }

    private static void removeGraceBonus(Player player, ResourceLocation MODIFIER_ID) {
        GraceArmorMod.ATTRIBUTE_SET.forEach((attr, value) -> {
            AttributeInstance instance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr));
            if (instance != null)
                if (instance.getModifier(MODIFIER_ID) != null)
                    instance.removeModifier(MODIFIER_ID);
        });
    }

    public static void updateCCModifier(LivingEntity livingEntity, double bonus) {
        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_bonus_cc_modifier");
        AttributeInstance criticalChance = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_CHANCE.get()));
        if (criticalChance == null) return;

        if (criticalChance.getModifier(MODIFIER_ID) != null)
            criticalChance.removeModifier(MODIFIER_ID);
        if (bonus != 0)
            criticalChance.addPermanentModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    public static void updateSCModifier(LivingEntity livingEntity, double bonus) {
        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_bonus_sc_modifier");
        AttributeInstance statusChance = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.STATUS_CHANCE.get()));
        if (statusChance == null) return;

        if (statusChance.getModifier(MODIFIER_ID) != null)
            statusChance.removeModifier(MODIFIER_ID);
        if (bonus != 0)
            statusChance.addPermanentModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    public static void updateScopeModifier(LivingEntity livingEntity, boolean mode) {
        int scopeLevel = -1;
        int scopeGalLevel = -1;
        if (livingEntity.hasEffect(MobEffectRegistry.SCOPE))
            scopeLevel = livingEntity.getEffect(MobEffectRegistry.SCOPE).getAmplifier();
        if (livingEntity.hasEffect(MobEffectRegistry.SCOPE_GALVANIZED))
            scopeGalLevel = livingEntity.getEffect(MobEffectRegistry.SCOPE_GALVANIZED).getAmplifier();
        if (scopeLevel == -1 && scopeGalLevel == -1) return;

        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_modifier");
        AttributeInstance criticalChance = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_CHANCE.get()));
        if (criticalChance == null) return;

        AttributeModifier mod = new AttributeModifier(
                MODIFIER_ID,
                -(scopeLevel + 1) * ServerConfigs.MOD_RARE_SCOPE.get() / 200 - (scopeGalLevel + 1) * ServerConfigs.MOD_LEGENDARY_GALVANIZED_SCOPE_KILLBONUS.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (mode) criticalChance.addPermanentModifier(mod);
        else criticalChance.removeModifier(mod);
    }

    public static void MultishotGalvanizedToolModBonus(Player player) {
        if (player.hasEffect(MobEffectRegistry.MULTISHOT_GALVANIZED)) {
            int amplifier = player.getEffect(MobEffectRegistry.MULTISHOT_GALVANIZED).getAmplifier();
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.MULTISHOT_GALVANIZED,
                    ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION.get() * 20,
                    Math.min(amplifier + 1,
                            Math.min(255, ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_STACK.get() - 1)),
                    false,
                    true,
                    true
            ));
        } else {
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.MULTISHOT_GALVANIZED,
                    ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION.get() * 20,
                    0,
                    false,
                    true,
                    true
            ));
        }
        player.getPersistentData().putInt("gal_multishot_left", ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION.get() * 20);
    }

    public static void ScopeGalvanizedToolModBonus(Player player) {
        if (player.hasEffect(MobEffectRegistry.SCOPE_GALVANIZED)) {
            int amplifier = player.getEffect(MobEffectRegistry.SCOPE_GALVANIZED).getAmplifier();
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.SCOPE_GALVANIZED,
                    ServerConfigs.MOD_RARE_SCOPE_DURATION.get() * 20,
                    Math.min(amplifier + 1,
                            Math.min(255, ServerConfigs.MOD_LEGENDARY_GALVANIZED_SCOPE_STACK.get() - 1)),
                    false,
                    true,
                    true
            ));
        } else {
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.SCOPE_GALVANIZED,
                    ServerConfigs.MOD_RARE_SCOPE_DURATION.get() * 20,
                    0,
                    false,
                    true,
                    true
            ));
        }
        player.getPersistentData().putInt("gal_scope_left", ServerConfigs.MOD_RARE_SCOPE_DURATION.get() * 20);
    }

    public static void FuryKillModBonus(Player player, int modLevel) {
        if (player.hasEffect(MobEffectRegistry.FURY_KILL)) {
            int amplifier = player.getEffect(MobEffectRegistry.FURY_KILL).getAmplifier();
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.FURY_KILL,
                    ServerConfigs.MOD_RARE_BERSERKER_FURY_DURATION.get() * 20,
                    Math.min(amplifier + modLevel,
                            Math.min(255, ServerConfigs.MOD_RARE_BERSERKER_FURY_STACK.get() * 5 - 1)),
                    false,
                    true,
                    true
            ));
        } else {
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.FURY_KILL,
                    ServerConfigs.MOD_RARE_BERSERKER_FURY_DURATION.get() * 20,
                    modLevel - 1,
                    false,
                    true,
                    true
            ));
        }
        player.getPersistentData().putInt("fury_kill_left", ServerConfigs.MOD_RARE_SCOPE_DURATION.get() * 20);
    }

    public static void ScopeToolModBonus(Player player, int modLevel, int factor) {
        player.addEffect(new MobEffectInstance(
                MobEffectRegistry.SCOPE,
                ServerConfigs.MOD_RARE_SCOPE_DURATION.get() * 20,
                modLevel * factor - 1,
                false,
                true,
                true
        ));
    }
}
