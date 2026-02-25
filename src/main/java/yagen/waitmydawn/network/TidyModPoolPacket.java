package yagen.waitmydawn.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public record TidyModPoolPacket() implements CustomPacketPayload {
    public static final Type<TidyModPoolPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "tidy_mod_pool"));
    public static final StreamCodec<ByteBuf, TidyModPoolPacket> STREAM_CODEC = StreamCodec.unit(new TidyModPoolPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TidyModPoolPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ModOperationMenu menu) {
                context.player().getData(DataAttachmentRegistry.MOD_POOL).tidy();
                menu.refreshProxyTarget();
                menu.broadcastChanges();
            }
        });
    }
}