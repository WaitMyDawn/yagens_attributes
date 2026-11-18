package yagen.waitmydawn.yagens_attributes.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.registries.ModBlocks;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GalliumData {
    private static final String MOD_ID = YagensAttributes.MODID;
    private static final ResourceLocation ORE_ID = id("gallium_ore");
    private static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_ORE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ORE_ID);
    private static final ResourceKey<PlacedFeature> PLACED_ORE = ResourceKey.create(Registries.PLACED_FEATURE, ORE_ID);
    private static final ResourceLocation COMMON_ORE = common("ores/gallium");

    private GalliumData() {}

    public static void gather(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var registries = event.getLookupProvider();
        var existing = event.getExistingFileHelper();
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, registries, worldgen(), Set.of(MOD_ID)));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(OreLoot::new, LootContextParamSets.BLOCK)), registries));
        var blocks = new OreTags(output, registries, existing);
        generator.addProvider(event.includeServer(), blocks);
        generator.addProvider(event.includeServer(), new ItemTagsProvider(output, registries, blocks.contentsGetter(), MOD_ID, existing) {
            @Override
            protected void addTags(HolderLookup.Provider lookup) {
                tag(TagKey.create(Registries.ITEM, COMMON_ORE)).add(ModItems.GALLIUM_ORE.get(), ModItems.DEEPSLATE_GALLIUM_ORE.get());
                tag(Tags.Items.ORES).addTag(TagKey.create(Registries.ITEM, COMMON_ORE));
                var raw = TagKey.create(Registries.ITEM, common("raw_materials/gallium"));
                var refined = TagKey.create(Registries.ITEM, common("ingots/gallium"));
                tag(raw).add(ModItems.RAW_GALLIUM.get());
                tag(refined).add(ModItems.GALLIUM.get());
                tag(Tags.Items.RAW_MATERIALS).addTag(raw);
                tag(Tags.Items.INGOTS).addTag(refined);
            }
        });
        generator.addProvider(event.includeServer(), new Recipes(output, registries));
        generator.addProvider(event.includeClient(), new Models(output, existing));
    }

    private static RegistrySetBuilder worldgen() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, context -> {
                    var stone = OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                            ModBlocks.GALLIUM_ORE.get().defaultBlockState());
                    var deepslate = OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                            ModBlocks.DEEPSLATE_GALLIUM_ORE.get().defaultBlockState());
                    // Match the main gold batch's count, size, and exposure rule within the gallium height range.
                    context.register(CONFIGURED_ORE, new ConfiguredFeature<>(Feature.ORE,
                            new OreConfiguration(List.of(stone, deepslate), 9, 0.5F)));
                })
                .add(Registries.PLACED_FEATURE, context -> {
                    var ore = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CONFIGURED_ORE);
                    var heights = HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(48));
                    context.register(PLACED_ORE, new PlacedFeature(ore,
                            List.of(CountPlacement.of(4), InSquarePlacement.spread(), heights, BiomeFilter.biome())));
                })
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                    var overworld = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);
                    var ore = context.lookup(Registries.PLACED_FEATURE).getOrThrow(PLACED_ORE);
                    context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ORE_ID),
                            new BiomeModifiers.AddFeaturesBiomeModifier(overworld, HolderSet.direct(ore), GenerationStep.Decoration.UNDERGROUND_ORES));
                });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static ResourceLocation common(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    private static final class OreTags extends BlockTagsProvider {
        private OreTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existing) {
            super(output, registries, MOD_ID, existing);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            var gallium = TagKey.create(Registries.BLOCK, COMMON_ORE);
            tag(gallium).add(ModBlocks.GALLIUM_ORE.get(), ModBlocks.DEEPSLATE_GALLIUM_ORE.get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(gallium);
            tag(BlockTags.NEEDS_STONE_TOOL).addTag(gallium);
            tag(Tags.Blocks.ORES).addTag(gallium);
        }
    }

    private static final class OreLoot extends BlockLootSubProvider {
        private OreLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        }

        @Override
        protected void generate() {
            add(ModBlocks.GALLIUM_ORE.get(), ore -> createOreDrop(ore, ModItems.RAW_GALLIUM.get()));
            add(ModBlocks.DEEPSLATE_GALLIUM_ORE.get(), ore -> createOreDrop(ore, ModItems.RAW_GALLIUM.get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.GALLIUM_ORE.get(), ModBlocks.DEEPSLATE_GALLIUM_ORE.get());
        }
    }

    private static final class Recipes extends RecipeProvider {
        private Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            var gallium = ModItems.GALLIUM.get();
            List<ItemLike> inputs = List.of(ModItems.RAW_GALLIUM.get(), ModItems.GALLIUM_ORE.get(), ModItems.DEEPSLATE_GALLIUM_ORE.get());
            for (ItemLike input : inputs) {
                String name = getItemName(input);
                var ingredient = Ingredient.of(input);
                SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, gallium, 0.7F, 200)
                        .group("gallium").unlockedBy("has_" + name, has(input))
                        .save(output, id("gallium_from_smelting_" + name));
                SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, gallium, 0.7F, 100)
                        .group("gallium").unlockedBy("has_" + name, has(input))
                        .save(output, id("gallium_from_blasting_" + name));
            }
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OROKIN_CATALYST.get())
                    .pattern("JJ ")
                    .pattern("JZI")
                    .pattern(" IJ")
                    .define('J', gallium)
                    .define('I', Items.IRON_INGOT)
                    .define('Z', Items.AMETHYST_SHARD)
                    .unlockedBy("has_gallium", has(gallium))
                    .save(output, id("orokin_catalyst"));
        }
    }

    private static final class Models extends BlockStateProvider {
        private Models(PackOutput output, ExistingFileHelper existing) {
            super(output, MOD_ID, existing);
        }

        @Override
        protected void registerStatesAndModels() {
            var stone = ModBlocks.GALLIUM_ORE.get();
            var deepslate = ModBlocks.DEEPSLATE_GALLIUM_ORE.get();
            simpleBlockWithItem(stone, cubeAll(stone));
            simpleBlockWithItem(deepslate, cubeAll(deepslate));
            itemModels().basicItem(ModItems.RAW_GALLIUM.get());
            itemModels().basicItem(ModItems.GALLIUM.get());
        }
    }
}
