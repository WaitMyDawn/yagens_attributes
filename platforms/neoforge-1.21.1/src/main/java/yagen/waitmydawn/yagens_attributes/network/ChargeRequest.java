package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.util.UUID;

public record ChargeRequest(int sequence, ResourceLocation item, UUID weapon, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<ChargeRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "charge_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargeRequest> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.sequence).writeResourceLocation(value.item); CombatCodecs.uuid(buffer, value.weapon); buffer.writeEnum(value.hand);
    }, buffer -> new ChargeRequest(buffer.readVarInt(), buffer.readResourceLocation(), CombatCodecs.uuid(buffer), buffer.readEnum(InteractionHand.class)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
