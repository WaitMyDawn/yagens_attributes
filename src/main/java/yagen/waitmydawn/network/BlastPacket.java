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

public record BlastPacket(Vec3 pos, double level) implements CustomPacketPayload {

    public static final Type<BlastPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "blast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlastPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.pos.x).writeDouble(pkt.pos.y).writeDouble(pkt.pos.z).writeDouble(pkt.level);
                    },
                    buf -> new BlastPacket(
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readDouble()));

    public static void handle(BlastPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.BLAST.get(),
                        packet.pos.x, packet.pos.y, packet.pos.z,
                        packet.level, 0, 0)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}