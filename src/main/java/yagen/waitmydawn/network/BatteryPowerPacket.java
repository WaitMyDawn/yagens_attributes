package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record BatteryPowerPacket(double value) implements CustomPacketPayload {
    public static final Type<BatteryPowerPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "battery_power"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BatteryPowerPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeDouble(pkt.value);
            },
            buf -> new BatteryPowerPacket(buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BatteryPowerPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            DataAttachmentRegistry.setBatteryPower(ctx.player(), pkt.value);
        });
    }
}