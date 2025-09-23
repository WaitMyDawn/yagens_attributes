package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.network.DamageNumberPacket;

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
        final LivingEntity sourceEntity;

        Heat(float damage, int ticksLeft, LivingEntity sourceEntity) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
            this.sourceEntity = sourceEntity;
        }
    }

    private static final Map<LivingEntity, List<Heat>> HEAT_MAP = new WeakHashMap<>();

    public static void addHeat(LivingEntity entity, float damage, int ticksLeft, LivingEntity sourceEntity) {
        HEAT_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Heat(damage, ticksLeft, sourceEntity));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return true;
        List<Heat> heats = HEAT_MAP.get(pLivingEntity);
        if (heats == null || heats.isEmpty()) return true;

        Iterator<Heat> it = heats.iterator();
        while (it.hasNext()) {
            Heat c = it.next();
            c.ticksLeft--;
            if (c.ticksLeft % 20 == 0) {
                pLivingEntity.hurt(pLivingEntity.damageSources().lava(), c.damage);
                if(c.sourceEntity instanceof Player){
                    Vec3 pos = pLivingEntity.position().add(0, pLivingEntity.getBbHeight() * 0.7, 0);
                    PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity,
                            new DamageNumberPacket(pos, c.damage, 0xFF7518, 0));
                }

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
