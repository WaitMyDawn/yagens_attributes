package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;

public record ShieldPayload(int gateTicks) implements CustomPacketPayload {
    public ShieldPayload { if (gateTicks < 0 || gateTicks > 10) throw new IllegalArgumentException("Invalid shield gate"); }
    public static final Type<ShieldPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "shield"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldPayload> CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeVarInt(value.gateTicks), buffer -> new ShieldPayload(buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
