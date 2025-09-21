package yagen.waitmydawn.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.api.mods.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD)
public class YagenAttributesModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YagensAttributes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> YAGENS_ATTRIBUTES_ITEMS_TAB = CREATIVE_MODE_TAB.register("yagens_attributes.items", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.yagens_attributes.items"))
            .icon(() -> new ItemStack(ItemRegistry.MOD_ESSENCE.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(ItemRegistry.MOD_ESSENCE.get());
                entries.accept(ItemRegistry.UNKNOWN_RIVEN.get());
                entries.accept(ItemRegistry.KUVA.get());
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> YAGENS_ATTRIBUTES_MODS_TAB = CREATIVE_MODE_TAB.register("yagens_attributes.mods", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.yagens_attributes.mods"))
            .icon(() -> new ItemStack(ItemRegistry.MOD.get()))
            //.withTabsBefore(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "yagens_attributes.items_tab"))
            .build());


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> YAGENS_ATTRIBUTES_BLOCKS_TAB = CREATIVE_MODE_TAB.register("yagens_attributes.blocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.yagens_attributes.blocks"))
            .icon(() -> new ItemStack(ItemRegistry.MOD_OPERATION_BLOCK_ITEM.get()))
            .displayItems((enabledFeatures, entries) -> {
                ItemRegistry.getYagensItems().forEach(holder -> {
                    if (holder.get() instanceof BlockItem) {
                        entries.accept(holder.get());
                    }
                });
            })
            //.withTabsBefore(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "yagens_attributes.mods_tab"))
            .build());

    @SubscribeEvent
    public static void fillCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == YAGENS_ATTRIBUTES_MODS_TAB.get()) {
            ModRegistry.getEnabledMods().stream()
                    .filter(modType -> modType != ModRegistry.none())
                    .forEach(mod -> {
                        if (mod == ModRegistry.getMod("yagens_attributes:riven_tool_mod")) return;
                        for (int i = 1; i <= mod.getMaxLevel(); i++) {
                            var itemstack = new ItemStack(ItemRegistry.MOD.get());
                            var modList = IModContainer.createModContainer(mod, i, itemstack);
                            event.accept(itemstack);
                        }
                    });
        }
        if (event.getTab() == YAGENS_ATTRIBUTES_ITEMS_TAB.get()) {
            for (FormaType formaType : FormaType.values()) {
                event.accept(ItemRegistry.createForma(formaType.getValue()));
            }
        }
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}