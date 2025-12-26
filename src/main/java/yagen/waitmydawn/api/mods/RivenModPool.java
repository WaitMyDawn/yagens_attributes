package yagen.waitmydawn.api.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RivenModPool {
    private RivenModPool() {
    }

    public static Map<Item, Float> DISPOSITION_MAP = new HashMap<>();

    private static final List<Item> ITEM_LIST = new ArrayList<>();

    public static void clear() {
        DISPOSITION_MAP.clear();
    }

    public static void register(Item item, float disposition) {
        if (item == null || item == Items.AIR || disposition == 0) return;
        if (!DISPOSITION_MAP.containsKey(item)) {
            ITEM_LIST.add(item);
        }

        DISPOSITION_MAP.put(item, disposition);
    }

    public static float getDisposition(Item item) {
        return DISPOSITION_MAP.getOrDefault(item, 0.5f);
    }

    public static Item randomWeapon(RandomSource random) {
        if (ITEM_LIST.isEmpty()) return Items.AIR;
        return ITEM_LIST.get(random.nextInt(ITEM_LIST.size()));
    }
}
