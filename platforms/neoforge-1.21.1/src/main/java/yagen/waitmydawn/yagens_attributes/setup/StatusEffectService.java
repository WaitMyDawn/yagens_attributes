package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.StatusEffectPayload;
import java.util.*;

/** Coalesces visual emissions independently of layer count and damage transactions. */
@EventBusSubscriber(modid = "yagens_attributes")
public final class StatusEffectService {
    private record Key(ServerLevel level, UUID entity, DamageType effect) {}
    private static final class Pending {
        final Entity entity; final Vec3 position; final int seed;
        int lifetime; boolean burst;
        final Map<UUID, StatusEffectPayload.Target> targets = new LinkedHashMap<>();
        Pending(Entity entity) { this.entity = entity; position = entity.position().add(0, entity.getBbHeight() * .55, 0); seed = entity.level().getRandom().nextInt(); }
    }
    private static final Map<Key, Pending> QUEUED = new LinkedHashMap<>();
    private StatusEffectService() {}
    public static void emit(Entity host, DamageType type, int lifetime, boolean burst, Collection<? extends LivingEntity> targets) {
        if (!Set.of(DamageType.SLASH, DamageType.ELECTRICITY, DamageType.MAGNETIC, DamageType.BLAST).contains(type)
                || type != DamageType.ELECTRICITY && !targets.isEmpty()) throw new IllegalArgumentException("Unsupported status effect emission");
        if (!(host.level() instanceof ServerLevel level)) return;
        var key = new Key(level, host.getUUID(), type);
        if (!QUEUED.containsKey(key) && QUEUED.size() >= 256) return;
        var pending = QUEUED.computeIfAbsent(key, unused -> new Pending(host));
        pending.lifetime = Math.max(pending.lifetime, Math.max(1, Math.min(72000, lifetime))); pending.burst |= burst;
        targets.stream().sorted(Comparator.comparingDouble(host::distanceToSqr)).forEach(target -> {
            if (pending.targets.size() < 32) pending.targets.putIfAbsent(target.getUUID(), new StatusEffectPayload.Target(target.getId(), target.getUUID()));
        });
    }
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST) public static void flush(ServerTickEvent.Post event) {
        var iterator = QUEUED.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next(); if (entry.getKey().level().getServer() != event.getServer()) continue;
            var pending = entry.getValue(); iterator.remove();
            if (pending.entity.level() != entry.getKey().level()) continue;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(pending.entity, new StatusEffectPayload(pending.entity.getId(), pending.entity.getUUID(), pending.position,
                    entry.getKey().effect(), pending.lifetime, pending.seed, pending.burst, List.copyOf(pending.targets.values())));
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { QUEUED.clear(); }
}
