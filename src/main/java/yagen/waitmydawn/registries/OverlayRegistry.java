package yagen.waitmydawn.registries;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.overlays.ComboCountOverlay;
import yagen.waitmydawn.gui.overlays.ScanInfoOverlay;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class OverlayRegistry {
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event){
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_count"), ComboCountOverlay.instance);
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scan_info"), ScanInfoOverlay.instance);
    }
}
