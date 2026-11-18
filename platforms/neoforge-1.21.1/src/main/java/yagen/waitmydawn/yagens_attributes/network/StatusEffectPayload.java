package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.*;

public record StatusEffectPayload(int entityId, UUID entityUuid, Vec3 position, DamageType effect,
                                  int lifetime, int seed, boolean burst, List<Target> targets) implements CustomPacketPayload {
    public static boolean supports(DamageType type) {
        return switch (type) {
            case IMPACT, PUNCTURE, SLASH, ELECTRICITY, MAGNETIC, BLAST, HEAT, TOXIN -> true;
            default -> false;
        };
    }
    public record Target(int id, UUID uuid) { public Target { Objects.requireNonNull(uuid); if (id < 0) throw new IllegalArgumentException("Invalid effect target"); } }
    public StatusEffectPayload {
        Objects.requireNonNull(entityUuid); Objects.requireNonNull(position); Objects.requireNonNull(effect); targets = List.copyOf(targets);
        if (entityId < 0 || !Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)
                || lifetime < 1 || lifetime > 72000 || targets.size() > 32
                || !supports(effect)
                || effect != DamageType.ELECTRICITY && !targets.isEmpty() || targets.stream().map(Target::uuid).distinct().count() != targets.size()
                || targets.stream().map(Target::id).distinct().count() != targets.size())
            throw new IllegalArgumentException("Invalid status effect snapshot");
    }
    public static final Type<StatusEffectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("yagens_attributes", "status_effect_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatusEffectPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.entityId); buffer.writeUUID(value.entityUuid);
        buffer.writeDouble(value.position.x); buffer.writeDouble(value.position.y); buffer.writeDouble(value.position.z);
        buffer.writeEnum(value.effect); buffer.writeVarInt(value.lifetime); buffer.writeInt(value.seed); buffer.writeBoolean(value.burst);
        buffer.writeVarInt(value.targets.size()); for (var target : value.targets) { buffer.writeVarInt(target.id); buffer.writeUUID(target.uuid); }
    }, buffer -> {
        int id = buffer.readVarInt(); UUID uuid = buffer.readUUID(); Vec3 pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        var effect = buffer.readEnum(DamageType.class); int life = buffer.readVarInt(), seed = buffer.readInt(); boolean burst = buffer.readBoolean(); int size = buffer.readVarInt();
        if (size < 0 || size > 32) throw new IllegalArgumentException("Too many effect targets");
        var targets = new ArrayList<Target>(); for (int i = 0; i < size; i++) targets.add(new Target(buffer.readVarInt(), buffer.readUUID()));
        return new StatusEffectPayload(id, uuid, pos, effect, life, seed, burst, targets);
    });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
