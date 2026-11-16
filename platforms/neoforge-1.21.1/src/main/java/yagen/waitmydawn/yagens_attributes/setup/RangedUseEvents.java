package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.RangedStats;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.core.StatusRules;
import yagen.waitmydawn.yagens_attributes.network.ClientPackets;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class RangedUseEvents {
    private record Use(ItemStack item, RangedStats.ChargeClock clock, double rate) {}
    private static final ThreadLocal<Map<LivingEntity, Use>> USES = ThreadLocal.withInitial(WeakHashMap::new);
    private RangedUseEvents() {}
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void start(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity().level().isClientSide()) { ClientPackets.useStart(event); return; }
        USES.get().remove(event.getEntity());
        var shot = RangedShooting.snapshot(event.getEntity(), event.getItem());
        if (shot != null) {
            double rate = shot.fireRate() * (1 - StatusRules.coldSlow(StatusEffects.count(event.getEntity(), DamageType.COLD), true));
            USES.get().put(event.getEntity(), new Use(event.getItem(), new RangedStats.ChargeClock(rate), rate));
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void tick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity().level().isClientSide()) { ClientPackets.useTick(event); return; }
        var use = USES.get().get(event.getEntity());
        if (use != null && use.item() == event.getItem()) event.setDuration(use.clock().beforeTick(event.getDuration()));
    }
    @SubscribeEvent public static void stop(LivingEntityUseItemEvent.Stop event) { clear(event.getEntity()); }
    @SubscribeEvent public static void finish(LivingEntityUseItemEvent.Finish event) { clear(event.getEntity()); }
    private static void clear(LivingEntity entity) {
        if (entity.level().isClientSide()) ClientPackets.useStop(entity); else USES.get().remove(entity);
    }
    public static double activeRate(LivingEntity entity, ItemStack item, double fallback) {
        var use = USES.get().get(entity); return use != null && use.item() == item ? use.rate() : fallback;
    }
    @SubscribeEvent public static void loose(ArrowLooseEvent event) {
        var snapshot = RangedShooting.snapshot(event.getEntity(), event.getBow());
        if (snapshot != null && snapshot.multishot() == 0) event.setCanceled(true);
    }
}
