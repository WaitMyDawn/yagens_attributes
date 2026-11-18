package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.data.WorldState;
import yagen.waitmydawn.yagens_attributes.registries.Components;

import java.util.UUID;

public final class WeaponLifecycleApi {
    private WeaponLifecycleApi() {}

    /**
     * Creates an independent physical item for a duplication operation. The copy acquires its own
     * identity on first use. Use the native stack copy for moves, persistence, and temporary views.
     */
    public static ItemStack copyAsNew(ItemStack source) {
        return Components.clearWeaponState(source.copy());
    }

    /**
     * Retires a weapon after committed permanent removal. Capture its identity before shrinking
     * the source stack. This is idempotent and must run on the logical server thread.
     */
    public static void retireDestroyedWeapon(ServerLevel level, UUID weaponId) {
        if (weaponId == null) return;
        var server = level.getServer();
        if (!server.isSameThread()) throw new IllegalStateException("Weapon lifecycle requires the server thread");
        WorldState.get(server).retireWeapon(weaponId);
    }
}
