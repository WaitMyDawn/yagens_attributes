package yagen.waitmydawn.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.mission.MissionHandler;

import java.util.Comparator;
import java.util.List;

public class RadiationStatusEffect extends MobEffect {
    public RadiationStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!(livingEntity.level() instanceof ServerLevel level)) return true;
        if (MissionHandler.isBoss(livingEntity)) return true;
        if (!(livingEntity instanceof Mob mob)) return true;
        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget == null || currentTarget instanceof Player) {

            double range = 10.0;
            AABB searchBox = mob.getBoundingBox().inflate(range);
            List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchBox, candidate -> {
                return candidate != mob && candidate.isAlive() && !(candidate instanceof Player);
            });

            if (!candidates.isEmpty()) {
                LivingEntity newTarget = candidates.get(mob.getRandom().nextInt(candidates.size()));
                mob.setTarget(newTarget);
                mob.getLookControl().setLookAt(newTarget, 30.0F, 30.0F);
            }
        }
        return true;
    }
}
