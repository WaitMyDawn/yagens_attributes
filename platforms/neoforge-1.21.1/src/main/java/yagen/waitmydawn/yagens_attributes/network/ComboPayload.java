package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.Combo;
import java.util.UUID;

public record ComboPayload(UUID weapon, int count, int remaining, int duration, int hitsPerTier, int maxTier) implements CustomPacketPayload {
    public ComboPayload {
        var rules = new Combo.Rules(hitsPerTier, maxTier);
        if (count < 0 || count > rules.maximumCount() || duration < 0 || duration > 144000 || remaining < 0 || remaining > duration
                || count > 0 && (weapon == null || remaining == 0) || count == 0 && remaining != 0)
            throw new IllegalArgumentException("Invalid combo snapshot");
    }
    public static final Type<ComboPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ComboPayload> CODEC = StreamCodec.of((buffer, value) -> {
        CombatCodecs.uuid(buffer, value.weapon); buffer.writeVarInt(value.count).writeVarInt(value.remaining).writeVarInt(value.duration).writeVarInt(value.hitsPerTier).writeVarInt(value.maxTier);
    }, buffer -> new ComboPayload(CombatCodecs.uuid(buffer), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
