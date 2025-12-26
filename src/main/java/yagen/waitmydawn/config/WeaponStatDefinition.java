package yagen.waitmydawn.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.api.attribute.DamageType;

import java.util.Map;
import java.util.Optional;

public record WeaponStatDefinition(
        Optional<Float> rivenDisposition,
        Optional<Map<ResourceLocation, Double>> attributes,
        Optional<Map<DamageType, Float>> damageDistribution
) {
    public static final Codec<WeaponStatDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("riven_disposition").forGetter(WeaponStatDefinition::rivenDisposition),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("attributes").forGetter(WeaponStatDefinition::attributes),
            Codec.unboundedMap(DamageType.CODEC, Codec.FLOAT).optionalFieldOf("damage_distribution").forGetter(WeaponStatDefinition::damageDistribution)
    ).apply(instance, WeaponStatDefinition::new));
}
