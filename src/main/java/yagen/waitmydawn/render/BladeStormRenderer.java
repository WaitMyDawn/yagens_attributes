package yagen.waitmydawn.render;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import yagen.waitmydawn.entity.BladeEntity;

public class BladeStormRenderer {
    public static void spawnBlade(ServerPlayer player, LivingEntity target) {
        ServerLevel level = player.serverLevel();
        Vec3 start = target.position()
                .add(0, target.getBbHeight() * 1.5, 0)
                .add(target.getLookAngle().scale(-1.5));
        Vec3 end = target.getBoundingBox().getCenter();
        Vec3 vel = end.subtract(start).normalize();

        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                start.x, start.y - target.getBbHeight() * 0.75, start.z,
                20,
                vel.x, vel.y, vel.z,
                0.05);

        BladeEntity blade = new BladeEntity(level, player);
        blade.moveTo(start.x, start.y, start.z);
        blade.shoot(vel.x, vel.y, vel.z, 0.3F, 0);
        level.addFreshEntity(blade);
    }
}
