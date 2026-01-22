package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.network.BlastPacket;

import java.util.Map;
import java.util.WeakHashMap;

public class BlastStatusEffect extends MobEffect {
    public BlastStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    private static final Map<LivingEntity, LivingEntity> BLAST_MAP = new WeakHashMap<>();

    public static void addBlast(LivingEntity entity, LivingEntity sourceEntity) {
        BLAST_MAP.put(entity, sourceEntity);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;

        Vec3 center = pLivingEntity.position().add(0, pLivingEntity.getBbHeight() / 2, 0);
        pLivingEntity.level().explode(
                BLAST_MAP.get(pLivingEntity),
                center.x,
                center.y,
                center.z,
                pAmplifier,
                Level.ExplosionInteraction.NONE
        );
        PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity,
                new BlastPacket(center, pAmplifier));
        BLAST_MAP.remove(BLAST_MAP.get(pLivingEntity));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return pDuration <= 1 || pDuration % 20 == 0;
    }

}
