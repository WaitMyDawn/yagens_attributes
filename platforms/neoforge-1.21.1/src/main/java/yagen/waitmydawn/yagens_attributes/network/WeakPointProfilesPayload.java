package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record WeakPointProfilesPayload(boolean first, boolean last, Map<ResourceLocation, List<WeakPointRegion>> profiles)
        implements CustomPacketPayload {
    public static final int CHUNK_SIZE = 32;
    public static final Type<WeakPointProfilesPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "weak_point_profiles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeakPointProfilesPayload> CODEC =
            StreamCodec.of(WeakPointProfilesPayload::encode, WeakPointProfilesPayload::decode);

    @Override public Type<WeakPointProfilesPayload> type() { return TYPE; }

    private static void encode(RegistryFriendlyByteBuf buffer, WeakPointProfilesPayload payload) {
        buffer.writeBoolean(payload.first()).writeBoolean(payload.last()).writeVarInt(payload.profiles().size());
        payload.profiles().forEach((id, regions) -> {
            buffer.writeResourceLocation(id).writeVarInt(regions.size());
            for (var region : regions) {
                buffer.writeUtf(region.id(), 64).writeEnum(region.anchor()).writeEnum(region.rotation());
                writePoint(buffer, region.min());
                writePoint(buffer, region.max());
                buffer.writeEnum(region.state()).writeEnum(region.part());
            }
        });
    }

    private static WeakPointProfilesPayload decode(RegistryFriendlyByteBuf buffer) {
        boolean first = buffer.readBoolean(), last = buffer.readBoolean();
        int count = CombatCodecs.count(buffer, CHUNK_SIZE);
        var profiles = new HashMap<ResourceLocation, List<WeakPointRegion>>();
        for (int i = 0; i < count; i++) {
            var entity = buffer.readResourceLocation();
            int size = CombatCodecs.count(buffer, WeakPointRegion.MAX_PER_ENTITY);
            var regions = new ArrayList<WeakPointRegion>(size);
            for (int j = 0; j < size; j++) {
                regions.add(new WeakPointRegion(buffer.readUtf(64), buffer.readEnum(WeakPointRegion.Anchor.class),
                        buffer.readEnum(WeakPointRegion.Rotation.class), readPoint(buffer), readPoint(buffer),
                        buffer.readEnum(WeakPointRegion.State.class), buffer.readEnum(WeakPointRegion.Part.class)));
            }
            if (profiles.put(entity, List.copyOf(regions)) != null) throw new IllegalArgumentException("Duplicate weak point profile");
        }
        return new WeakPointProfilesPayload(first, last, Map.copyOf(profiles));
    }

    private static void writePoint(RegistryFriendlyByteBuf buffer, WeakPointRegion.Point point) {
        buffer.writeDouble(point.x()).writeDouble(point.y()).writeDouble(point.z());
    }

    private static WeakPointRegion.Point readPoint(RegistryFriendlyByteBuf buffer) {
        return new WeakPointRegion.Point(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
