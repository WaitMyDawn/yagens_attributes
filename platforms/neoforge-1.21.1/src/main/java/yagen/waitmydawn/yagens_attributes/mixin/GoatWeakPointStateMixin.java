package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class GoatWeakPointStateMixin {
    @Inject(method = "broadcastEntityEvent", at = @At("HEAD"))
    private void yagens$mirrorGoatHeadPose(Entity entity, byte event, CallbackInfo info) {
        // Vanilla uses these packets to drive the head angle in aiStep on the client.
        if (entity instanceof Goat goat && (event == 58 || event == 59)) goat.handleEntityEvent(event);
    }
}
