package yagen.waitmydawn.effect;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;


public class MagneticStatusEffect extends MobEffect {
    public MagneticStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        double radius = livingEntity.getBbHeight() * 0.7;
        if (!(livingEntity.level() instanceof ServerLevel serverLevel)) return true;
        spawnMagneticParticles(serverLevel, livingEntity, radius);
        AABB searchBox = livingEntity.getBoundingBox().inflate(radius);
        List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, searchBox);
        for (Projectile projectile : projectiles) {
            if (projectile.onGround() || !projectile.isAlive()) continue;
//            if (projectile.getOwner() == livingEntity) continue;
            Vec3 targetPos = livingEntity.position().add(0, livingEntity.getBbHeight() * 0.5, 0);
            Vec3 projectilePos = projectile.position();
            Vec3 targetDir = targetPos.subtract(projectilePos).normalize();
            Vec3 currentVel = projectile.getDeltaMovement();
            double currentSpeed = currentVel.length();
            if (currentSpeed < 0.0001) {
                projectile.setDeltaMovement(targetDir.scale(0.5));
                return true;
            }
            Vec3 currentDir = currentVel.normalize();
            // percent: 0 - 1
            double turnFactor = Math.min(1.0, 0.25 + amplifier);
            Vec3 newDir = currentDir.lerp(targetDir, turnFactor).normalize();
            double newSpeed = Math.max(currentSpeed, 0.5);
            projectile.setDeltaMovement(newDir.scale(newSpeed));
            updateProjectileRotation(projectile, newDir);
            projectile.hasImpulse = true;
        }

        return true;
    }

    private void updateProjectileRotation(Projectile projectile, Vec3 direction) {
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float) (Math.atan2(direction.x, direction.z) * (180 / Math.PI));
        float xRot = (float) (Math.atan2(direction.y, horizontalDistance) * (180 / Math.PI));
        projectile.setYRot(yRot);
        projectile.setXRot(xRot);
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }

    private void spawnMagneticParticles(ServerLevel level, LivingEntity entity, double radius) {
        double time = entity.tickCount;
        double entityY = entity.getY() + entity.getBbHeight() * 0.5;

        Vector3f colorStart = new Vector3f(0.0f, 1.0f, 0.8f);
        Vector3f colorEnd = new Vector3f(0.5f, 0.0f, 0.8f);
        var particleData = new DustColorTransitionOptions(colorStart, colorEnd, 1.0f);

        int orbitCount = 6;
        for (int i = 0; i < orbitCount; i++) {
            double offset = i * (Math.PI * 2 / orbitCount);

            double theta = (time * 0.4) + offset;
            double phi = (time * 0.1) + offset;

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(particleData,
                    entity.getX() + x,
                    entityY + y,
                    entity.getZ() + z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        if (entity.getRandom().nextFloat() < 0.3f) {
            double u = Math.random();
            double v = Math.random();
            double theta = 2 * Math.PI * u;
            double phi = Math.acos(2 * v - 1);

            double rx = radius * Math.sin(phi) * Math.cos(theta);
            double ry = radius * Math.cos(phi);
            double rz = radius * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(particleData,
                    entity.getX() + rx,
                    entityY + ry,
                    entity.getZ() + rz,
                    1, 0, 0, 0, 0);
        }
    }
}
