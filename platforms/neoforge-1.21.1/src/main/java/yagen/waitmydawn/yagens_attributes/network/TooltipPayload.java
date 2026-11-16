package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.util.UUID;

public record TooltipPayload(ResourceLocation item, UUID weapon, WeaponInspectionData data) implements CustomPacketPayload {
    public static final Type<TooltipPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "inspect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TooltipPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeResourceLocation(value.item); CombatCodecs.uuid(buffer, value.weapon); WeaponInspectionData.write(buffer, value.data);
    }, buffer -> new TooltipPayload(buffer.readResourceLocation(), CombatCodecs.uuid(buffer), WeaponInspectionData.read(buffer)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
