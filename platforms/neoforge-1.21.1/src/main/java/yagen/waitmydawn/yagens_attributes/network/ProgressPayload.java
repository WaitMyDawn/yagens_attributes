package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeaponProgress;
import java.util.UUID;

public record ProgressPayload(UUID weapon, WeaponProgress progress) implements CustomPacketPayload {
    public static final Type<ProgressPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeUUID(value.weapon); CombatCodecs.progress(buffer, value.progress);
    }, buffer -> new ProgressPayload(buffer.readUUID(), CombatCodecs.progress(buffer)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
