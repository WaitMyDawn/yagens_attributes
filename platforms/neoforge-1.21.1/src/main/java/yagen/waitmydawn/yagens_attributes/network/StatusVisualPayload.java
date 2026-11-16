package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.DamageType;

public record StatusVisualPayload(Vec3 position, DamageType damageType, double radius, boolean burst) implements CustomPacketPayload {
    public StatusVisualPayload {
        if (!Double.isFinite(position.x + position.y + position.z) || !Double.isFinite(radius) || radius < 0 || radius > 8) throw new IllegalArgumentException("Invalid status visual");
    }
    public static final Type<StatusVisualPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "status_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatusVisualPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeDouble(value.position.x); buffer.writeDouble(value.position.y); buffer.writeDouble(value.position.z);
        buffer.writeEnum(value.damageType); buffer.writeDouble(value.radius); buffer.writeBoolean(value.burst);
    }, buffer -> new StatusVisualPayload(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readEnum(DamageType.class), buffer.readDouble(), buffer.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
