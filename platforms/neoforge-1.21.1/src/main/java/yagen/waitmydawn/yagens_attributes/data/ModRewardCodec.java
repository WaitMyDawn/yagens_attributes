package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import yagen.waitmydawn.yagens_attributes.core.ModInfo;
import yagen.waitmydawn.yagens_attributes.core.loot.ModReward;
import java.util.*;

/** Reward parameters can be decoded by data tools without bootstrapping Minecraft registries. */
public final class ModRewardCodec {
    private static final Codec<ModReward.Method> METHOD = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(ModReward.Method.valueOf(value.toUpperCase(Locale.ROOT))); } catch (IllegalArgumentException failure) { return DataResult.error(() -> "Unknown reward method: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
    private static final Codec<ModInfo.Rarity> RARITY = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(ModInfo.Rarity.valueOf(value.toUpperCase(Locale.ROOT))); } catch (IllegalArgumentException failure) { return DataResult.error(() -> "Unknown quality: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
    public static final Codec<ModReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            METHOD.fieldOf("method").forGetter(ModReward::method), Codec.unboundedMap(RARITY, Codec.DOUBLE).optionalFieldOf("quality_weights", Map.of()).forGetter(ModReward::qualityWeights),
            Codec.STRING.optionalFieldOf("mod", "").forGetter(ModReward::mod), Codec.intRange(0, 100).optionalFieldOf("min_rank_percent", 0).forGetter(ModReward::minPercent),
            Codec.intRange(0, 100).optionalFieldOf("max_rank_percent", 100).forGetter(ModReward::maxPercent)).apply(instance, ModReward::new));
    private ModRewardCodec() {}
}
