package yagen.waitmydawn.registries;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.gui.mod_recycle.ModRecycleMenu;

import java.util.function.Supplier;


public class MenuRegistry {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static final Supplier<MenuType<ModOperationMenu>> MOD_OPERATION_MENU = registerMenuType(ModOperationMenu::new, "mod_operation_menu");
    public static final Supplier<MenuType<ModRecycleMenu>> MOD_RECYCLE_MENU = registerMenuType(ModRecycleMenu::new, "mod_recycle_menu");

}
