package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class CombatCodecs {
    private CombatCodecs() {}

    public static void progress(RegistryFriendlyByteBuf buffer, WeaponProgress progress) { buffer.writeVarInt(progress.level()).writeVarInt(progress.experience()); }
    public static WeaponProgress progress(RegistryFriendlyByteBuf buffer) { return new WeaponProgress(buffer.readVarInt(), buffer.readVarInt()); }
    public static void resolved(RegistryFriendlyByteBuf buffer, Map<String, Double> values) {
        buffer.writeVarInt(values.size()); values.forEach((key, value) -> buffer.writeUtf(key, 64).writeDouble(value));
    }
    public static Map<String, Double> resolved(RegistryFriendlyByteBuf buffer) {
        int count = count(buffer, Stats.KEYS.size()); var values = new HashMap<String, Double>();
        for (int i = 0; i < count; i++) {
            String key = buffer.readUtf(64); double value = buffer.readDouble();
            if (!Stats.KEYS.contains(key) || !Double.isFinite(value) || values.put(key, value) != null) throw new IllegalArgumentException("Invalid resolved stat");
        }
        return Map.copyOf(values);
    }

    public static int count(RegistryFriendlyByteBuf buffer, int maximum) {
        int value = buffer.readVarInt();
        if (value < 0 || value > maximum) throw new IllegalArgumentException("Invalid collection size");
        return value;
    }

    public static UUID uuid(RegistryFriendlyByteBuf buffer) { return buffer.readBoolean() ? buffer.readUUID() : null; }
    public static void uuid(RegistryFriendlyByteBuf buffer, UUID value) {
        buffer.writeBoolean(value != null); if (value != null) buffer.writeUUID(value);
    }

    public static void profile(RegistryFriendlyByteBuf buffer, WeaponProfile value) {
        buffer.writeDouble(value.criticalChance()).writeDouble(value.criticalDamage()).writeDouble(value.statusChance()).writeDouble(value.statusDuration());
        buffer.writeDouble(value.fireRate()).writeDouble(value.multishot()).writeDouble(value.rivenDisposition());
        for (DamageType type : DamageType.values()) buffer.writeDouble(value.distribution().getOrDefault(type, 0D));
        buffer.writeDouble(value.baseDamage()).writeEnum(value.category()).writeVarInt(value.slots().size());
        for (var polarity : value.slots()) buffer.writeEnum(polarity);
    }

    public static WeaponProfile profile(RegistryFriendlyByteBuf buffer) {
        double cc = buffer.readDouble(), cd = buffer.readDouble(), sc = buffer.readDouble(), duration = buffer.readDouble();
        double fireRate = buffer.readDouble(), multishot = buffer.readDouble(), disposition = buffer.readDouble();
        var weights = new EnumMap<DamageType, Double>(DamageType.class);
        for (DamageType type : DamageType.values()) weights.put(type, buffer.readDouble());
        double baseDamage = buffer.readDouble();
        var category = buffer.readEnum(ModInfo.Category.class);
        int count = count(buffer, 64);
        var slots = new ArrayList<ModInfo.Polarity>();
        for (int i = 0; i < count; i++) slots.add(buffer.readEnum(ModInfo.Polarity.class));
        return new WeaponProfile(cc, cd, sc, duration, weights, baseDamage, category, slots, fireRate, multishot, disposition);
    }

    public static void bonuses(RegistryFriendlyByteBuf buffer, Map<String, PanelPayload.ModifierView> bonuses) {
        buffer.writeVarInt(bonuses.size());
        for (var entry : bonuses.entrySet()) {
            var value = entry.getValue();
            buffer.writeUtf(entry.getKey(), 64);
            buffer.writeDouble(value.addin()).writeDouble(value.addition()).writeUtf(value.multiply(), 256).writeDouble(value.addout()).writeDouble(value.inputAddin()).writeDouble(value.inputAddition()).writeBoolean(value.multiplyNegative());
        }
    }

    public static Map<String, PanelPayload.ModifierView> bonuses(RegistryFriendlyByteBuf buffer) {
        int size = count(buffer, Stats.KEYS.size());
        Map<String, PanelPayload.ModifierView> result = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buffer.readUtf(64);
            if (!Stats.KEYS.contains(key) || result.containsKey(key)) throw new IllegalArgumentException("Invalid stat key");
            double in = buffer.readDouble(), addition = buffer.readDouble();
            String factors = buffer.readUtf(256);
            double out = buffer.readDouble(), reference = buffer.readDouble(), relativeAddition = buffer.readDouble();
            if (!Double.isFinite(in) || !Double.isFinite(addition) || !Double.isFinite(out) || !Double.isFinite(reference) || !Double.isFinite(relativeAddition)) throw new IllegalArgumentException("Invalid modifier summary");
            result.put(key, new PanelPayload.ModifierView(in, addition, factors, out, reference, relativeAddition, buffer.readBoolean()));
        }
        return Map.copyOf(result);
    }
}
