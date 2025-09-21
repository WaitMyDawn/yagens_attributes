package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;

import java.util.*;

public class HeatStatusEffect extends MobEffect {
    public HeatStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "heat_armor"),
                -0.125,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static final class Heat {
        final float damage;
        int ticksLeft;

        Heat(float damage, int ticksLeft) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
        }
    }

    private static final Map<LivingEntity, List<Heat>> HEAT_MAP = new WeakHashMap<>();

    public static void addHeat(LivingEntity entity, float damage, int ticksLeft) {
        HEAT_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Heat(damage, ticksLeft));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        List<Heat> heats = HEAT_MAP.get(pLivingEntity);
        if (heats == null || heats.isEmpty()) return true;

        Iterator<Heat> it = heats.iterator();
        while (it.hasNext()) {
            Heat c = it.next();
            c.ticksLeft--;
            if (c.ticksLeft % 20 == 0) {
                pLivingEntity.hurt(pLivingEntity.damageSources().lava(), c.damage);
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
