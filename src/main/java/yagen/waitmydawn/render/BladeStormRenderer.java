package yagen.waitmydawn.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import yagen.waitmydawn.entity.BladeEntity;
import yagen.waitmydawn.util.ServerTasks;

public class BladeStormRenderer {
    public static void spawnDaggerAndStab(ServerPlayer player, LivingEntity target) {
        ServerLevel level = player.serverLevel();
        Vec3 start = target.position()
                .add(0, target.getBbHeight() * 1.5, 0)
                .add(target.getLookAngle().scale(-1.5));
        Vec3 end   = target.getBoundingBox().getCenter();
        Vec3 vel   = end.subtract(start).normalize();

        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                start.x, start.y, start.z,
                10,
                vel.x, vel.y, vel.z,
                0.1);

        BladeEntity dagger = new BladeEntity(level, player);
        dagger.moveTo(start.x, start.y, start.z);
        dagger.shoot(vel.x, vel.y, vel.z, 0.15F, 0);
        level.addFreshEntity(dagger);


//        int flightTicks = (int) (start.distanceTo(end) * 5);
//        ServerTasks.runLater(level, flightTicks, () -> {
//            if (target.isAlive()) {
//                player.attack(target);
//            }
//        });
    }
}
