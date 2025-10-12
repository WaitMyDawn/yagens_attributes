package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.util.DamageCompat;
import yagen.waitmydawn.network.DamageNumberPacket;
import yagen.waitmydawn.registries.DamageTypeRegistry;

import java.util.*;

public class SlashStatusEffect extends MobEffect {
    public SlashStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final class Cut {
        final float damage;
        int ticksLeft;
        final LivingEntity sourceEntity;

        Cut(float damage, int ticksLeft, LivingEntity sourceEntity) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
            this.sourceEntity = sourceEntity;
        }
    }

    private static final Map<LivingEntity, List<Cut>> CUT_MAP = new WeakHashMap<>();

    public static void addCut(LivingEntity entity, float damage, int ticksLeft, LivingEntity sourceEntity) {
        CUT_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Cut(damage, ticksLeft, sourceEntity));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;
        List<Cut> cuts = CUT_MAP.get(pLivingEntity);
        if (cuts == null || cuts.isEmpty()) return true;

        Iterator<Cut> it = cuts.iterator();
        while (it.hasNext()) {
            Cut c = it.next();
            c.ticksLeft--;

            if (c.ticksLeft % 20 == 0) {
                pLivingEntity.hurt(DamageCompat.getDamageSource(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE,c.sourceEntity), c.damage);
                pLivingEntity.invulnerableTime = 0;
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
