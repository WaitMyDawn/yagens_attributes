package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.data.MeleeEnchantments;

@Mixin(Player.class)
public abstract class MeleeEnchantmentAttackMixin {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getEnchantedDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)F"))
    private float yagens$measureEnchantment(Player player, Entity target, float damage, DamageSource source, Operation<Float> original,
                                            @Share("meleeEnchantment") LocalRef<MeleeEnchantments.Bonus> addition) {
        addition.set(null);
        var stack = player.getWeaponItem();
        if (!(player instanceof ServerPlayer) || !ServerConfigs.ENABLE_COMBAT.get() || !stack.isEnchanted() || !MeleeEnchantments.present(stack))
            return original.call(player, target, damage, source);
        return MeleeEnchantments.measure(target, source, () -> original.call(player, target, damage, source), addition::set);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"))
    private float yagens$captureCharge(Player player, float partialTick, Operation<Float> original,
                                      @Share("meleeAttackStrength") LocalFloatRef strength) {
        float value = original.call(player, partialTick);
        strength.set(value);
        return value;
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean yagens$primaryImpact(Entity target, DamageSource source, float damage, Operation<Boolean> original,
                                         @Share("meleeEnchantment") LocalRef<MeleeEnchantments.Bonus> addition,
                                         @Share("meleeAttackStrength") LocalFloatRef strength) {
        if (addition.get() == null) return original.call(target, source, damage);
        return MeleeEnchantments.duringImpact(addition.get(), target, source, damage, strength.get(),
                () -> original.call(target, source, damage));
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean yagens$sweepImpact(LivingEntity target, DamageSource source, float damage, Operation<Boolean> original,
                                       @Share("meleeEnchantment") LocalRef<MeleeEnchantments.Bonus> addition,
                                       @Share("meleeAttackStrength") LocalFloatRef strength) {
        if (addition.get() == null) return original.call(target, source, damage);
        return MeleeEnchantments.duringImpact(addition.get(), target, source, damage, strength.get(),
                () -> original.call(target, source, damage));
    }
}
