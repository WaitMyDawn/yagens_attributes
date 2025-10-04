package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record SyncPreShootCountPacket(DataAttachmentRegistry.PreShoot preShoot) implements CustomPacketPayload {

    public static final Type<SyncPreShootCountPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "sync_pre_shoot_count"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPreShootCountPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.preShoot.count());
                        buf.writeInt(pkt.preShoot.painLevel());
                        buf.writeInt(pkt.preShoot.morePainLevel());
                    },
                    buf -> new SyncPreShootCountPacket(
                            new DataAttachmentRegistry.PreShoot(buf.readInt(), buf.readInt(), buf.readInt()))
            );

    public static void handle(SyncPreShootCountPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                ctx.player().setData(DataAttachmentRegistry.PRE_SHOOT_COUNT.get(), packet.preShoot)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}