package yagen.waitmydawn.player;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.util.ServerTasks;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonServerEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerTasks.tick(event.getServer());
    }
}