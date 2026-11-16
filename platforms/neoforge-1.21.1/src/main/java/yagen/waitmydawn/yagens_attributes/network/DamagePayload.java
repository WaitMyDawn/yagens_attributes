package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.util.UUID;
import java.util.function.Consumer;

public record DamagePayload(int targetId, Vec3 position, UUID attacker, float amount, int tier, int procMask) implements CustomPacketPayload {
    public static final Type<DamagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "damage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DamagePayload> CODEC = StreamCodec.of(
        (buffer, value) -> {
            buffer.writeVarInt(value.targetId);
            buffer.writeDouble(value.position.x).writeDouble(value.position.y).writeDouble(value.position.z);
            buffer.writeUUID(value.attacker); buffer.writeFloat(value.amount); buffer.writeVarInt(value.tier); buffer.writeVarInt(value.procMask);
        }, buffer -> new DamagePayload(buffer.readVarInt(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readUUID(), buffer.readFloat(), buffer.readVarInt(), buffer.readVarInt()));

    // Client setup installs the sink without linking client-only classes on a dedicated server.
    private static Consumer<DamagePayload> clientSink = payload -> {};
    public static void clientSink(Consumer<DamagePayload> sink) { clientSink = sink; }
    public static void receive(DamagePayload payload) {
        if (Float.isFinite(payload.amount) && payload.amount > 0 && payload.tier >= 0 && payload.tier <= 100
                && Double.isFinite(payload.position.x) && Double.isFinite(payload.position.y) && Double.isFinite(payload.position.z)) clientSink.accept(payload);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
