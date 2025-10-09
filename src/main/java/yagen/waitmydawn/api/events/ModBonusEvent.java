package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.network.SyncComboPacket;
import yagen.waitmydawn.network.SyncPreShootCountPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModBonusEvent {
    @SubscribeEvent
    public static void killBonusEvent(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof Player player)) return;
        ItemStack itemStack = player.getMainHandItem();
        if (!IModContainer.isModContainer(itemStack)) return;
        var container = IModContainer.get(itemStack);
        for (ModSlot slot : container.getActiveMods()) {
            if (slot.getMod().getModName().equals("multishot_galvanized_tool_mod"))
                MultishotGalvanizedToolModBonus(player);
        }
    }

    @SubscribeEvent
    public static void comboBonusEvent(PlayerTickEvent.Post event) {
        Player attacker = event.getEntity();

        if (attacker.level().isClientSide) return;
        double comboBonusCC = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_CC.get())).getValue();
        double comboBonusSC = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_SC.get())).getValue();

        DataAttachmentRegistry.Combo old = attacker.getData(DataAttachmentRegistry.COMBO.get());
        if (old.leftDuration() > 0) {
            int comboLevel = old.getComboLevel();
            updateCCModifier(attacker, comboBonusCC * comboLevel);
            updateSCModifier(attacker, comboBonusSC * comboLevel);
            DataAttachmentRegistry.Combo updated = old.decrement();
            if (updated != old) {
                attacker.setData(DataAttachmentRegistry.COMBO.get(), updated);
                if (!attacker.level().isClientSide)
                    PacketDistributor.sendToPlayer((ServerPlayer) attacker,
                            new SyncComboPacket(updated));
            }
        } else {
            updateCCModifier(attacker, 0);
            updateSCModifier(attacker, 0);
            DataAttachmentRegistry.Combo updated = old.withCount(0);
            if (updated != old) {
                attacker.setData(DataAttachmentRegistry.COMBO.get(), updated);
//                if (!attacker.level().isClientSide)
                PacketDistributor.sendToPlayer((ServerPlayer) attacker,
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

    public static void updateCCModifier(LivingEntity player, double bonus) {
        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_bonus_cc_modifier");
        AttributeInstance criticalChance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_CHANCE.get()));
        if (criticalChance == null) return;

        AttributeModifier mod = new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (bonus == 0) {
            criticalChance.removeModifier(mod);
        } else if (criticalChance.getModifier(MODIFIER_ID) != null) {
            criticalChance.removeModifier(mod);
            criticalChance.addPermanentModifier(mod);
        } else {
            criticalChance.addPermanentModifier(mod);
        }
    }

    public static void updateSCModifier(LivingEntity player, double bonus) {
        ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_bonus_sc_modifier");
        AttributeInstance statusChance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.STATUS_CHANCE.get()));
        if (statusChance == null) return;

        AttributeModifier mod = new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (bonus == 0) {
            statusChance.removeModifier(mod);
        } else if (statusChance.getModifier(MODIFIER_ID) != null) {
            statusChance.removeModifier(mod);
            statusChance.addPermanentModifier(mod);
        } else {
            statusChance.addPermanentModifier(mod);
        }
    }

    public static void MultishotGalvanizedToolModBonus(Player player) {
        if (player.hasEffect(MobEffectRegistry.MULTISHOT_GALVANIZED)) {
            int amplifier = player.getEffect(MobEffectRegistry.MULTISHOT_GALVANIZED).getAmplifier();
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.MULTISHOT_GALVANIZED,
                    400,
                    Math.min(amplifier + 1, 4),
                    false,
                    true,
                    true
            ));
        } else {
            player.addEffect(new MobEffectInstance(
                    MobEffectRegistry.MULTISHOT_GALVANIZED,
                    400,
                    0,
                    false,
                    true,
                    true
            ));
        }
        player.getPersistentData().putInt("YAGalvanizedLeft", 400);
    }
}
