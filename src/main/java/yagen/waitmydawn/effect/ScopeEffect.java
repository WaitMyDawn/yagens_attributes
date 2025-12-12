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

public class ScopeEffect extends MobEffect {

    public ScopeEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                YAttributes.CRITICAL_CHANCE,
                ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_effect"),
                0.135,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.removeEffect(MobEffectRegistry.SCOPE_GALVANIZED);
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

}
