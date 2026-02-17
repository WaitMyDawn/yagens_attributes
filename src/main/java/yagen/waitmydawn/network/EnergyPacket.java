package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record EnergyPacket(double value) implements CustomPacketPayload {
    public static final Type<EnergyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "energy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeDouble(pkt.value),
            buf -> new EnergyPacket(buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EnergyPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.flow() == PacketFlow.CLIENTBOUND)
                DataAttachmentRegistry.setEnergy(ctx.player(), pkt.value);
            else if (ctx.flow() == PacketFlow.SERVERBOUND)
                if (pkt.value < 0) { // reduce
                    DataAttachmentRegistry.reduceEnergy(ctx.player(),
                            pkt.value * (2 - ctx.player().getAttributeValue(YAttributes.ABILITY_EFFICIENCY))
                    );
                    PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(),
                            new EnergyPacket(DataAttachmentRegistry.getEnergy(ctx.player())));
                } else if (pkt.value == 0) // sync
                    PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(),
                            new EnergyPacket(DataAttachmentRegistry.getEnergy(ctx.player())));
        });
    }
}