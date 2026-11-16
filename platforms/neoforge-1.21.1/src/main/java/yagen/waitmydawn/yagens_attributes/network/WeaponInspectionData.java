package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public record WeaponInspectionData(Map<String, Double> resolved, WeaponProgress progress, int usedCapacity, boolean creative) {
    public static void write(RegistryFriendlyByteBuf buffer, WeaponInspectionData value) {
        CombatCodecs.resolved(buffer, value.resolved); CombatCodecs.progress(buffer, value.progress);
        buffer.writeVarInt(value.usedCapacity).writeBoolean(value.creative);
    }
    public static WeaponInspectionData read(RegistryFriendlyByteBuf buffer) {
        var resolved = CombatCodecs.resolved(buffer); var progress = CombatCodecs.progress(buffer);
        int used = buffer.readVarInt();
        if (used < 0 || used > 80_000_000) throw new IllegalArgumentException("Invalid used capacity");
        return new WeaponInspectionData(resolved, progress, used, buffer.readBoolean());
    }
}
