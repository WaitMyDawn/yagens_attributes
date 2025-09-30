package yagen.waitmydawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import yagen.waitmydawn.registries.BlockRegistry;

public class ModOperationTableEntity extends BlockEntity {
    public float yaw;
    public float prevYaw;
    public float pitch;
    public float prevPitch;

    public float offsetY;
    public float prevOffsetY;

    public float approach;
    public float prevApproach;

    public float playerDirX;
    public float playerDirZ;
    public float prevPlayerDirX;
    public float prevPlayerDirZ;

    private float floatTime;
    private static final float FLOAT_SPEED = 0.05f;
    private static final float FLOAT_AMPLITUDE = 0.1f;

    public ModOperationTableEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.MOD_OPERATION_TABLE_ENTITY.get(), pos, state);
        System.out.println("BlockEntity created at " + pos);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ModOperationTableEntity entity) {
        entity.prevYaw = entity.yaw;
        entity.prevPitch = entity.pitch;
        entity.prevOffsetY = entity.offsetY;
        entity.prevApproach = entity.approach;
        entity.prevPlayerDirX = entity.playerDirX;
        entity.prevPlayerDirZ = entity.playerDirZ;

        entity.floatTime += FLOAT_SPEED;
        float targetBaseOffsetY = Mth.sin(entity.floatTime) * FLOAT_AMPLITUDE;

        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, false);

        float targetDirX = 0;
        float targetDirZ = 0;
        float targetApproach = 0;

        if (player != null) {
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);

            float targetYaw = (float) Math.atan2(dz, dx);

            entity.yaw = smoothRotate(entity.yaw, targetYaw, 0.1f);

            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                targetDirX = (float) (dx / distance);
                targetDirZ = (float) (dz / distance);
            }

            targetApproach = (float) Mth.clamp(1.0 - (distance / 5.0), 0.0, 0.8);

            targetBaseOffsetY *= 0.3f;

        } else {
            entity.yaw = smoothRotate(entity.yaw, 0, 0.05f);
            entity.pitch = smoothRotate(entity.pitch, 0, 0.05f);
            entity.approach = Mth.lerp(0.1f, entity.approach, 0);
        }

        entity.playerDirX = smoothDirection(entity.playerDirX, targetDirX, 0.2f);
        entity.playerDirZ = smoothDirection(entity.playerDirZ, targetDirZ, 0.2f);

        entity.approach += (targetApproach - entity.approach) * 0.1f;

        entity.offsetY = targetBaseOffsetY;
    }

    public static float smoothDirection(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public static float smoothRotate(float current, float target, float speed) {
        float delta = target - current;
        while (delta >= Math.PI) delta -= 2 * Math.PI;
        while (delta < -Math.PI) delta += 2 * Math.PI;
        return current + delta * speed;
    }
}
