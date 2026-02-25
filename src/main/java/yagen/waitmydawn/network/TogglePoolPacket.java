package yagen.waitmydawn.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;

public record TogglePoolPacket() implements CustomPacketPayload {
    public static final Type<TogglePoolPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "toggle_pool"));
    public static final StreamCodec<ByteBuf, TogglePoolPacket> STREAM_CODEC = StreamCodec.unit(new TogglePoolPacket());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TogglePoolPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ModOperationMenu menu) {
                menu.toggleModPoolMode();
            }
        });
    }
}