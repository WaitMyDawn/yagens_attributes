package yagen.waitmydawn.compat;

import net.minecraft.world.item.Item;
import yagen.waitmydawn.tennory.registries.ItemRegistry;

public class TennoryCompat {
    public static boolean isGlaive(Item item) {
        return item == ItemRegistry.GLAIVE.get();
    }
}
