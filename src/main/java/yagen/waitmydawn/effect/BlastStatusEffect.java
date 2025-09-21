package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BlastStatusEffect extends MobEffect {
    public BlastStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
        pLivingEntity.level().explode(
                null,
                pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(),
                pAmplifier,
                Level.ExplosionInteraction.NONE
        );
    }
    @Override
    public boolean isInstantenous() {
        return true;
    }
}
