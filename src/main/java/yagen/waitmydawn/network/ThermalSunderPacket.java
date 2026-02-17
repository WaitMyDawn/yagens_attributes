package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.events.AttackEventHandler;
import yagen.waitmydawn.api.util.DamageCompat;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.effect.ColdStatusEffect;
import yagen.waitmydawn.effect.HeatStatusEffect;
import yagen.waitmydawn.effect.ThermalSunderStatusEffect;
import yagen.waitmydawn.registries.DamageTypeRegistry;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.List;
import java.util.UUID;

import static yagen.waitmydawn.api.events.AttackEventHandler.forceEffect;

public record ThermalSunderPacket(boolean state) implements CustomPacketPayload {
    public static final Type<ThermalSunderPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThermalSunderPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBoolean(pkt.state());
                    },
                    buf -> new ThermalSunderPacket(buf.readBoolean())
            );

    public static void handle(ThermalSunderPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            double baseDamage = ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER.get() * player.getAttributeValue(YAttributes.ABILITY_STRENGTH);
            DamageType damageType;
            int amp;
            if (pkt.state()) {
                damageType = DamageType.HEAT;
                baseDamage *= 2;
            } else damageType = DamageType.COLD;
            double batteryPower = DataAttachmentRegistry.getBatteryPower(player);
            double radius = ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER_RANGE.get() * player.getAttributeValue(YAttributes.ABILITY_RANGE);
            int duration = (int) (ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER_DURATION.get() * 20 * player.getAttributeValue(YAttributes.ABILITY_DURATION));
            boolean isInfinite = batteryPower == 100 && player.hasEffect(MobEffectRegistry.REDLINE);
            Vec3 center = player.position();
            baseDamage = baseDamage * (1 + batteryPower * 0.04);

            AABB box = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            List<LivingEntity> targets = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    e -> e.isAlive() && e != player
            );

            for (LivingEntity livingEntity : targets) {
                double distance = center.distanceTo(livingEntity.position());
                double multiplier = multiplier(radius, distance);
                double multiplierDamage = baseDamage * multiplier;

                if (pkt.state()) { // apply heat
                    if (!livingEntity.hasEffect(MobEffectRegistry.COLD_STATUS)) { // no cold
                        if (livingEntity.hasEffect(MobEffectRegistry.HEAT_STATUS))
                            amp = HeatStatusEffect.heatSize(livingEntity);
                        else amp = 0;
                        double ampDamage = multiplierDamage * Math.pow(2, amp);
                        livingEntity.hurt(DamageCompat.getDamageSource(
                                DamageTypeRegistry.HEAT_STATUS_DAMAGE_TYPE, player), (float) ampDamage);
                        AttackEventHandler.statusEffect(damageType, player, livingEntity, (float) ampDamage);
                    } else { // cold + heat
                        amp = livingEntity.getEffect(MobEffectRegistry.COLD_STATUS).getAmplifier();
                        double ampDamage = multiplierDamage * Math.pow(2, amp + 1);
                        if (amp > 0) {
                            forceEffect(livingEntity,
                                    new MobEffectInstance(MobEffectRegistry.THERMAL_SUNDER, duration, 0));
                            if (batteryPower > 80.0)
                                ThermalSunderStatusEffect.updateModifiers(livingEntity, Math.max(-1, -(batteryPower - 80.0) * 0.05));
                        }
                        livingEntity.hurt(DamageCompat.getDamageSource(
                                DamageTypeRegistry.BLAST_STATUS_DAMAGE_TYPE, player), (float) ampDamage);
                        livingEntity.knockback(multiplier, center.x - livingEntity.getX(), center.z - livingEntity.getZ());
                        HeatStatusEffect.removeHeat(livingEntity);
                        ColdStatusEffect.removeCold(livingEntity);
                    }
                } else { // apply cold
                    if (!livingEntity.hasEffect(MobEffectRegistry.HEAT_STATUS)) { // no heat
                        if (livingEntity.hasEffect(MobEffectRegistry.COLD_STATUS))
                            amp = livingEntity.getEffect(MobEffectRegistry.COLD_STATUS).getAmplifier();
                        else amp = 0;
                        double ampDamage = multiplierDamage * Math.pow(2, amp);
                        livingEntity.hurt(DamageCompat.getDamageSource(
                                DamageTypeRegistry.COLD_STATUS_DAMAGE_TYPE, player), (float) ampDamage);
                        AttackEventHandler.statusEffect(damageType, player, livingEntity, (float) ampDamage);
                        livingEntity.knockback(multiplier / 2, livingEntity.getX() - center.x, livingEntity.getZ() - center.z);
                    } else { // heat + cold
                        amp = HeatStatusEffect.heatSize(livingEntity);
                        double ampDamage = multiplierDamage * Math.pow(2, amp + 2);
                        livingEntity.hurt(DamageCompat.getDamageSource(
                                DamageTypeRegistry.BLAST_STATUS_DAMAGE_TYPE, player), (float) ampDamage);
                        livingEntity.knockback(multiplier, livingEntity.getX() - center.x, livingEntity.getZ() - center.z);
                        ColdStatusEffect.removeCold(livingEntity);
                        HeatStatusEffect.removeHeat(livingEntity);
                    }
                }
            }
            if (!isInfinite) {
                if (pkt.state()) {
                    DataAttachmentRegistry.setBatteryPower(player, batteryPower - 10);
                    PacketDistributor.sendToPlayer(player, new BatteryPowerPacket(DataAttachmentRegistry.getBatteryPower(player)));
                } else {
                    DataAttachmentRegistry.setBatteryPower(player, batteryPower + 10);
                    if (batteryPower + 10 >= 100)
                        player.addEffect(new MobEffectInstance(MobEffectRegistry.REDLINE, duration, 0));
                    PacketDistributor.sendToPlayer(player, new BatteryPowerPacket(DataAttachmentRegistry.getBatteryPower(player)));
                }
            }
            PacketDistributor.sendToPlayer(player, new ThermalSunderClientPacket(pkt.state()));
        });
    }

    private static double multiplier(double range, double distance) {
        return Math.max(0, 1 - 0.75 * distance / range);
    }

    @Override
    public Type<ThermalSunderPacket> type() {
        return TYPE;
    }
}