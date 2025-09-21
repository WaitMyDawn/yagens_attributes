package yagen.waitmydawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import yagen.waitmydawn.registries.BlockRegistry;

public class ModOperationTableEntity extends BlockEntity {
    // 旋转相关
    public float yaw;          // 当前水平朝向 (弧度)
    public float prevYaw;
    public float pitch;        // 当前垂直朝向 (弧度)
    public float prevPitch;

    // 位置偏移相关
    public float offsetY;      // 垂直漂浮偏移
    public float prevOffsetY;

    // 动画状态
    public float approach;     // 接近玩家的程度 0-1
    public float prevApproach;

    public float playerDirX;
    public float playerDirZ;
    public float prevPlayerDirX;
    public float prevPlayerDirZ;

    // 漂浮动画参数
    private float floatTime;   // 用于正弦波计算的时间
    private static final float FLOAT_SPEED = 0.05f;
    private static final float FLOAT_AMPLITUDE = 0.1f;

    public ModOperationTableEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.MOD_OPERATION_TABLE_ENTITY.get(), pos, state);
        System.out.println("BlockEntity created at " + pos);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ModOperationTableEntity entity) {
        // 保存上一帧状态
        entity.prevYaw = entity.yaw;
        entity.prevPitch = entity.pitch;
        entity.prevOffsetY = entity.offsetY;
        entity.prevApproach = entity.approach;
        entity.prevPlayerDirX = entity.playerDirX;
        entity.prevPlayerDirZ = entity.playerDirZ;

        // 基础漂浮动画 - 始终执行
        entity.floatTime += FLOAT_SPEED;
        float targetBaseOffsetY = Mth.sin(entity.floatTime) * FLOAT_AMPLITUDE;

        // 检测5格范围内的玩家
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, false);

        float targetDirX = 0;
        float targetDirZ = 0;
        float targetApproach = 0;

        if (player != null) {
            // 计算玩家相对位置
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);

            // 计算三维目标角度
            float targetYaw = (float) Math.atan2(dz, dx);

            // 平滑旋转到目标角度
            entity.yaw = smoothRotate(entity.yaw, targetYaw, 0.1f);

            // 计算距离并决定接近程度
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                targetDirX = (float)(dx / distance);
                targetDirZ = (float)(dz / distance);
            }

            targetApproach = (float) Mth.clamp(1.0 - (distance / 5.0), 0.0, 0.8);

            // 有玩家时减少漂浮幅度
            targetBaseOffsetY *= 0.3f;

        } else {
            // 没有玩家时回归中性状态
            entity.yaw = smoothRotate(entity.yaw, 0, 0.05f);
            entity.pitch = smoothRotate(entity.pitch, 0, 0.05f);
            entity.approach = Mth.lerp(0.1f, entity.approach, 0);
        }

        // 平滑过渡方向向量
        entity.playerDirX = smoothDirection(entity.playerDirX, targetDirX, 0.2f);
        entity.playerDirZ = smoothDirection(entity.playerDirZ, targetDirZ, 0.2f);

        // 平滑过渡接近程度
        entity.approach += (targetApproach - entity.approach) * 0.1f;

        // 应用最终偏移
        entity.offsetY = targetBaseOffsetY;
    }

    // 新增：平滑方向向量辅助方法
    public static float smoothDirection(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    // 平滑旋转辅助方法
    public static float smoothRotate(float current, float target, float speed) {
        float delta = target - current;
        // 确保最短旋转路径
        while (delta >= Math.PI) delta -= 2 * Math.PI;
        while (delta < -Math.PI) delta += 2 * Math.PI;
        return current + delta * speed;
    }
}
