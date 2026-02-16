package yagen.waitmydawn.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.network.BatteryPowerPacket;
import yagen.waitmydawn.network.EnergyPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SyncEvent {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new EnergyPacket(
                    DataAttachmentRegistry.getEnergy(player)
            ));
            PacketDistributor.sendToPlayer(player, new BatteryPowerPacket(
                    DataAttachmentRegistry.getBatteryPower(player)
            ));
        }
    }
}
