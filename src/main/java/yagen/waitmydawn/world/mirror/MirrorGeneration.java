package yagen.waitmydawn.world.mirror;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DimensionRegistry;

public class MirrorGeneration {
    public static final ResourceKey<LevelStem> MIRROR_STEM =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID,"mirror"));

    public static void levelBootstrap(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<DimensionType> dimensions = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoisePresets =
                context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        context.register(MIRROR_STEM, levelStem(biomes, noiseSettings, dimensions, multiNoisePresets));
    }

    private static LevelStem levelStem(
            HolderGetter<Biome> biomes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            HolderGetter<DimensionType> dimensions,
            HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoisePresets) {

        Holder<MultiNoiseBiomeSourceParameterList> overworldPreset =
                multiNoisePresets.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(overworldPreset);

        Holder<NoiseGeneratorSettings> overworldNoiseSettings =
                noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(
                biomeSource,
                overworldNoiseSettings
        );

        return new LevelStem(dimensions.getOrThrow(DimensionRegistry.MIRROR), chunkGenerator);
    }
}
