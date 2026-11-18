package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.setup.WeaponLifecycle;

@Mixin(ItemEntity.class)
public abstract class DroppedWeaponMixin {
    @Inject(method = "makeFakeItem", at = @At("TAIL"))
    private void yagens$detachVisualDrop(CallbackInfo callback) {
        var entity = (ItemEntity) (Object) this;
        var stack = entity.getItem();
        if (stack.has(Components.WEAPON_ID.get())) {
            // The command's pickup animation can share the delivered item's identity.
            var visual = stack.copy();
            visual.remove(Components.WEAPON_ID.get());
            entity.setItem(visual);
        }
    }

    // The slice starts at the expiry hook so a postponed expiry or an empty pickup cannot retire the weapon.
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V",
            ordinal = 0, shift = At.Shift.AFTER), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;onItemExpire(Lnet/minecraft/world/entity/item/ItemEntity;)I")))
    private void yagens$retireExpiredWeapon(CallbackInfo callback) {
        WeaponLifecycle.retireCarrier((ItemEntity) (Object) this);
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", shift = At.Shift.AFTER))
    private void yagens$retireDestroyedDrop(CallbackInfoReturnable<Boolean> callback) {
        WeaponLifecycle.retireCarrier((ItemEntity) (Object) this);
    }
}
