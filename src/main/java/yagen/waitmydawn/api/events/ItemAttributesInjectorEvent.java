package yagen.waitmydawn.api.events;

import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DefaultItemAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ItemAttributesInjectorEvent {
    @SubscribeEvent
    public static void onComputeModifiers(ItemAttributeModifierEvent e) {
        DefaultItemAttributes.apply(e.getItemStack());
    }
}