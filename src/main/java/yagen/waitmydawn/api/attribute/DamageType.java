package yagen.waitmydawn.api.attribute;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum DamageType {
    PUNCTURE,
    SLASH,
    IMPACT,
    COLD,
    HEAT,
    ELECTRICITY,
    TOXIN,
    CORROSIVE,
    VIRAL,
    MAGNETIC,
    RADIATION,
    BLAST,
    GAS;

    public static final Codec<DamageType> CODEC = Codec.STRING.xmap(
            DamageType::valueOf,
            DamageType::name
    );
    public static final StreamCodec<ByteBuf, DamageType> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(DamageType::valueOf, DamageType::name);
}