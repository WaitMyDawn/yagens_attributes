package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.data.StatusState;
import yagen.waitmydawn.yagens_attributes.network.StatusMarkersPayload;
import java.util.*;

@EventBusSubscriber(modid = "yagens_attributes")
public final class StatusMarkerSync {
    private static final Set<LivingEntity> DIRTY = Collections.newSetFromMap(new IdentityHashMap<>());
    private StatusMarkerSync() {}
    public static StatusMarkersPayload snapshot(LivingEntity target) {
        var counts = new EnumMap<DamageType, Integer>(DamageType.class);
        if (target.isAlive()) for (var layer : StatusEffects.state(target).layers) counts.merge(layer.type(), 1, Integer::sum);
        return new StatusMarkersPayload(target.getId(), target.getUUID(), counts);
    }
    public static void changed(LivingEntity target, StatusState state, int[] counts) {
        if (!(target.level() instanceof ServerLevel) || Arrays.equals(state.markerCounts, counts)) return;
        DIRTY.add(target);
    }
    public static void changed(LivingEntity target) {
        var state = StatusEffects.state(target); int[] counts = new int[DamageType.values().length];
        for (var layer : state.layers) counts[layer.type().ordinal()]++;
        changed(target, state, counts);
    }
    public static void tracking(ServerPlayer player, LivingEntity target) {
        if (target.hasData(yagen.waitmydawn.yagens_attributes.registries.RangedAttachments.STATUS)) PacketDistributor.sendToPlayer(player, snapshot(target));
    }
    @SubscribeEvent public static void flush(ServerTickEvent.Post event) {
        // A volley can change the same entity many times; only its final counts need broadcasting.
        for (var target : DIRTY) if (!target.isRemoved()) {
            var packet = snapshot(target); var state = StatusEffects.state(target);
            int[] counts = new int[DamageType.values().length];
            packet.counts().forEach((type, count) -> counts[type.ordinal()] = count);
            if (!Arrays.equals(state.markerCounts, counts)) {
                state.markerCounts = counts;
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, packet);
            }
        }
        DIRTY.clear();
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { DIRTY.clear(); }
}
