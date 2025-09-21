package yagen.waitmydawn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.ParticleRegistry;

public record DamageNumberPacket(Vec3 pos, double amount, double dColor, double dLevel) implements CustomPacketPayload {

    public static final Type<DamageNumberPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "damage_number"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageNumberPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.pos.x).writeDouble(pkt.pos.y).writeDouble(pkt.pos.z);
                        buf.writeDouble(pkt.amount);
                        buf.writeDouble(pkt.dColor);
                        buf.writeDouble(pkt.dLevel);
                    },
                    buf -> new DamageNumberPacket(
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readDouble(), buf.readDouble(), buf.readDouble())
            );

    /* 客户端收到后立刻生成粒子 */
    public static void handle(DamageNumberPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().particleEngine.createParticle(
                        (SimpleParticleType) ParticleRegistry.DAMAGE_NUMBER.get(),
                        packet.pos.x, packet.pos.y, packet.pos.z,
                        packet.amount, packet.dColor, packet.dLevel)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}