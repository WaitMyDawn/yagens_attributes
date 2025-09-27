package yagen.waitmydawn.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.network.DamageNumberPacket;
import yagen.waitmydawn.registries.DamageTypeRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class NourishEffect extends MobEffect {
    public NourishEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    public static float NOURISH_NEED = 1024f;

    private static final class Nourish {
        double enhance = 1.05;
        float nourishCount = 0f;

        Nourish(double enhance, float nourishCount) {
            this.enhance = enhance;
            this.nourishCount = nourishCount;
        }
    }

    public static final Map<LivingEntity, Nourish> NOURISH_MAP = new WeakHashMap<>();
//    public static final Map<LivingEntity, Integer> NOURISH_TIME_MAP = new WeakHashMap<>();
//
//    public static void setNourishTime(LivingEntity entity, int ticks) {
//        NOURISH_TIME_MAP.put(entity, ticks);
//    }

    public static void addNourishCount(LivingEntity entity, float nourishCount) {
        double enhance = Math.min(1.05 + 0.45 * nourishCount / NOURISH_NEED, 1.5);
        NOURISH_MAP.put(entity, new Nourish(enhance, nourishCount));
    }

    public static double getNourishEnhance(LivingEntity entity) {
        Nourish n = NOURISH_MAP.get(entity);
        return n == null ? 1.05 : n.enhance;
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
//        if (NOURISH_TIME_MAP.get(pLivingEntity) > 0) {
//            setNourishTime(pLivingEntity, NOURISH_TIME_MAP.get(pLivingEntity) - 1);
//        } else {
//            NOURISH_MAP.remove(pLivingEntity);
//            pLivingEntity.sendSystemMessage(Component.literal("Cancelled"));
//        }
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
