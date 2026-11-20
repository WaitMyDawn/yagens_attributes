package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.List;

/** Published after a hit spends health or absorption; repeated types represent independent procs. */
public final class StatusProcEvent extends Event {
    public record Application(List<DamageType> types, StatusContext context) {
        public Application { types = List.copyOf(types); }
    }

    private final ServerPlayer attacker;
    private final LivingEntity target;
    private final List<DamageType> procs;
    private final int durationTicks;
    private final List<Application> applications;

    public StatusProcEvent(ServerPlayer attacker, LivingEntity target, List<DamageType> procs, int durationTicks) {
        this(attacker, target, procs, durationTicks, StatusContext.EMPTY);
    }
    public StatusProcEvent(ServerPlayer attacker, LivingEntity target, List<DamageType> procs, int durationTicks, StatusContext context) {
        this(attacker, target, durationTicks, List.of(new Application(procs, context)));
    }
    public StatusProcEvent(ServerPlayer attacker, LivingEntity target, int durationTicks, List<Application> applications) {
        this.attacker = attacker; this.target = target; this.durationTicks = durationTicks;
        this.applications = List.copyOf(applications);
        this.procs = this.applications.stream().flatMap(application -> application.types().stream()).toList();
    }
    public ServerPlayer attacker() { return attacker; }
    public LivingEntity target() { return target; }
    public List<DamageType> procs() { return procs; }
    public int durationTicks() { return durationTicks; }
    /** The first application's context. Batch consumers must read each application's context. */
    public StatusContext context() { return applications.isEmpty() ? StatusContext.EMPTY : applications.getFirst().context(); }
    public List<Application> applications() { return applications; }
}
