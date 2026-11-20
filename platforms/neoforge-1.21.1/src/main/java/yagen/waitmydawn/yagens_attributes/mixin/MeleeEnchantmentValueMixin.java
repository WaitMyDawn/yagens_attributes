package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import yagen.waitmydawn.yagens_attributes.data.MeleeEnchantments;

@Mixin(Enchantment.class)
public abstract class MeleeEnchantmentValueMixin {
    @WrapMethod(method = "modifyDamage")
    private void yagens$measureAddition(ServerLevel level, int rank, ItemStack stack, Entity target, DamageSource source,
                                        MutableFloat damage, Operation<Void> original) {
        boolean measure = MeleeEnchantments.measures(level, (Enchantment) (Object) this, target, source);
        float before = damage.floatValue();
        original.call(level, rank, stack, target, source, damage);
        if (measure) MeleeEnchantments.record((double) damage.floatValue() - before);
    }
}
