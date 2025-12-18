package yagen.waitmydawn.effect;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.util.DamageCompat;
import yagen.waitmydawn.network.DamageNumberPacket;
import yagen.waitmydawn.network.ElectricityPacket;
import yagen.waitmydawn.registries.DamageTypeRegistry;

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
                    if (target instanceof Player) continue;
                    PacketDistributor.sendToPlayersTrackingEntity(target,
                            new ElectricityPacket(
                                    pLivingEntity.position().add(0,
                                            Mth.nextDouble(pLivingEntity.getRandom(), pLivingEntity.getEyeHeight() / 3,
                                                    pLivingEntity.getEyeHeight()), 0),
                                    target.position().add(0,
                                            Mth.nextDouble(target.getRandom(), target.getEyeHeight() / 3,
                                                    target.getEyeHeight()), 0)));
                    target.hurt(DamageCompat.getDamageSource(DamageTypeRegistry.ELECTRICITY_STATUS_DAMAGE_TYPE, c.sourceEntity), c.damage);
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
