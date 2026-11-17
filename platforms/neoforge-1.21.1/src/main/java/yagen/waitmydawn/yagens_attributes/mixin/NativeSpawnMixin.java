package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.compat.NativeAttacks;

@Mixin(ServerLevel.class)
public abstract class NativeSpawnMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void yagens$emit(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (NativeAttacks.spawning((ServerLevel) (Object) this, entity)) callback.setReturnValue(true);
    }
    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void yagens$source(Entity entity, Operation<Void> original) { NativeAttacks.tick(entity, () -> original.call(entity)); }
}
