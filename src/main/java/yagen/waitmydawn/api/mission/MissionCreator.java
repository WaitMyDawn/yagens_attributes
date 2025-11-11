package yagen.waitmydawn.api.mission;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class MissionCreator {
    private static final int[] exterminateDistance = {400, 800, 1200};

    public static double getRandMissionDistance(Level level, Player player) {
        // difficulty from player, as a test is 0->400
        return (0.8 + player.getRandom().nextDouble() / 2.5) * exterminateDistance[0];
    }

    public static Vec3 getRandMissionPosition(Level level, Player player, double distance) {
        float yaw = player.getRandom().nextFloat() * Mth.TWO_PI;
        double dx = Mth.sin(yaw) * distance;
        double dz = Mth.cos(yaw) * distance;

        Vec3 center = player.position();
        int x = Mth.floor(center.x + dx);
        int z = Mth.floor(center.z + dz);

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        if (!level.getWorldBorder().isWithinBounds(pos)) {
            pos = new BlockPos(
                    Mth.clamp(x,
                            (int) level.getWorldBorder().getMinX() + 1,
                            (int) level.getWorldBorder().getMaxX() - 1),
                    y,
                    Mth.clamp(z,
                            (int) level.getWorldBorder().getMinZ() + 1,
                            (int) level.getWorldBorder().getMaxZ() - 1)
            );
        }

        return Vec3.atCenterOf(pos);
    }
}
