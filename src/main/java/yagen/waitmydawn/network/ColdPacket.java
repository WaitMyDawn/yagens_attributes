package yagen.waitmydawn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.ParticleRegistry;

public record ColdPacket(Vec3 pos, double width,double height) implements CustomPacketPayload {

    public static final Type<ColdPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "cold"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColdPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.pos.x).writeDouble(pkt.pos.y).writeDouble(pkt.pos.z).writeDouble(pkt.width).writeDouble(pkt.height);
                    },
                    buf -> new ColdPacket(
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readDouble(),buf.readDouble()));

    public static void handle(ColdPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.COLD.get(),
                        packet.pos.x, packet.pos.y, packet.pos.z,
                        packet.width, packet.height, 0)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}