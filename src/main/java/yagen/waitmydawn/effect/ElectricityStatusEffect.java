package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static yagen.waitmydawn.api.util.DamageCompat.getDamageAfterAbsorbPure;

public class ElectricityStatusEffect extends MobEffect {
    public ElectricityStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final class Electricity {
        final float damage;
        int ticksLeft;
        final LivingEntity sourceEntity;

        Electricity(float damage, int ticksLeft, LivingEntity sourceEntity) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
            this.sourceEntity = sourceEntity;
        }
    }
    
    private static final Map<LivingEntity, List<Electricity>> ELECTRICITY_MAP = new WeakHashMap<>();

    public static void addElectricity(LivingEntity entity, float damage, int ticksLeft, LivingEntity sourceEntity) {
        ELECTRICITY_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Electricity(damage, ticksLeft, sourceEntity));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;
        List<Electricity> electricity = ELECTRICITY_MAP.get(pLivingEntity);
        if (electricity == null || electricity.isEmpty()) return true;

        Iterator<Electricity> it = electricity.iterator();
        while (it.hasNext()) {
            Electricity c = it.next();
            c.ticksLeft--;
            
            if (c.ticksLeft % 20 == 0) {
                List<LivingEntity> nearby = pLivingEntity.level()
                        .getEntitiesOfClass(LivingEntity.class,
                                pLivingEntity.getBoundingBox().inflate(3.0));
                for (LivingEntity target : nearby) {
                    if(target instanceof Player) continue;
                    target.hurt(target.damageSources().generic(), getDamageAfterAbsorbPure(c.damage, (float) pLivingEntity.getArmorValue(), (float) pLivingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS), c.sourceEntity));
                    target.invulnerableTime = 0;
                }
            }

            if (c.ticksLeft <= 0) {
                it.remove();
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
