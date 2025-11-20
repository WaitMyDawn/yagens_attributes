package yagen.waitmydawn.render;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.NoneMod;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.Optional;

public class EndoModel extends NBTOverrideItemModel {
    public EndoModel(BakedModel original, ModelBakery loader) {
        super(original, loader);
    }


    @Override
    Optional<ResourceLocation> getModelFromStack(ItemStack itemStack) {
        int level = ComponentRegistry.getEndoInfo(itemStack).level();
        if(level>0){
            return Optional.of(getModModelLocation(level));
        }
        return Optional.empty(); // default
    }

    public static ResourceLocation getModModelLocation(int level) {
        return ResourceLocation.fromNamespaceAndPath(
                YagensAttributes.MODID,
                String.format("item/%s", levelToString(level))
        );
    }

    private static String levelToString(int level) {
        switch (level) {
            case 1 ->{return "rare_endo";}
            case 2 ->{return "spark_endo";}
            default ->{return "intact_endo";}
        }
    }
}