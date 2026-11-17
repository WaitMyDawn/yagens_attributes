package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public record WeaponBuffsPayload(UUID weapon, long time, List<WeaponBuff> buffs) implements CustomPacketPayload {
    public WeaponBuffsPayload {
        buffs = List.copyOf(buffs);
        if (time < 0 || buffs.size() > 256 || weapon == null && !buffs.isEmpty()) throw new IllegalArgumentException("Invalid buff snapshot");
        var keys = new HashSet<String>();
        for (var buff : buffs) {
            if (!keys.add(buff.mod() + "/" + buff.index()) || buff.deadlines().stream().anyMatch(value -> value <= time || value - time > 72000L * 64))
                throw new IllegalArgumentException("Invalid buff deadlines or duplicate key");
        }
    }
    public static final Type<WeaponBuffsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("yagens_attributes", "weapon_buffs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponBuffsPayload> CODEC = StreamCodec.of((buffer, value) -> {
        CombatCodecs.uuid(buffer, value.weapon); buffer.writeVarLong(value.time); buffer.writeVarInt(value.buffs.size());
        for (var buff : value.buffs) {
            buffer.writeUUID(buff.mod()); buffer.writeVarInt(buff.index()); buffer.writeResourceLocation(ResourceLocation.parse(buff.type()));
            buffer.writeVarInt(buff.rank()); buffer.writeUtf(buff.stat(), 64); buffer.writeDouble(buff.addition()); buffer.writeBoolean(buff.perTargetStatus());
            buffer.writeVarInt(buff.duration()); buffer.writeVarInt(buff.maximum()); buffer.writeEnum(buff.decay()); buffer.writeVarInt(buff.deadlines().size());
            buff.deadlines().forEach(buffer::writeVarLong);
        }
    }, buffer -> {
        UUID weapon = CombatCodecs.uuid(buffer); long time = buffer.readVarLong(); int size = buffer.readVarInt();
        if (size < 0 || size > 256) throw new IllegalArgumentException("Too many weapon buffs");
        var buffs = new ArrayList<WeaponBuff>();
        for (int i = 0; i < size; i++) {
            UUID mod = buffer.readUUID(); int index = buffer.readVarInt(); String type = buffer.readResourceLocation().toString(); int rank = buffer.readVarInt();
            String stat = buffer.readUtf(64); double addition = buffer.readDouble(); boolean target = buffer.readBoolean();
            int duration = buffer.readVarInt(), maximum = buffer.readVarInt(); var decay = buffer.readEnum(ConditionalModifier.Decay.class); int count = buffer.readVarInt();
            if (count < 1 || count > 64) throw new IllegalArgumentException("Invalid buff layer count");
            var deadlines = new ArrayList<Long>(); for (int j = 0; j < count; j++) deadlines.add(buffer.readVarLong());
            buffs.add(new WeaponBuff(mod, index, type, rank, stat, addition, target, duration, maximum, decay, deadlines));
        }
        return new WeaponBuffsPayload(weapon, time, buffs);
    });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
