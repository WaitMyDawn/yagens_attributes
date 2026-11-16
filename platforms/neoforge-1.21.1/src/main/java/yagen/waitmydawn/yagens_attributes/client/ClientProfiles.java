package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.WeaponProfile;
import yagen.waitmydawn.yagens_attributes.network.ProfilePayload;
import java.util.*;

public final class ClientProfiles {
    private static Map<ResourceLocation, WeaponProfile> profiles = Map.of();
    private static Map<ResourceLocation, WeaponProfile> pending;
    private ClientProfiles() {}

    public static void accept(ProfilePayload payload) {
        if (payload.first()) pending = new HashMap<>();
        if (pending == null) return;
        pending.putAll(payload.profiles());
        if (payload.last()) { profiles = Map.copyOf(pending); pending = null; }
    }
    public static WeaponProfile get(ItemStack stack) { return profiles.get(BuiltInRegistries.ITEM.getKey(stack.getItem())); }
    public static WeaponProfile get(ResourceLocation id) { return profiles.get(id); }
    public static void clear() { profiles = Map.of(); pending = null; }
}
