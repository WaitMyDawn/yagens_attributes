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

public record HeatPacket(Vec3 pos,double radius,double height) implements CustomPacketPayload {

    public static final Type<HeatPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "heat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.pos.x).writeDouble(pkt.pos.y).writeDouble(pkt.pos.z).writeDouble(pkt.radius).writeDouble(pkt.height);
                    },
                    buf -> new HeatPacket(
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readDouble(),buf.readDouble()));

    public static void handle(HeatPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.HEAT.get(),
                        packet.pos.x, packet.pos.y, packet.pos.z,
                        packet.radius, packet.height, 0)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}