package yagen.waitmydawn.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get())
                .pattern("MMM")
                .pattern("MMM")
                .pattern("MMM")
                .define('M',ItemRegistry.MOD_ESSENCE.get())
                .unlockedBy("mod_essence_block_item", has(ItemRegistry.MOD_ESSENCE.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ORDIS.get())
                .pattern(" G ")
                .pattern("IAI")
                .pattern(" I ")
                .define('G',Items.GOLD_INGOT)
                .define('I',Items.IRON_INGOT)
                .define('A',Items.AMETHYST_SHARD)
                .unlockedBy("ordis", has(Items.AMETHYST_SHARD))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MOD_OPERATION_BLOCK_ITEM.get())
                .pattern("MOM")
                .pattern("MAM")
                .pattern("   ")
                .define('M',ItemRegistry.MOD_ESSENCE.get())
                .define('O',ItemRegistry.ORDIS.get())
                .define('A',Items.AMETHYST_BLOCK)
                .unlockedBy("mod_operation_table", has(ItemRegistry.ORDIS.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.MOD_RECYCLE_BLOCK_ITEM.get())
                .pattern("OOO")
                .pattern("LMW")
                .pattern("OOO")
                .define('M',ItemRegistry.MOD_ESSENCE.get())
                .define('O',Items.OBSIDIAN)
                .define('L',Items.LAVA_BUCKET)
                .define('W',Items.WATER_BUCKET)
                .unlockedBy("mod_recycle_table", has(ItemRegistry.MOD_ESSENCE.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,ItemRegistry.MOD_ESSENCE.get(),9)
                .requires(ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get())
                .unlockedBy("mod_essence", has(ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get()))
                .save(recipeOutput);
    }

    protected static <T extends AbstractCookingRecipe> void cookRecipes(
            RecipeOutput recipeOutput, String cookingMethod, RecipeSerializer<T> cookingSerializer, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime
    ) {
//        simpleCookingRecipe(recipeOutput, cookingMethod, cookingSerializer, recipeFactory, cookingTime, ModItems.BREAD_SWORD, ModItems.BREAD_SWORD_HOT, 0.35F);
//        simpleCookingRecipe(recipeOutput, cookingMethod, cookingSerializer, recipeFactory, cookingTime, ModItems.BREAD_SWORD_HOT, ModItems.BREAD_SWORD_VERY_HOT, 0.35F);
    }

    protected static <T extends AbstractCookingRecipe> void simpleCookingRecipe(
            RecipeOutput recipeOutput,
            String cookingMethod,
            RecipeSerializer<T> cookingSerializer,
            AbstractCookingRecipe.Factory<T> recipeFactory,
            int cookingTime,
            ItemLike material,
            ItemLike result,
            float experience
    ) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(material), RecipeCategory.FOOD, result, experience, cookingTime, cookingSerializer, recipeFactory)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID,getItemName(result) + "_from_" + cookingMethod));
    }
}
