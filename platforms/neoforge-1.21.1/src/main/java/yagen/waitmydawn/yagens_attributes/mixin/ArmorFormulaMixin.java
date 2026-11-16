package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.ArmorRules;
import yagen.waitmydawn.yagens_attributes.api.ArmorPenetrationEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mixin(value = CombatRules.class, priority = 2000)
public abstract class ArmorFormulaMixin {
    @Inject(method = "getDamageAfterAbsorb", at = @At("HEAD"), cancellable = true)
    private static void yagens$armor(LivingEntity target, float damage, DamageSource source, float armor, float toughness,
                                    CallbackInfoReturnable<Float> callback) {
        var mode = ArmorRules.Mode.parse(ServerConfigs.ARMOR_MODE.get());
        if (mode == ArmorRules.Mode.NONE) return;
        var event = NeoForge.EVENT_BUS.post(new ArmorPenetrationEvent(target, source));
        double effectiveArmor = event.penetration().armor(armor), effectiveToughness = event.penetration().toughness(toughness);
        double remaining = mode == ArmorRules.Mode.IMPROVED_VANILLA ? ArmorRules.improvedRemaining(damage, effectiveArmor, effectiveToughness)
                : ArmorRules.remaining(effectiveArmor, effectiveToughness, ServerConfigs.ARMOR_SCALE.get(), ServerConfigs.TOUGHNESS_SCALE.get());
        var weapon = source.getWeaponItem();
        if (weapon != null && target.level() instanceof ServerLevel level) {
            float reduction = (float) (1 - remaining);
            float modified = EnchantmentHelper.modifyArmorEffectiveness(level, weapon, target, source, reduction);
            // Preserve the unrounded remaining fraction when the native float API makes no change.
            remaining = Math.max(0, Math.min(1, remaining + (reduction - Math.max(0, Math.min(1, modified)))));
        }
        callback.setReturnValue((float) (damage * remaining));
    }
}
