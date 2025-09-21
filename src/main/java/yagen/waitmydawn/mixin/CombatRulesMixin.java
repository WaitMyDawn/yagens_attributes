package yagen.waitmydawn.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.registries.DamageTypeRegistry;

@Mixin(value = CombatRules.class, priority = 2000)
public class CombatRulesMixin {
    @Overwrite
    public static float getDamageAfterAbsorb(LivingEntity entity, float damage, DamageSource damageSource, float armor, float toughness) {
//        if(damageSource.is(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE)) {
//            System.out.println("damageSource is " + damageSource);
//            return damage;
//        }

        if (damageSource.getEntity() instanceof Player player) {
            armor = Math.max(0,armor * (1 -
                    (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_PENETRATION_PERCENT.get())).getValue())
                    - (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_PENETRATION.get())).getValue());
            toughness = Math.max(0,toughness * (1 -
                    (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_TOUGHNESS_PENETRATION_PERCENT.get())).getValue())
                    - (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_TOUGHNESS_PENETRATION.get())).getValue());
        }

        float atFactor = armor * (toughness + 4);
        float baseFactor = 40 / (atFactor + 40);
        float effectiveness = 1 - baseFactor;
        float equivalentHealth = 20 / baseFactor;
        float afterDamageEffectiveness = Math.max(effectiveness / 4,
                effectiveness -
                        damage / (equivalentHealth + equivalentHealth / 8 * toughness)
        );
        float afterEnchantmentEffectiveness = afterDamageEffectiveness;

        ItemStack itemstack = damageSource.getWeaponItem();
        if (itemstack != null && entity.level() instanceof ServerLevel serverlevel) {
            afterEnchantmentEffectiveness = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverlevel, itemstack, entity, damageSource, afterDamageEffectiveness), 0.0F, 1.0F);
        }

        float finalFactor = 1 - afterEnchantmentEffectiveness;
        return finalFactor * damage;
    }
}
