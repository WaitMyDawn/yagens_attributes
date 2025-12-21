package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.util.DamageCompat;
import yagen.waitmydawn.network.GasPacket;
import yagen.waitmydawn.registries.DamageTypeRegistry;

import java.util.*;

public class GasStatusEffect extends MobEffect {
    public GasStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final class Gas {
        final float damage;
        int ticksLeft;
        final LivingEntity sourceEntity;

        Gas(float damage, int ticksLeft, LivingEntity sourceEntity) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
            this.sourceEntity = sourceEntity;
        }
    }

    private static final Map<LivingEntity, List<Gas>> GAS_MAP = new WeakHashMap<>();

    public static void addGas(LivingEntity entity, float damage, int ticksLeft, LivingEntity sourceEntity) {
        GAS_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Gas(damage, ticksLeft, sourceEntity));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;
        List<Gas> gas = GAS_MAP.get(pLivingEntity);
        if (gas == null || gas.isEmpty()) return true;

        Iterator<Gas> it = gas.iterator();
        while (it.hasNext()) {
            Gas c = it.next();
            c.ticksLeft--;

            if (c.ticksLeft % 20 == 0) {
                pLivingEntity.hurt(DamageCompat.getDamageSource(DamageTypeRegistry.GAS_STATUS_DAMAGE_TYPE,c.sourceEntity), c.damage);
                pLivingEntity.invulnerableTime = 0;
                PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity,
                        new GasPacket(pLivingEntity.position(),
                                pLivingEntity.getBbWidth() / 2,
                                pLivingEntity.getBbHeight() * 0.7));
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
