package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yagen.waitmydawn.yagens_attributes.api.WeaponLifecycleApi;
import yagen.waitmydawn.yagens_attributes.registries.Components;

import java.util.IdentityHashMap;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class WeaponInventoryMixin {
    @Shadow @Final public Player player;

    @WrapMethod(method = "clearOrCountMatchingItems")
    private int yagens$retireClearedWeapons(Predicate<ItemStack> filter, int limit, Container crafting, Operation<Integer> original) {
        if (limit == 0 || !(player.level() instanceof ServerLevel level)) return original.call(filter, limit, crafting);
        var matched = new IdentityHashMap<ItemStack, UUID>();
        int cleared = original.call((Predicate<ItemStack>) stack -> {
            if (!filter.test(stack)) return false;
            var weapon = stack.get(Components.WEAPON_ID.get());
            if (weapon != null) matched.put(stack, weapon);
            return true;
        }, limit, crafting);
        matched.forEach((stack, weapon) -> {
            if (stack.isEmpty()) WeaponLifecycleApi.retireDestroyedWeapon(level, weapon);
        });
        return cleared;
    }
}
