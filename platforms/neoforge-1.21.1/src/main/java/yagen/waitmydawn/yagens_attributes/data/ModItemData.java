package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Physical cards are stackable templates; account instance UUIDs are allocated only on deposit. */
public record ModItemData(ResourceLocation type, int level) {
    public ModItemData { if (type == null || level < 0 || level > 60) throw new IllegalArgumentException("Invalid Mod item data"); }
    public static final Codec<ModItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(ModItemData::type), Codec.intRange(0, 60).fieldOf("level").forGetter(ModItemData::level)).apply(instance, ModItemData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModItemData> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> { buffer.writeResourceLocation(value.type); buffer.writeVarInt(value.level); },
            buffer -> new ModItemData(buffer.readResourceLocation(), buffer.readVarInt()));
}
