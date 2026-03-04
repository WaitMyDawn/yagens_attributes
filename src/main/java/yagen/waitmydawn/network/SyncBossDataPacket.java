package yagen.waitmydawn.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.capabilities.PlayerBossData;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record SyncBossDataPacket(PlayerBossData data) implements CustomPacketPayload {

    public static final Type<SyncBossDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "sync_boss_data"));

    public static final StreamCodec<ByteBuf, SyncBossDataPacket> STREAM_CODEC = StreamCodec.composite(
            PlayerBossData.STREAM_CODEC,
            SyncBossDataPacket::data,
            SyncBossDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBossDataPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            PlayerBossData clientData = player.getData(DataAttachmentRegistry.BOSSES_LIST);
            clientData.setBosses(payload.data().getList());
        });
    }
}