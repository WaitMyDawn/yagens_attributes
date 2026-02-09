package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.Map;
import java.util.WeakHashMap;

public class NourishEffect extends MobEffect {
    public NourishEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final class Nourish {
        double enhance = ServerConfigs.MOD_WARFRAME_NOURISH_INIT_ENHANCE.get();
        float nourishCount = 0f;

        Nourish(double enhance, float nourishCount) {
            this.enhance = enhance;
            this.nourishCount = nourishCount;
        }
    }

    public static final Map<LivingEntity, Nourish> NOURISH_MAP = new WeakHashMap<>();

    public static void addNourishCount(LivingEntity entity, float nourishCount) {
        double enhance = Math.min(ServerConfigs.MOD_WARFRAME_NOURISH_INIT_ENHANCE.get() +
                (ServerConfigs.MOD_WARFRAME_NOURISH_MAX_ENHANCE.get() - ServerConfigs.MOD_WARFRAME_NOURISH_INIT_ENHANCE.get())
                        * nourishCount / ServerConfigs.MOD_WARFRAME_NOURISH_MAX_COUNT.get(), ServerConfigs.MOD_WARFRAME_NOURISH_MAX_ENHANCE.get());
        NOURISH_MAP.put(entity, new Nourish(enhance, nourishCount));
    }

    public static double getNourishEnhance(LivingEntity entity) {
        Nourish n = NOURISH_MAP.get(entity);
        return n == null ? ServerConfigs.MOD_WARFRAME_NOURISH_INIT_ENHANCE.get() : n.enhance;
    }

    public static float getNourishCount(LivingEntity entity) {
        Nourish n = NOURISH_MAP.get(entity);
        return n == null ? 0 : n.nourishCount;
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;
        int left = pLivingEntity.getPersistentData().getInt("YANourishLeft");
        if (left <= 1) {
            NOURISH_MAP.remove(pLivingEntity);
        } else
            pLivingEntity.getPersistentData().putInt("YANourishLeft", left - 1);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

}
