package yagen.waitmydawn.yagens_attributes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yagen.waitmydawn.yagens_attributes.core.RangedStats;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void yagens$emptyVolley(ItemStack weapon, Level level, LivingEntity shooter, int remaining, CallbackInfo callback) {
        var shot = RangedShooting.snapshot(shooter, weapon);
        if (shot != null && shot.multishot() == 0 && shooter instanceof Player player && EnchantmentHelper.getTridentSpinAttackStrength(weapon, player) == 0) callback.cancel();
    }
    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean yagens$throw(Level world, Entity entity, ItemStack weapon, Level level, LivingEntity shooter, int remaining) {
        var snapshot = RangedShooting.snapshot(shooter, weapon);
        if (snapshot != null && world instanceof ServerLevel server && entity instanceof Projectile projectile) {
            var volley = RangedStats.volley(snapshot.multishot(), shooter.getRandom()::nextDouble);
            if (volley.count() == 0) return false;
            return RangedShooting.spawn(server, projectile, snapshot, volley);
        }
        return world.addFreshEntity(entity);
    }
}
