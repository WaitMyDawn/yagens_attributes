package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;

import java.util.UUID;

/** A direct weak point hit that spent health or absorption. */
public final class WeakPointHitEvent extends Event {
    private final ServerPlayer attacker;
    private final LivingEntity target;
    private final DamageSource source;
    private final String region;
    private final Vec3 location;
    private final float damage;
    private final UUID weapon;
    private final AttackKind attackKind;

    public WeakPointHitEvent(ServerPlayer attacker, LivingEntity target, DamageSource source, String region, Vec3 location,
                             float damage, UUID weapon, AttackKind attackKind) {
        this.attacker = attacker;
        this.target = target;
        this.source = source;
        this.region = region;
        this.location = location;
        this.damage = damage;
        this.weapon = weapon;
        this.attackKind = attackKind;
    }

    public ServerPlayer attacker() { return attacker; }
    public LivingEntity target() { return target; }
    public DamageSource source() { return source; }
    public String region() { return region; }
    public Vec3 location() { return location; }
    public float damage() { return damage; }
    public UUID weapon() { return weapon; }
    public AttackKind attackKind() { return attackKind; }
}
