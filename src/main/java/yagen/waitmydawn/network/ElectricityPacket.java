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

public record ElectricityPacket(Vec3 from, Vec3 to) implements CustomPacketPayload {

    public static final Type<ElectricityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "electricity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ElectricityPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.from.x).writeDouble(pkt.from.y).writeDouble(pkt.from.z);
                        buf.writeDouble(pkt.to.x).writeDouble(pkt.to.y).writeDouble(pkt.to.z);
                    },
                    buf -> new ElectricityPacket(
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()))
            );

    public static void handle(ElectricityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.ELECTRICITY.get(),
                        packet.from.x, packet.from.y, packet.from.z,
                        packet.to.x,   packet.to.y,   packet.to.z)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}