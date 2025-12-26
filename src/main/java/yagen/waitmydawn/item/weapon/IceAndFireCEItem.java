package yagen.waitmydawn.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import yagen.waitmydawn.util.SupportedMod;

import java.util.function.Supplier;

public class IceAndFireCEItem {
    private IceAndFireCEItem() {
    }
    public static final Supplier<Item> TIDE_TRIDENT =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(SupportedMod.IAF.getValue(), "tide_trident"));
}
