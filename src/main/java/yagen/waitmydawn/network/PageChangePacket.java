package yagen.waitmydawn.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;

public record PageChangePacket(int page) implements CustomPacketPayload {
    public static final Type<PageChangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "page_change"));
    public static final StreamCodec<ByteBuf, PageChangePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PageChangePacket::page, PageChangePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PageChangePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ModOperationMenu menu) {
                menu.changePage(payload.page);
            }
        });
    }
}