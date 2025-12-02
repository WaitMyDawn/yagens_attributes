package yagen.waitmydawn.datagen;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DamageTypeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yagen.waitmydawn.registries.DimensionRegistry;
import yagen.waitmydawn.world.mirror.MirrorGeneration;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistryDataGenerator extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, DamageTypeRegistry::bootstrap)
            .add(Registries.LEVEL_STEM, MirrorGeneration::levelBootstrap)
//            .add(Registries.NOISE_SETTINGS, MirrorGeneration::noiseBootstrap)
            .add(Registries.DIMENSION_TYPE, DimensionRegistry::bootstrap);

    public RegistryDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, BUILDER, Set.of("minecraft", YagensAttributes.MODID));
    }
}
