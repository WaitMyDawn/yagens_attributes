package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.StatusEffects;

/** Pauses both goal-based and Brain-based AI without persisting the entity's NoAI flag. */
@Mixin(Mob.class)
public abstract class StatusControlMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void yagens$stun(CallbackInfo callback) {
        var mob = (Mob) (Object) this;
        if (StatusEffects.controlled(mob)) {
            mob.getNavigation().stop(); mob.setXxa(0); mob.setYya(0); mob.setZza(0); mob.setJumping(false); mob.setSpeed(0);
            callback.cancel();
        }
    }
}
