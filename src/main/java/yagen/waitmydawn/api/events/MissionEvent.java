package yagen.waitmydawn.api.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;

import java.util.Objects;

import static yagen.waitmydawn.api.mission.MissionData.getExterminateAreaCount;


@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionEvent {
    @SubscribeEvent
    public static void exterminateEvent(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MissionData data = MissionData.get(server);
        var active = data.getPlayerActiveTask(player);
        if (active == null) return;
        MissionData.SharedTaskData sData = active.getValue();
        ResourceLocation taskId = active.getKey();

//        var nearestPlayer = MissionData.nearestPlayer(server, sData);

        data.addProgress(player.level().dimension().location(), taskId);
    }

    @SubscribeEvent
    public static void missionEntitySummonEvent(PlayerTickEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MissionData data = MissionData.get(server);
        var active = data.getPlayerActiveTask(player);
        if (active == null) return;
        MissionData.SharedTaskData sData = active.getValue();
        ResourceLocation taskId = active.getKey();

        var nearestPlayerMap = MissionData.nearestPlayer(server, sData);
        Player nearestPlayer = nearestPlayerMap.getKey();
        if (!(nearestPlayer == player)) return;
        double moveDistance = (int) (sData.distance - nearestPlayerMap.getValue());
        int areaCount = getExterminateAreaCount(sData);
    }



}
