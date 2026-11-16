package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

public final class WeaponInspectionCache {
    private record Key(ResourceLocation item, UUID weapon) {}
    private record Cached(WeaponInspectionData data, long tick) {}
    private static final Map<Key, Cached> VALUES = new LinkedHashMap<>();
    private static final Map<Key, Long> REQUESTS = new LinkedHashMap<>();
    private static long lastRequestTick = Long.MIN_VALUE / 2;
    private WeaponInspectionCache() {}
    public static WeaponInspectionData get(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return null;
        var key = new Key(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.get(Components.WEAPON_ID.get()));
        var cached = VALUES.get(key);
        return cached == null ? null : cached.data();
    }
    public static int menuSlot(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        if (player == null || stack.isEmpty()) return -1;
        var menu = player.containerMenu;
        for (int slot = 0; slot < Math.min(4096, menu.slots.size()); slot++)
            if (ItemStack.isSameItemSameComponents(menu.slots.get(slot).getItem(), stack)) return slot;
        return -1;
    }
    /** Called only for the tooltip actually being rendered, not while Creative/search indexes build their text. */
    public static void request(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.getConnection() == null || stack.isEmpty()) return;
        var key = new Key(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.get(Components.WEAPON_ID.get()));
        long tick = minecraft.level.getGameTime();
        Cached cached = VALUES.get(key);
        if ((cached == null || tick - cached.tick() >= 20) && tick - REQUESTS.getOrDefault(key, Long.MIN_VALUE / 2) >= 20 && tick - lastRequestTick >= 2) {
            var menu = minecraft.player.containerMenu;
            int slot = menuSlot(stack);
            if (slot >= 0) {
                REQUESTS.put(key, tick); trim(REQUESTS); lastRequestTick = tick;
                PacketDistributor.sendToServer(new TooltipRequest(menu.containerId, slot, key.item(), key.weapon()));
            }
        }
    }
    public static void accept(TooltipPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        VALUES.put(new Key(payload.item(), payload.weapon()), new Cached(payload.data(), level.getGameTime())); trim(VALUES);
    }
    public static void accept(ProgressPayload payload) {
        VALUES.replaceAll((key, old) -> Objects.equals(key.weapon(), payload.weapon())
                ? new Cached(new WeaponInspectionData(old.data.resolved(), payload.progress(), old.data.usedCapacity(), old.data.creative()), old.tick) : old);
    }
    public static void accept(PanelPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level == null || payload.profile() == null || payload.weaponId() == null || payload.weaponStack().isEmpty()
                || !payload.weaponId().equals(payload.weaponStack().get(Components.WEAPON_ID.get()))) return;
        var key = new Key(BuiltInRegistries.ITEM.getKey(payload.weaponStack().getItem()), payload.weaponId());
        var data = new WeaponInspectionData(payload.resolved(), payload.progress(), payload.usedCapacity(), payload.creative());
        if (WeaponTooltip.complete(payload.profile(), data)) { VALUES.put(key, new Cached(data, level.getGameTime())); trim(VALUES); }
    }
    private static void trim(Map<?, ?> map) { while (map.size() > 256) map.remove(map.keySet().iterator().next()); }
    public static void clear() { VALUES.clear(); REQUESTS.clear(); lastRequestTick = Long.MIN_VALUE / 2; }
}
