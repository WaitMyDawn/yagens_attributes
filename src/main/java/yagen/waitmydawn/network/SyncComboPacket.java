package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record SyncComboPacket(DataAttachmentRegistry.Combo combo) implements CustomPacketPayload {

    public static final Type<SyncComboPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "sync_combo"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncComboPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.combo.count());
                        buf.writeInt(pkt.combo.leftDuration());
                    },
                    buf -> new SyncComboPacket(
                            new DataAttachmentRegistry.Combo(buf.readInt(), buf.readInt()))
            );

    public static void handle(SyncComboPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                ctx.player().setData(DataAttachmentRegistry.COMBO.get(), packet.combo)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}