package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import yagen.waitmydawn.yagens_attributes.compat.NativeAttacks;

@Mixin(ItemCooldowns.class)
public abstract class NativeCooldownMixin {
    @ModifyVariable(method = "addCooldown", at = @At("HEAD"), argsOnly = true)
    private int yagens$cooldown(int duration, Item item, int originalDuration) { return NativeAttacks.cooldown(item, duration); }
}
