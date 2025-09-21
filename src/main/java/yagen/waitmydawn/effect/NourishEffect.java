package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.attribute.YAttributes;

import java.util.Map;
import java.util.WeakHashMap;

public class NourishEffect extends MobEffect {

    public NourishEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final Map<LivingEntity, Double> NOURISH_ENHANCE_MAP = new WeakHashMap<>();

    public static void addNourishEnhance(LivingEntity entity, double enhence) {
//        if(!(entity instanceof Player)) return;
//        double strength = entity.getAttribute(YAttributes.).getValue();
//        NOURISH_ENHANCE_MAP.put(entity, strength * enhence);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

}
