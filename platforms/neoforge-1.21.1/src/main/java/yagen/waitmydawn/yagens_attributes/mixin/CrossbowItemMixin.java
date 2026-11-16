package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Inject(method = "performShooting", at = @At("HEAD"), cancellable = true)
    private void yagens$emptyVolley(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float velocity, float inaccuracy, LivingEntity target, CallbackInfo callback) {
        var shot = RangedShooting.snapshot(shooter, weapon);
        if (shot != null && shot.multishot() == 0) callback.cancel();
    }
}
