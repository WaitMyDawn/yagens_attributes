package yagen.waitmydawn.api.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.api.attribute.YAttributes;

public class DamageCompat {
    public static float getDamageAfterAbsorbPure(float damage, float armor, float toughness, LivingEntity sourceEntity) {
        if (sourceEntity instanceof Player player) {
            armor = Math.max(0, armor * (1 -
                    (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_PENETRATION_PERCENT.get())).getValue())
                    - (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ARMOR_PENETRATION.get())).getValue());
            toughness = Math.max(0, toughness * (1 -
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

        float finalFactor = 1 - afterDamageEffectiveness;
        return finalFactor * damage;
    }

    public static double linearGrowth(double x, double x1, double x2, double y1, double y2) {
        return y1 + (y2 - y1) / (x2 - x1) * (x - x1);
    }

    public static double logGrowth(double damage, double startDamage,
                                   double startSize, double maxSize,
                                   double scaleFactor) {

        double excessDamage = damage - startDamage;
        double progress = 1 - Math.exp(-excessDamage / scaleFactor);
        return startSize + (maxSize - startSize) * progress;
    }
}
