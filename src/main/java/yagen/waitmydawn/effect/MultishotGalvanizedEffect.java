package yagen.waitmydawn.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.registries.MobEffectRegistry;

public class MultishotGalvanizedEffect extends MobEffect {

    public MultishotGalvanizedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                YAttributes.MULTISHOT,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "multishot_galvanized_killbonus"),
                0.3,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide()) return true;
        int left = pLivingEntity.getPersistentData().getInt("YAGalvanizedLeft");
        if (left <= 1) {
            if (pAmplifier > 0) {
                pLivingEntity.removeEffect(MobEffectRegistry.MULTISHOT_GALVANIZED);
                pLivingEntity.addEffect(new MobEffectInstance(
                        MobEffectRegistry.MULTISHOT_GALVANIZED,
                        400,
                        --pAmplifier,
                        false,
                        true,
                        true
                ));
            }
            pLivingEntity.getPersistentData().putInt("YAGalvanizedLeft", 400);
        } else
            pLivingEntity.getPersistentData().putInt("YAGalvanizedLeft", left - 1);
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
