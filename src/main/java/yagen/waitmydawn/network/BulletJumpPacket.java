package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;

import java.util.UUID;

public record BulletJumpPacket(UUID playerId, boolean state) implements CustomPacketPayload {
    public static final Type<BulletJumpPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "bullet_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BulletJumpPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.playerId());
                        buf.writeBoolean(pkt.state());
                    },
                    buf -> new BulletJumpPacket(buf.readUUID(),buf.readBoolean())
            );

    public static void handle(BulletJumpPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!player.getUUID().equals(pkt.playerId())) return;

            player.getPersistentData().putBoolean("BulletJump", pkt.state());
        });
    }

    @Override
    public Type<BulletJumpPacket> type() {
        return TYPE;
    }
}