package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeaponProfile;
import java.util.*;

public record ProfilePayload(boolean first, boolean last, Map<ResourceLocation, WeaponProfile> profiles) implements CustomPacketPayload {
    public static final int CHUNK_SIZE = 64;
    public static final Type<ProfilePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "profiles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfilePayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeBoolean(value.first).writeBoolean(value.last).writeVarInt(value.profiles.size());
        value.profiles.forEach((id, profile) -> { buffer.writeResourceLocation(id); CombatCodecs.profile(buffer, profile); });
    }, buffer -> {
        boolean first = buffer.readBoolean(), last = buffer.readBoolean();
        int count = CombatCodecs.count(buffer, CHUNK_SIZE);
        Map<ResourceLocation, WeaponProfile> result = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            if (result.put(id, CombatCodecs.profile(buffer)) != null) throw new IllegalArgumentException("Duplicate profile ID");
        }
        return new ProfilePayload(first, last, Map.copyOf(result));
    });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
