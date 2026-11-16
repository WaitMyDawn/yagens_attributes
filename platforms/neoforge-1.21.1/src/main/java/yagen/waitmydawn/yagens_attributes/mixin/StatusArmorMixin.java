package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.setup.StatusDamage;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.ArmorRules;

/** Nonlinear modes evaluate independent layers separately; linear mitigation evaluates the batch once. */
@Mixin(LivingEntity.class)
public abstract class StatusArmorMixin {
    @WrapOperation(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private float yagens$layers(LivingEntity target, float damage, DamageSource source, float armor, float toughness, Operation<Float> original) {
        if (ArmorRules.Mode.parse(ServerConfigs.ARMOR_MODE.get()) == ArmorRules.Mode.LINEAR || !(source instanceof StatusDamage status) || status.portionCount() < 2) return original.call(target, damage, source, armor, toughness);
        double sum = 0;
        for (int i = 0; i < status.portionCount(); i++) sum += status.portion(i);
        if (sum <= 0) return original.call(target, damage, source, armor, toughness);
        double result = 0;
        for (int i = 0; i < status.portionCount(); i++) result += original.call(target, (float) (damage * status.portion(i) / sum), source, armor, toughness);
        return (float) Math.min(Float.MAX_VALUE, result);
    }
}
