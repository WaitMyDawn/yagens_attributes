package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.network.SyncComboPacket;
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
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        double comboBonusCC = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_CC.get())).getValue();
        double comboBonusSC = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_BONUS_SC.get())).getValue();

        DataAttachmentRegistry.Combo old = player.getData(DataAttachmentRegistry.COMBO.get());
        if (old.leftDuration() > 0) {
            int comboCount = old.count();
            int comboLevel = DataAttachmentRegistry.getComboLevel(comboCount);
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
            DataAttachmentRegistry.Combo updated = old.withCount(0);
            if (updated != old) {
                player.setData(DataAttachmentRegistry.COMBO.get(), updated);
                if (!player.level().isClientSide)
                    PacketDistributor.sendToPlayer((ServerPlayer) player,
                            new SyncComboPacket(updated));
            }
        }

    }

    private static void updateCCModifier(Player player, double bonus) {
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

    private static void updateSCModifier(Player player, double bonus) {
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
