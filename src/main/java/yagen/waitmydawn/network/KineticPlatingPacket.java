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

import static yagen.waitmydawn.api.events.AttackEventHandler.forceEffect;

public record KineticPlatingPacket(boolean state) implements CustomPacketPayload {
    public static final Type<KineticPlatingPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "kinetic_plating"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KineticPlatingPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBoolean(pkt.state());
                    },
                    buf -> new KineticPlatingPacket(buf.readBoolean())
            );

    public static void handle(KineticPlatingPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            int duration = (int) (20 * ServerConfigs.MOD_WARFRAME_KINETIC_PLATING_DURATION.get() * player.getAttributeValue(YAttributes.ABILITY_DURATION));



            player.addEffect(new MobEffectInstance(MobEffectRegistry.KINETIC_PLATING, duration));
        });
    }

    @Override
    public Type<KineticPlatingPacket> type() {
        return TYPE;
    }
}