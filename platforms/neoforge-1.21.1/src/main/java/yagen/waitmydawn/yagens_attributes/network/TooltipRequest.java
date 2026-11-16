package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.util.UUID;

public record TooltipRequest(int menuId, int slot, ResourceLocation item, UUID weapon) implements CustomPacketPayload {
    public static final Type<TooltipRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "inspect_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TooltipRequest> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.menuId).writeVarInt(value.slot).writeResourceLocation(value.item); CombatCodecs.uuid(buffer, value.weapon);
    }, buffer -> new TooltipRequest(buffer.readVarInt(), buffer.readVarInt(), buffer.readResourceLocation(), CombatCodecs.uuid(buffer)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
