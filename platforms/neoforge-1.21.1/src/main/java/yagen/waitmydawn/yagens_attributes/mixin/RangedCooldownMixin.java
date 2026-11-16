package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;
import yagen.waitmydawn.yagens_attributes.setup.StatusDamage;
import yagen.waitmydawn.yagens_attributes.setup.ShieldService;

/** Independent ranged, secondary and shield hits preserve the cooldown and prior-damage state of other attacks. */
@Mixin(LivingEntity.class)
public abstract class RangedCooldownMixin {
    @WrapOperation(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.GETFIELD))
    private int yagens$readCooldown(LivingEntity entity, Operation<Integer> original, DamageSource source, float amount) {
        return independent(entity, source) ? 0 : original.call(entity);
    }
    @WrapOperation(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD))
    private void yagens$writeCooldown(LivingEntity entity, int value, Operation<Void> original, DamageSource source, float amount) {
        if (!independent(entity, source)) original.call(entity, value);
    }
    @WrapOperation(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F", opcode = Opcodes.PUTFIELD))
    private void yagens$writePriorDamage(LivingEntity entity, float value, Operation<Void> original, DamageSource source, float amount) {
        if (!independent(entity, source)) original.call(entity, value);
    }
    private static boolean independent(LivingEntity entity, DamageSource source) {
        return StatusDamage.isSecondary(source) || RangedShooting.rangedDamage(source)
                || ShieldService.enabled(entity) && (entity.getAbsorptionAmount() > 0 || ShieldService.protectedNow(entity));
    }
}
