package yagen.waitmydawn.registries;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.capabilities.ReservoirsInventoryHandler;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.item.mod.armor_mod.ReservoirsArmorMod;

public class CapabilitiesRegistry {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> {
                    AbstractMod mod = Mod.getModSlotFromStack(stack).getMod();

                    if (mod instanceof ReservoirsArmorMod) {
                        return new ReservoirsInventoryHandler(stack);
                    }

                    return null;
                },
                ItemRegistry.MOD.get()
        );
    }
}
