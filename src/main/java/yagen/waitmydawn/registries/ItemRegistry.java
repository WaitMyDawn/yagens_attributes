package yagen.waitmydawn.registries;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.item.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.List;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, YagensAttributes.MODID);

    /**
     * Mod
     */
    public static final DeferredHolder<Item, Item> MOD = ITEMS.register("mod", Mod::new);


    /**
     * Item
     */
    public static final DeferredHolder<Item, Item> MOD_ESSENCE = ITEMS.register("mod_essence",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.yagens_attributes.mod_essence.tooltip1")
                            .withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredHolder<Item, Item> FORMA = ITEMS.register("forma",
            () -> new FormaItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UNKNOWN_RIVEN = ITEMS.register("unknown_riven",
            () -> new UnknownRivenItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KUVA = ITEMS.register("kuva",
            () -> new KuvaItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ORDIS = ITEMS.register("ordis",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PUMILUM = ITEMS.register("pumilum",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GRANDIFLORUM = ITEMS.register("grandiflorum",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HYDRANGEA = ITEMS.register("hydrangea",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> BLADE = ITEMS.register("blade",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SUMMON = ITEMS.register("summon",
            () -> new SummonItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ENDO = ITEMS.register("endo",
            () -> new EndoItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TELEPORT = ITEMS.register("teleport",
            () -> new TeleportItem(new Item.Properties()));

    public static ItemStack createForma(String slotName) {
        ItemStack stack = new ItemStack(ItemRegistry.FORMA.get());
        stack.set(ComponentRegistry.FORMA_TYPE.get(), slotName);
        return stack;
    }
    public static ItemStack createEndo(int level, String missionType) {
        ItemStack stack = new ItemStack(ItemRegistry.ENDO.get());
        ComponentRegistry.setEndoInfo(stack, new ComponentRegistry.EndoInfo(level,missionType));
        return stack;
    }

    /**
     * Block Item
     */
    public static final DeferredHolder<Item, Item> MOD_OPERATION_BLOCK_ITEM = ITEMS.register("mod_operation_table",
            () -> new BlockItem(BlockRegistry.MOD_OPERATION_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOD_RECYCLE_BLOCK_ITEM = ITEMS.register("mod_recycle_table",
            () -> new BlockItem(BlockRegistry.MOD_RECYCLE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOD_ESSENCE_BLOCK_ITEM = ITEMS.register("mod_essence_block",
            () -> new BlockItem(BlockRegistry.MOD_ESSENCE_BLOCK.get(), new Item.Properties()));


    public static Collection<DeferredHolder<Item, ? extends Item>> getYagensItems() {
        return ITEMS.getEntries();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
