package yagen.waitmydawn.render;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.NoneMod;

import java.util.Optional;

public class EndoModel extends NBTOverrideItemModel {
    public EndoModel(BakedModel original, ModelBakery loader) {
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
                mod.getModResource().getNamespace(),
                String.format("item/%s", mod.getModResource().getPath()) //
        );
    }
}