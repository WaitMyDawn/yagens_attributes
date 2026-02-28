package yagen.waitmydawn.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.RivenStatsManager;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.config.WeaponStatsManager;
import yagen.waitmydawn.network.BatteryPowerPacket;
import yagen.waitmydawn.network.EnergyPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SyncEvent {
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeaponStatsManager());
        event.addListener(new RivenStatsManager());
    }

    @SubscribeEvent
    public static void syncConfigVersion(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ServerConfigs.SPEC){
            double newVersion = 1.29;
            if (ServerConfigs.CONFIG_VERSION.get() < newVersion) {
                ServerConfigs.MOD_RARE_ANCIENT_STABILIZER.set(5.0);
                ServerConfigs.MOD_RARE_FLEETING_EXPERTISE_DURATION.set(10.0);
                ServerConfigs.MOD_RARE_FLEETING_EXPERTISE_EFFICIENCY.set(10.0);
                ServerConfigs.CONFIG_VERSION.set(newVersion);
                ServerConfigs.SPEC.save();
            }
        }
    }

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
