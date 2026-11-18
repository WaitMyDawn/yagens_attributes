package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.api.WeaponLifecycleApi;
import yagen.waitmydawn.yagens_attributes.registries.Components;

public final class WeaponLifecycle {
    private WeaponLifecycle() {}

    public static void retire(ServerLevel level, ItemStack destroyed) {
        WeaponLifecycleApi.retireDestroyedWeapon(level, destroyed.get(Components.WEAPON_ID.get()));
    }

    public static void retireCarrier(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !entity.isRemoved()) return;
        if (entity instanceof ItemEntity dropped) {
            retire(level, dropped.getItem());
        } else if (entity instanceof ThrownTrident trident && trident.pickup == AbstractArrow.Pickup.ALLOWED) {
            var shot = RangedShooting.shot(trident);
            if (shot == null || !shot.virtual()) retire(level, trident.getPickupItemStackOrigin());
        }
    }
}
