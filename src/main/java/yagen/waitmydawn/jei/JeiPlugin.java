package yagen.waitmydawn.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.NoneMod;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.registries.BlockRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ClientLootLoader.LootInfo> lootStats = ClientLootLoader.scanAll();

        YagensAttributes.LOGGER.info("JEI Loaded {} loot tables with custom mod function.", lootStats.size());

        List<ClientLootLoader.LootInfo> commonLoot = new ArrayList<>();
        List<ClientLootLoader.LootInfo> uncommonLoot = new ArrayList<>();
        List<ClientLootLoader.LootInfo> rareLoot = new ArrayList<>();
        List<ClientLootLoader.LootInfo> legendaryLoot = new ArrayList<>();

        for (ClientLootLoader.LootInfo info : lootStats) {
            if (info.common() > 0) commonLoot.add(info);
            if (info.uncommon() > 0) uncommonLoot.add(info);
            if (info.rare() > 0) rareLoot.add(info);
            if (info.legendary() > 0) legendaryLoot.add(info);
        }

        ModRegistry.REGISTRY.stream().forEach((mod) -> {
            if (mod instanceof NoneMod) return;
            ItemStack stack = new ItemStack(ItemRegistry.MOD.get());
            IModContainer.createModContainer(mod, mod.getMaxLevel(), stack);

            List<ClientLootLoader.LootInfo> sources = switch (mod.getRarity()) {
                case COMMON -> commonLoot;
                case UNCOMMON -> uncommonLoot;
                case RARE -> rareLoot;
                case LEGENDARY -> legendaryLoot;
                default -> new ArrayList<>();
            };

            if (!sources.isEmpty()) {
                List<ModLootWrapper> wrappers = new ArrayList<>();
                for (ClientLootLoader.LootInfo info : sources) {
                    wrappers.add(new ModLootWrapper(info, stack));
                }
                registration.addRecipes(ModLootStatsCategory.TYPE, wrappers);
            }
        });
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.MOD_OPERATION_BLOCK.get()),
                ModLootStatsCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ModLootStatsCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ItemRegistry.MOD.get(), new ModJeiInterpreter());
    }
}
