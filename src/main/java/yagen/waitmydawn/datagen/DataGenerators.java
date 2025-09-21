package yagen.waitmydawn.datagen;

import yagen.waitmydawn.YagensAttributes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = event.getGenerator().getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        var provider = event.getLookupProvider();

        DatapackBuiltinEntriesProvider datapackProvider = new RegistryDataGenerator(output, provider);
        CompletableFuture<HolderLookup.Provider> lookupProvider = datapackProvider.getRegistryProvider();
        generator.addProvider(event.includeServer(), datapackProvider);
        generator.addProvider(event.includeServer(), new DamageTypeTagGenerator(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.BlocksGenerator::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.ChestsGenerator::new, LootContextParamSets.CHEST)
                ),
                provider
        ));
        generator.addProvider(event.includeServer(), new LootTableGenerator.GlobalModifierGenerator(output, provider));
        generator.addProvider(event.includeServer(),new RecipeGenerator(output,lookupProvider));
    }
}
