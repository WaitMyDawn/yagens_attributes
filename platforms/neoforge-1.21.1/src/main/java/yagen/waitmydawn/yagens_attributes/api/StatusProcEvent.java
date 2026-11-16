package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.List;

/** Published after a hit spends health or absorption; repeated types represent independent procs. */
public final class StatusProcEvent extends Event {
    private final ServerPlayer attacker;
    private final LivingEntity target;
    private final List<DamageType> procs;
    private final int durationTicks;
    private final StatusContext context;

    public StatusProcEvent(ServerPlayer attacker, LivingEntity target, List<DamageType> procs, int durationTicks) {
        this(attacker, target, procs, durationTicks, StatusContext.EMPTY);
    }
    public StatusProcEvent(ServerPlayer attacker, LivingEntity target, List<DamageType> procs, int durationTicks, StatusContext context) {
        this.attacker = attacker; this.target = target; this.procs = List.copyOf(procs); this.durationTicks = durationTicks;
        this.context = context;
    }
    public ServerPlayer attacker() { return attacker; }
    public LivingEntity target() { return target; }
    public List<DamageType> procs() { return procs; }
    public int durationTicks() { return durationTicks; }
    public StatusContext context() { return context; }
}
