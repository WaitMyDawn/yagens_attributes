package yagen.waitmydawn.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.jei.ClientLootLoader.LootInfo;

import static yagen.waitmydawn.api.util.ModCompat.TRANSFORM_POOL_BY_RARITY;

public class ModLootStatsCategory implements IRecipeCategory<ModLootWrapper> {

    public static final RecipeType<ModLootWrapper> TYPE = RecipeType.create(YagensAttributes.MODID, "loot_stats", ModLootWrapper.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ModLootStatsCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 65);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.CHEST));
    }

    @Override
    public RecipeType<ModLootWrapper> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.yagens_attributes.loot_stats_title");
    }

    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ModLootWrapper recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 5, 20)
                .addItemStack(new ItemStack(Items.CHEST));

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 135, 20)
                .addItemStack(recipe.resultStack());

        builder.createFocusLink(inputSlot, outputSlot);
    }

    @Override
    public void draw(ModLootWrapper wrapper, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        int xPos = 30;
        int yPos = 5;
        int lineHeight = 10;
        LootInfo recipe = wrapper.info();

        ModRarity currentRarity = ModRarity.RIVEN;
        try {
            var container = IModContainer.get(wrapper.resultStack());
            if (container != null && container.getModAtIndex(0).getMod() != null) {
                currentRarity = container.getModAtIndex(0).getMod().getRarity();
            }
        } catch (Exception ignored) {
        }

        String tableName = recipe.tableId().getPath();
        int slashIndex = tableName.lastIndexOf('/');
        int additionalIndex = tableName.indexOf("additional_");
        if (slashIndex != -1 && additionalIndex > slashIndex) {
            String prefix = tableName.substring(0, slashIndex + 1);
            String suffix = tableName.substring(additionalIndex + "additional_".length());
            tableName = prefix + suffix;
        }
        tableName=deleteRepeatString(tableName);

        int maxWidth = 120;
        int textWidth = font.width(tableName);

        if (textWidth <= maxWidth) {
            guiGraphics.drawString(font, tableName, xPos, yPos, 0xFFFFFF, true);
        } else {
            int overflow = textWidth - maxWidth;
            double speed = 15.0;
            double pauseTime = 1.5;
            double moveDuration = overflow / speed;
            double totalCycleTime = pauseTime + moveDuration + pauseTime + moveDuration;
            double currentTime = (Util.getMillis() / 1000.0) % totalCycleTime;
            double progress;

            if (currentTime < pauseTime) {
                progress = 0.0;
            } else if (currentTime < pauseTime + moveDuration) {
                progress = (currentTime - pauseTime) / moveDuration;
            } else if (currentTime < pauseTime + moveDuration + pauseTime) {
                progress = 1.0;
            } else {
                double timeBack = currentTime - (pauseTime + moveDuration + pauseTime);
                progress = 1.0 - (timeBack / moveDuration);
            }
            int xOffset = (int) (overflow * progress);
            Matrix4f matrix = guiGraphics.pose().last().pose();
            int absoluteX = (int) (matrix.m30() + xPos);
            int absoluteY = (int) (matrix.m31() + yPos);
            guiGraphics.enableScissor(absoluteX, absoluteY, absoluteX + maxWidth, absoluteY + font.lineHeight);
            guiGraphics.drawString(font, tableName, xPos - xOffset, yPos, 0xFFFFFF, true);
            guiGraphics.disableScissor();
        }

        yPos += lineHeight + 2;
        String levelStr = String.format("Mod Level: %d%% ~ %d%%", recipe.minLevel(), recipe.maxLevel());
        guiGraphics.drawString(font, levelStr, xPos, yPos, 0x006600, false);

        yPos += lineHeight + 4;
        if (currentRarity != ModRarity.RIVEN && currentRarity != ModRarity.WARFRAME) {
            int totalWeight = recipe.common() + recipe.uncommon() + recipe.rare() + recipe.legendary();
            int wight = switch (currentRarity) {
                case COMMON -> recipe.common();
                case UNCOMMON -> recipe.uncommon();
                case RARE -> recipe.rare();
                case LEGENDARY -> recipe.legendary();
                default -> 0;
            };
            if (wight > 0) {
                Component chance = Component.literal(String.format("Chance: %.1f%% ", 100.0 * wight / totalWeight * recipe.baseChance() / TRANSFORM_POOL_BY_RARITY.get(currentRarity).size()));
                guiGraphics.drawString(font, chance, xPos, yPos, 0x000000, false);
                String rollsText;
                if (Math.abs(recipe.minRolls() - recipe.maxRolls()) < 0.01f) {
                    rollsText = String.format("Rolls: %d", (int) recipe.minRolls());
                } else {
                    rollsText = String.format("Rolls: %d-%d", (int) recipe.minRolls(), (int) recipe.maxRolls());
                }
                guiGraphics.drawString(font, rollsText, xPos + font.width(chance), yPos, 0x000000, false);
            }

            yPos += lineHeight;
        }
        int currentX = xPos;

        if (recipe.common() > 0) {
            Component s = Component.translatable("rarity.yagens_attributes.common")
                    .append(":").append(String.valueOf(recipe.common())).append(" ");
            guiGraphics.drawString(font, s, currentX, yPos, 0xB87333, true);
            currentX += font.width(s);
        }
        if (recipe.uncommon() > 0) {
            Component s = Component.translatable("rarity.yagens_attributes.uncommon")
                    .append(":").append(String.valueOf(recipe.uncommon())).append(" ");
            guiGraphics.drawString(font, s, currentX, yPos, 0xC0C0C0, true);
            currentX += font.width(s);
        }
        if (recipe.rare() > 0) {
            Component s = Component.translatable("rarity.yagens_attributes.rare")
                    .append(":").append(String.valueOf(recipe.rare())).append(" ");
            guiGraphics.drawString(font, s, currentX, yPos, 0xFFD700, true);
            currentX += font.width(s);
        }
        if (recipe.legendary() > 0) {
            Component s = Component.translatable("rarity.yagens_attributes.legendary")
                    .append(":").append(String.valueOf(recipe.legendary()));
            guiGraphics.drawString(font, s, currentX, yPos, 0xFF4500, true);
        }
    }

    // input: archeology/kisegi_sanctuary_kisegi_sanctuary_archeology
    // return: archeology/kisegi_sanctuary_archeology
    public static String deleteRepeatString(String input) {
        if (input == null || input.isEmpty()) return input;
        int firstSlash = input.indexOf('/');
        if (firstSlash == -1 || firstSlash == input.length() - 1) return input;
        String prefix = input.substring(0, firstSlash + 1);
        String path = input.substring(firstSlash + 1);
        String[] parts = path.split("_");
        if (parts.length <= 1) return input;
        StringBuilder result = new StringBuilder(path.length());
        int i = 0;
        while (i < parts.length) {
            boolean duplicated = false;
            int maxLen = (parts.length - i) / 2;
            for (int len = 1; len <= maxLen; len++) {
                boolean same = true;
                for (int j = 0; j < len; j++) {
                    if (!parts[i + j].equals(parts[i + len + j])) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    for (int j = 0; j < len; j++) {
                        result.append(parts[i + j]).append('_');
                    }
                    i += 2 * len;
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated) {
                result.append(parts[i]).append('_');
                i++;
            }
        }
        if (result.length() > 0)
            result.setLength(result.length() - 1);
        return prefix + result;
    }

}