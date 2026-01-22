package yagen.waitmydawn.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.NoneMod;

public class ModJeiInterpreter implements ISubtypeInterpreter<ItemStack> {
    public ModJeiInterpreter() {
    }

    public @Nullable Object getSubtypeData(@NotNull ItemStack itemStack, @NotNull UidContext context) {
        if (!IModContainer.isModContainer(itemStack)) {
            return null;
        }

        AbstractMod mod = IModContainer.get(itemStack).getModAtIndex(0).getMod();
        if (mod instanceof NoneMod) {
            return null;
        }
        if (mod.getMaxLevel() != IModContainer.get(itemStack).getModAtIndex(0).getLevel())
            return null;
        return mod.getModResource();
    }


    public @NotNull String getLegacyStringSubtypeInfo(@NotNull ItemStack ingredient, @NotNull UidContext context) {
        return "null";
    }
}