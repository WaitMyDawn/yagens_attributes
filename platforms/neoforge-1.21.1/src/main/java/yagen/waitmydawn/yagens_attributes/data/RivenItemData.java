package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import yagen.waitmydawn.yagens_attributes.core.RivenRoll;

public final class RivenItemData {
    private RivenItemData() {}
    public static final Codec<RivenRoll> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { if (value.length() > 4096) throw new IllegalArgumentException("Riven payload too large"); return DataResult.success(RivenJson.roll(value)); }
        catch (RuntimeException exception) { return DataResult.error(() -> "Invalid Riven payload: " + exception.getMessage()); }
    }, RivenJson::roll);
    public static final StreamCodec<RegistryFriendlyByteBuf, RivenRoll> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(RivenJson.roll(value), 4096), buffer -> RivenJson.roll(buffer.readUtf(4096)));
}
