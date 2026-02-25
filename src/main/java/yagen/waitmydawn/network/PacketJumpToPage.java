package yagen.waitmydawn.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;

public record PacketJumpToPage(int pageIndex) implements CustomPacketPayload {
    public static final Type<PacketJumpToPage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "jump_to_page"));
    public static final StreamCodec<ByteBuf, PacketJumpToPage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketJumpToPage::pageIndex,
            PacketJumpToPage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketJumpToPage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ModOperationMenu menu) {
                menu.jumpToPage(payload.pageIndex);
            }
        });
    }
}
