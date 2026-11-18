package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.*;

/** Full count snapshots make removals and first-tracking replay independent of potion packets. */
public record StatusMarkersPayload(int entityId, UUID entityUuid, Map<DamageType, Integer> counts, boolean frozen) implements CustomPacketPayload {
    public StatusMarkersPayload(int entityId, UUID entityUuid, Map<DamageType, Integer> counts) {
        this(entityId, entityUuid, counts, false);
    }
    public StatusMarkersPayload {
        Objects.requireNonNull(entityUuid); counts = Map.copyOf(counts);
        if (entityId < 0 || frozen && !counts.containsKey(DamageType.COLD)
                || counts.values().stream().anyMatch(count -> count < 1 || count > 8192)) throw new IllegalArgumentException("Invalid status marker snapshot");
    }
    public static final Type<StatusMarkersPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("yagens_attributes", "status_markers"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatusMarkersPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.entityId()); buffer.writeUUID(value.entityUuid()); buffer.writeBoolean(value.frozen()); buffer.writeVarInt(value.counts().size());
        for (var type : DamageType.values()) if (value.counts().containsKey(type)) { buffer.writeEnum(type); buffer.writeVarInt(value.counts().get(type)); }
    }, buffer -> {
        int id = buffer.readVarInt(); UUID uuid = buffer.readUUID(); boolean frozen = buffer.readBoolean(); int size = buffer.readVarInt();
        if (size < 0 || size > DamageType.values().length) throw new IllegalArgumentException("Invalid marker type count");
        var counts = new EnumMap<DamageType, Integer>(DamageType.class);
        for (int i = 0; i < size; i++) if (counts.put(buffer.readEnum(DamageType.class), buffer.readVarInt()) != null) throw new IllegalArgumentException("Duplicate marker type");
        return new StatusMarkersPayload(id, uuid, counts, frozen);
    });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
