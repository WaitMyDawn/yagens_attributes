package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.ShieldRules;
import yagen.waitmydawn.yagens_attributes.data.ShieldState;
import yagen.waitmydawn.yagens_attributes.network.ShieldPayload;
import yagen.waitmydawn.yagens_attributes.registries.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ShieldService {
    private ShieldService() {}
    public static double natural(LivingEntity entity) {
        return entity.getAttribute(ShieldAttributes.MAX_SHIELD) == null ? 0 : entity.getAttributeValue(ShieldAttributes.MAX_SHIELD);
    }
    public static ShieldState state(LivingEntity entity) { return entity.getData(RangedAttachments.SHIELD); }
    public static boolean enabled(LivingEntity entity) { return natural(entity) > 0; }
    public static double capacity(LivingEntity entity) { return ShieldRules.capacity(natural(entity)); }
    public static void add(LivingEntity entity, double amount) {
        entity.setAbsorptionAmount((float) Math.min(capacity(entity), entity.getAbsorptionAmount() + amount));
    }
    public static boolean protectedNow(LivingEntity entity) {
        return !entity.level().isClientSide && entity.hasData(RangedAttachments.SHIELD) && state(entity).gate > 0;
    }
    public static void damaged(LivingEntity entity) {
        if (!enabled(entity)) return;
        state(entity).delay = ServerConfigs.SHIELD_RECHARGE_DELAY.get();
        state(entity).carry = 0;
    }
    public static void broken(LivingEntity entity, double before) {
        var value = state(entity);
        if (!value.consume(before)) return;
        if (entity instanceof ServerPlayer player) sync(player);
    }
    public static void sync(ServerPlayer player) {
        var value = state(player);
        PacketDistributor.sendToPlayer(player, new ShieldPayload(value.gate));
    }
    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide || !entity.isAlive()) return;
        if (!enabled(entity) && !entity.hasData(RangedAttachments.SHIELD)) return;
        var value = state(entity);
        if (value.gate > 0) value.gate--;
        if (value.delay > 0) value.delay--;
        double maximum = natural(entity);
        if (maximum <= 0) {
            value.armed = false; value.before = 0; value.carry = 0;
            if (entity.getAbsorptionAmount() > entity.getMaxAbsorption()) entity.setAbsorptionAmount(entity.getMaxAbsorption());
            return;
        }
        float old = entity.getAbsorptionAmount();
        if (old > capacity(entity)) entity.setAbsorptionAmount((float) capacity(entity));
        if (value.delay == 0 && value.gate == 0 && old < maximum && !StatusEffects.preventsRecharge(entity)) {
            double rate = entity.getAttributeValue(ShieldAttributes.SHIELD_REGEN);
            var recharge = ShieldRules.recharge(old, maximum, rate, value.carry);
            entity.setAbsorptionAmount(recharge.shield()); value.carry = recharge.carry();
            value.naturalGain(old, entity.getAbsorptionAmount());
        } else value.carry = 0;
        value.observe(entity.getAbsorptionAmount());
        if (entity instanceof ServerPlayer player && entity.tickCount % 20 == 0) sync(player);
    }
}
