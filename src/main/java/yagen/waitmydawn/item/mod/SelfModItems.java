package yagen.waitmydawn.item.mod;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;

public class SelfModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(YagensAttributes.MODID);

    public static final DeferredItem<Item> HEALTH_UP = ITEMS.register("self_mod_health_up",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ARCHANGEL_STAFF = ITEMS.register("self_mod_archangel_staff",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
