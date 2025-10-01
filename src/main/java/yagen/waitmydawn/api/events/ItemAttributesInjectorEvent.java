package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DefaultItemAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ItemAttributesInjectorEvent {
    @SubscribeEvent
    public static void onComputeModifiers(ItemAttributeModifierEvent e) {
//        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("l2archery", "starter_bow"));
//        if (e.getItemStack().is(item)) {
//            DefaultItemAttributes.apply(e.getItemStack());
//        }else
        DefaultItemAttributes.apply(e.getItemStack());
    }
}