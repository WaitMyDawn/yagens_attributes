package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.WeaponProfile;
import java.util.*;
import java.util.function.Function;

/** The same eligibility rule gates panel identities and the client hotbar selector. */
public final class WeaponEligibility {
    private WeaponEligibility() {}
    public static boolean operable(ItemStack stack, WeaponProfile profile) { return profile != null && stack.getCount() == 1 && stack.getMaxStackSize() == 1; }
    public static List<Integer> hotbar(List<ItemStack> inventory, Function<ItemStack, WeaponProfile> profiles) {
        var result = new ArrayList<Integer>();
        for (int slot = 0; slot < Math.min(9, inventory.size()); slot++) if (operable(inventory.get(slot), profiles.apply(inventory.get(slot)))) result.add(slot);
        return List.copyOf(result);
    }
}
