package yagen.waitmydawn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.ClientConfigs;

public record ThermalSunderClientPacket(boolean state) implements CustomPacketPayload {
    public static final Type<ThermalSunderClientPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThermalSunderClientPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBoolean(pkt.state());
                    },
                    buf -> new ThermalSunderClientPacket(buf.readBoolean())
            );

    public static void handle(ThermalSunderClientPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            if (ClientConfigs.IF_THERMAL_SUNDER_SOUND.get()) {
                if (pkt.state())
                    player.playSound(SoundEvents.FIRECHARGE_USE,
                            ClientConfigs.THERMAL_SUNDER_VOLUME.get().floatValue(),
                            ClientConfigs.THERMAL_SUNDER_PITCH.get().floatValue());
                else
                    player.playSound(SoundEvents.SNOW_GOLEM_SHOOT,
                            ClientConfigs.THERMAL_SUNDER_VOLUME.get().floatValue(),
                            ClientConfigs.THERMAL_SUNDER_PITCH.get().floatValue());
            }

        });
    }

    @Override
    public Type<ThermalSunderClientPacket> type() {
        return TYPE;
    }
}