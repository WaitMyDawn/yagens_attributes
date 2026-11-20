package yagen.waitmydawn.yagens_attributes.api;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.data.CombatData;
import yagen.waitmydawn.yagens_attributes.data.ShotData;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

/** Entry point for server-side attacks delivered by other mods after the originating weapon may have moved. */
public final class WeaponAttackApi {
    private static final ThreadLocal<Impact> ACTIVE = new ThreadLocal<>();

    private WeaponAttackApi() {}

    public static final class Snapshot {
        private final ShotData shot;
        private final List<DamageType> forcedProcs;

        private Snapshot(ShotData shot, List<DamageType> forcedProcs) {
            this.shot = shot;
            this.forcedProcs = forcedProcs;
        }
    }

    public record Impact(DamageSource source, Entity target, double multiplier, int hits, int portions, ShotData shot,
                         List<DamageType> forcedProcs) {}

    /** Capture before removing or damaging the weapon. Null permits the caller's native attack path. */
    public static Snapshot capture(ServerPlayer player, ItemStack weapon, ResourceLocation attackId) {
        requireServerThread(player);
        if (weapon.isEmpty()) return null;
        var attack = CombatData.WEAPONS.attack(BuiltInRegistries.ITEM.getKey(weapon.getItem()), attackId);
        if (attack == null) return null;
        var shot = RangedShooting.snapshot(player, weapon, attack.kind(), attack.profile());
        return shot == null ? null : new Snapshot(shot, attack.forcedProcs());
    }

    /** The move multiplier, including geometric falloff, scales damage after weapon Mods and before partitioning. */
    public static boolean hurt(Snapshot snapshot, Entity target, DamageSource source, double baseDamage, double multiplier, int hits, int portions) {
        double rawDamage = baseDamage * multiplier;
        if (hits < 1 || portions < hits || portions > 64 || !Double.isFinite(baseDamage) || baseDamage < 0
                || !Double.isFinite(multiplier) || multiplier < 0 || !Double.isFinite(rawDamage) || rawDamage > Float.MAX_VALUE)
            throw new IllegalArgumentException("Invalid attack damage or portion count");
        float damage = (float) (rawDamage / portions) * hits;
        if (snapshot == null || damage <= 0 || !(target instanceof LivingEntity) || target.level().isClientSide()
                || !(source.getEntity() instanceof ServerPlayer player) || !player.getUUID().equals(snapshot.shot.owner())) {
            return target.hurt(source, damage);
        }
        requireServerThread(player);
        var previous = ACTIVE.get();
        ACTIVE.set(new Impact(source, target, multiplier, hits, portions, snapshot.shot, snapshot.forcedProcs));
        try {
            return target.hurt(source, damage);
        } finally {
            if (previous == null) ACTIVE.remove();
            else ACTIVE.set(previous);
        }
    }

    public static Impact impact(DamageSource source, Entity target) {
        var impact = ACTIVE.get();
        return impact != null && impact.source() == source && impact.target() == target ? impact : null;
    }

    public static ShotData scopedShot(DamageSource source) {
        var impact = ACTIVE.get();
        return impact != null && impact.source() == source ? impact.shot() : null;
    }

    private static void requireServerThread(ServerPlayer player) {
        if (!player.server.isSameThread()) throw new IllegalStateException("Weapon attack API requires the logical server thread");
    }
}
