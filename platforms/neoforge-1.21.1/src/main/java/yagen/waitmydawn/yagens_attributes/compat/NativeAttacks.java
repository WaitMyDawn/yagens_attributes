package yagen.waitmydawn.yagens_attributes.compat;

import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.ShotData;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;
import java.util.function.*;

/** Scoped provenance around explicit native weapon actions; no held-item guessing at later impact. */
public final class NativeAttacks {
    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> FLUSHING = ThreadLocal.withInitial(() -> 0);
    private static final List<Class<? extends Entity>> EFFECTS = new ArrayList<>();
    private static final ThreadLocal<Map<LivingEntity, Cadence>> CADENCES = ThreadLocal.withInitial(WeakHashMap::new);
    private record Frame(ShotData shot, NativeWeapons.Adapter adapter, List<Projectile> projectiles, double rate, Item item) {}
    private static final class Cadence {
        ItemStack item; long tick; RangedStats.IntervalClock clock = new RangedStats.IntervalClock();
        Cadence(ItemStack item, long tick) { this.item = item; this.tick = tick; }
    }
    private NativeAttacks() {}
    public static void effect(Class<? extends Entity> type) { EFFECTS.add(type); }

    public static <T> T run(ItemStack stack, LivingEntity actor, Supplier<T> action) {
        var adapter = NativeWeapons.adapter(stack);
        if (!(actor instanceof ServerPlayer player) || adapter == null || !adapter.capture()) return action.get();
        return run(stack, player, adapter.kind(), action);
    }
    public static <T> T run(ItemStack stack, ServerPlayer player, AttackKind kind, Supplier<T> action) {
        var adapter = NativeWeapons.adapter(stack);
        if (adapter == null || !adapter.capture()) return action.get();
        var shot = RangedShooting.snapshot(player, stack, kind);
        if (shot == null) return action.get();
        return run(stack, player, adapter, shot, action);
    }
    private static <T> T run(ItemStack stack, ServerPlayer player, NativeWeapons.Adapter adapter, ShotData shot, Supplier<T> action) {
        var frame = new Frame(shot, adapter, new ArrayList<>(), RangedUseEvents.activeRate(player, stack, shot.fireRate()), stack.getItem());
        var frames = FRAMES.get(); frames.push(frame);
        T result;
        try { result = action.get(); }
        finally { frames.pop(); }
        flush(player.serverLevel(), frame);
        return result;
    }

    public static void useTick(ItemStack stack, LivingEntity actor, int count, IntConsumer action) {
        var adapter = NativeWeapons.adapter(stack);
        if (!(actor instanceof ServerPlayer player) || adapter == null || adapter.period() == 0) {
            run(stack, actor, () -> { action.accept(count); return null; }); return;
        }
        var shot = RangedShooting.snapshot(player, stack, adapter.kind());
        if (shot == null) { action.accept(count); return; }
        long now = actor.level().getGameTime();
        var cadence = CADENCES.get().get(actor);
        if (cadence == null || cadence.item != stack || now - cadence.tick > 1) {
            cadence = new Cadence(stack, now - 1); CADENCES.get().put(actor, cadence);
        }
        if (cadence.tick == now) return;
        cadence.tick = now;
        int calls = cadence.clock.advance(RangedUseEvents.activeRate(actor, stack, shot.fireRate()), adapter.period());
        int phase = count - Math.floorMod(count, adapter.period());
        if (calls == 0) run(stack, player, adapter, shot, () -> { action.accept(phase + 1); return null; });
        else for (int i = 0; i < calls && actor.isUsingItem(); i++)
            run(stack, player, adapter, shot, () -> { action.accept(phase); return null; });
    }

    public static ShotData source(DamageSource source) {
        var frames = FRAMES.get();
        if (frames.isEmpty() || source.getEntity() == null) return null;
        var shot = frames.peek().shot();
        return source.getEntity().getUUID().equals(shot.owner()) ? shot : null;
    }

    /** Returns true only when native insertion is deferred; the caller still observes a successful emission. */
    public static boolean spawning(ServerLevel level, Entity entity) {
        var frames = FRAMES.get();
        if (frames.isEmpty() || FLUSHING.get() != 0 || !(entity instanceof Projectile) && EFFECTS.stream().noneMatch(type -> type.isInstance(entity))) return false;
        if (entity.hasData(RangedAttachments.SHOT.get())) return false;
        var frame = frames.peek();
        if (entity instanceof Projectile projectile && (projectile.getOwner() == null
                || !projectile.getOwner().getUUID().equals(frame.shot().owner()))) return false;
        if (frame.adapter() != null && frame.adapter().volley() && frame.shot().attackKind() == AttackKind.RANGED
                && entity instanceof Projectile projectile) {
            frame.projectiles().add(projectile); return true;
        }
        entity.setData(RangedAttachments.SHOT.get(), frame.shot());
        return false;
    }

    private static void flush(ServerLevel level, Frame frame) {
        if (frame.projectiles().isEmpty()) return;
        ShotData shot = frame.shot();
        if (frame.adapter().countBaseline() > 0)
            shot = shot.baseMultishot(shot.profile().multishot() * frame.projectiles().size() / frame.adapter().countBaseline());
        var volley = RangedStats.volley(shot.multishot(), level.getRandom()::nextDouble);
        if (volley.count() == 0) return;
        var original = frame.projectiles().get(frame.projectiles().size() / 2);
        FLUSHING.set(FLUSHING.get() + 1);
        try { RangedShooting.spawn(level, original, shot, volley); }
        finally { FLUSHING.set(FLUSHING.get() - 1); }
    }

    public static void tick(Entity entity, Runnable action) {
        var shot = RangedShooting.shot(entity);
        if (shot == null || shot.profile() == null || shot.owner() == null) { action.run(); return; }
        var frames = FRAMES.get(); frames.push(new Frame(shot, null, List.of(), 1, null));
        try { action.run(); } finally { frames.pop(); }
    }
    public static int cooldown(Item item, int ticks) {
        if (ticks <= 0) return ticks;
        var frames = FRAMES.get();
        if (frames.isEmpty() || frames.peek().adapter() == null || frames.peek().item() != item || frames.peek().rate() <= 0) return ticks;
        return Math.max(1, (int) Math.min(Integer.MAX_VALUE, Math.ceil(ticks / frames.peek().rate())));
    }
    public static void clear(LivingEntity actor) { CADENCES.get().remove(actor); }
}
