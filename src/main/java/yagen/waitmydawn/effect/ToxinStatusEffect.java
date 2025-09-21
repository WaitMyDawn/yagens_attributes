package yagen.waitmydawn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.network.DamageNumberPacket;

import java.util.*;

import static yagen.waitmydawn.api.util.DamageCompat.getDamageAfterAbsorbPure;

public class ToxinStatusEffect extends MobEffect {
    public ToxinStatusEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    private static final class Toxin {
        final float damage;
        int ticksLeft;
        final LivingEntity sourceEntity;

        Toxin(float damage, int ticksLeft, LivingEntity sourceEntity) {
            this.damage = damage;
            this.ticksLeft = ticksLeft;
            this.sourceEntity = sourceEntity;
        }
    }

    private static final Map<LivingEntity, List<Toxin>> TOXIN_MAP = new WeakHashMap<>();

    public static void addToxin(LivingEntity entity, float damage, int ticksLeft, LivingEntity sourceEntity) {
        TOXIN_MAP.computeIfAbsent(entity, k -> new ArrayList<>())
                .add(new Toxin(damage, ticksLeft, sourceEntity));
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        List<Toxin> toxins = TOXIN_MAP.get(pLivingEntity);
        if (toxins == null || toxins.isEmpty()) return true;

        Iterator<Toxin> it = toxins.iterator();
        while (it.hasNext()) {
            Toxin c = it.next();
            c.ticksLeft--;
            if (c.ticksLeft % 20 == 0) {
                pLivingEntity.hurt(pLivingEntity.damageSources().generic(), getDamageAfterAbsorbPure(10, (float) pLivingEntity.getArmorValue(), (float) pLivingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS), c.sourceEntity));

                if(c.sourceEntity instanceof Player){
                    Vec3 pos = pLivingEntity.position().add(0, pLivingEntity.getBbHeight() * 0.7, 0);
                    PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity,
                            new DamageNumberPacket(pos, c.damage, 0x7CFC00, 0));
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
