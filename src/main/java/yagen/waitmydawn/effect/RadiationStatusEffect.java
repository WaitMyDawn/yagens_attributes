package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class RadiationStatusEffect extends MobEffect {
    public RadiationStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
        if (!(pLivingEntity instanceof Mob mob)) return;
        if(!mob.isAggressive()) return;

        List<LivingEntity> nearby = mob.level()
                .getEntitiesOfClass(LivingEntity.class,
                        mob.getBoundingBox().inflate(3.0),
                        e -> e != mob &&
                                e instanceof Mob mob2 &&
                                //!(e instanceof Player) &&
                                mob2.isAggressive()
                );

        nearby.stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr)).ifPresent(mob::setTarget);

    }

}
