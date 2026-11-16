package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

/** Item-local slot overrides preserve unmodified data-pack defaults and travel with the weapon. */
public record WeaponPolarityData(List<Slot> overrides) {
    public static final WeaponPolarityData EMPTY = new WeaponPolarityData(List.of());
    private static final Codec<ModInfo.Polarity> POLARITY = Codec.STRING.comapFlatMap(value -> {
        for (var polarity : ModInfo.Polarity.values()) if (polarity.key().equals(value)) return DataResult.success(polarity);
        return DataResult.error(() -> "Unknown polarity: " + value);
    }, ModInfo.Polarity::key);

    public record Slot(int index, ModInfo.Polarity polarity) {
        public Slot {
            if (index < 0 || index >= 64) throw new IllegalArgumentException("Invalid polarity slot");
            Objects.requireNonNull(polarity);
        }
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 63).fieldOf("slot").forGetter(Slot::index),
                POLARITY.fieldOf("polarity").forGetter(Slot::polarity)).apply(instance, Slot::new));
    }

    public WeaponPolarityData {
        overrides = overrides.stream().sorted(Comparator.comparingInt(Slot::index)).toList();
        if (overrides.size() > 64 || overrides.stream().map(Slot::index).distinct().count() != overrides.size())
            throw new IllegalArgumentException("Duplicate or excessive polarity overrides");
    }

    public static final Codec<WeaponPolarityData> CODEC = Slot.CODEC.listOf().comapFlatMap(values -> {
        try { return DataResult.success(new WeaponPolarityData(values)); }
        catch (IllegalArgumentException exception) { return DataResult.error(exception::getMessage); }
    }, WeaponPolarityData::overrides);
    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponPolarityData> STREAM_CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.overrides.size());
        for (var slot : value.overrides) buffer.writeVarInt(slot.index()).writeEnum(slot.polarity());
    }, buffer -> {
        int count = buffer.readVarInt();
        if (count < 0 || count > 64) throw new IllegalArgumentException("Invalid polarity count");
        var slots = new ArrayList<Slot>();
        for (int i = 0; i < count; i++) slots.add(new Slot(buffer.readVarInt(), buffer.readEnum(ModInfo.Polarity.class)));
        return new WeaponPolarityData(slots);
    });

    public WeaponPolarityData with(int index, ModInfo.Polarity polarity) {
        var next = new ArrayList<>(overrides.stream().filter(slot -> slot.index() != index).toList());
        next.add(new Slot(index, polarity));
        return new WeaponPolarityData(next);
    }

    public List<ModInfo.Polarity> resolve(List<ModInfo.Polarity> defaults, int minimumSlots) {
        int count = Math.max(defaults.size(), minimumSlots);
        for (var slot : overrides) count = Math.max(count, slot.index() + 1);
        if (count < 1 || count > 64) throw new IllegalArgumentException("Invalid slot count");
        var result = new ArrayList<ModInfo.Polarity>();
        for (int i = 0; i < count; i++) result.add(PolarityRules.slot(defaults, i));
        for (var slot : overrides) result.set(slot.index(), slot.polarity());
        return List.copyOf(result);
    }

    public static WeaponProfile profile(ItemStack stack, WeaponProfile defaults, ModLedger ledger) {
        if (defaults == null) return null;
        var id = stack.get(Components.WEAPON_ID.get());
        var slots = stack.getOrDefault(Components.WEAPON_POLARITIES.get(), EMPTY).resolve(defaults.slots(), ledger.slotCount(id, defaults.slots().size()));
        return new WeaponProfile(defaults.criticalChance(), defaults.criticalDamage(), defaults.statusChance(), defaults.statusDuration(), defaults.distribution(), defaults.baseDamage(), defaults.category(), slots, defaults.fireRate(), defaults.multishot(), defaults.rivenDisposition());
    }
}
