package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.RangedStats;
import java.util.UUID;

public record ChargePayload(ChargeRequest request, double rate) implements CustomPacketPayload {
    public ChargePayload {
        if (!Double.isFinite(rate) || rate < 0 || rate > RangedStats.LIMIT) throw new IllegalArgumentException("Invalid charge rate");
    }
    public static final Type<ChargePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "charge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargePayload> CODEC = StreamCodec.of((buffer, value) -> {
        ChargeRequest.CODEC.encode(buffer, value.request); buffer.writeDouble(value.rate);
    }, buffer -> new ChargePayload(ChargeRequest.CODEC.decode(buffer), buffer.readDouble()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
