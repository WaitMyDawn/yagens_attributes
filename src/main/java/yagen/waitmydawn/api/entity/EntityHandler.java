package yagen.waitmydawn.api.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.entity.EnergyOrbEntity;

public class EntityHandler {
    public static void spawnEnergyOrb(Level level, Vec3 pos, int value) {
        if (level.isClientSide) return;
        EnergyOrbEntity orb = new EnergyOrbEntity(level, pos.x, pos.y, pos.z, value);
        orb.pickupDelay = 10;
        level.addFreshEntity(orb);
    }
}
