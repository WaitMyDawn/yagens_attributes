package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;
import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processProjectileCount(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;I)I"))
    private static int yagens$ammunition(ServerLevel level, ItemStack weapon, Entity shooter, int count, Operation<Integer> original) {
        return shooter instanceof LivingEntity living && RangedShooting.handles(living, weapon) ? 1 : original.call(level, weapon, shooter, count);
    }
    @Shadow protected abstract Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean critical);
    @Shadow protected abstract void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, LivingEntity target);
    @Shadow protected abstract int getDurabilityUse(ItemStack ammo);
    @Inject(method = "shoot", at = @At("HEAD"), cancellable = true)
    private void yagens$shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> ammunition,
                             float velocity, float inaccuracy, boolean critical, LivingEntity target, CallbackInfo callback) {
        if (RangedShooting.shoot(level, shooter, hand, weapon, ammunition,
                ammo -> createProjectile(level, shooter, weapon, ammo, critical),
                projectile -> shootProjectile(shooter, projectile, 0, velocity, inaccuracy, 0, target),
                () -> getDurabilityUse(ammunition.stream().filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY)))) callback.cancel();
    }
}
