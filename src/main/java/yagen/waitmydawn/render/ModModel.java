package yagen.waitmydawn.render;

import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.AbstractMod;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.api.mods.NoneMod;

import java.util.Optional;

public class ModModel extends NBTOverrideItemModel {
    public ModModel(BakedModel original, ModelBakery loader) {
        super(original, loader);
    }


    @Override
    Optional<ResourceLocation> getModelFromStack(ItemStack itemStack) {
        if (IModContainer.isModContainer(itemStack)) {
            AbstractMod mod = IModContainer.get(itemStack).getModAtIndex(0).getMod();
            if (!(mod instanceof NoneMod)) {
                return Optional.of(getModModelLocation(mod));
            }
        }
        return Optional.empty(); // default
    }

    public static ResourceLocation getModModelLocation(AbstractMod mod) {
        return ResourceLocation.fromNamespaceAndPath(
                mod.getModResource().getNamespace(), // yagens_attributes
                String.format("item/%s", mod.getModResource().getPath()) // such as slash_up_tool_mod
        );
    }
}