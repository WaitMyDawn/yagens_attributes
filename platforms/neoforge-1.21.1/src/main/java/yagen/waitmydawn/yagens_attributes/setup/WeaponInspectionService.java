package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

public final class WeaponInspectionService {
    private static final Map<ServerPlayer, Integer> LAST_REQUEST = new WeakHashMap<>();
    private WeaponInspectionService() {}
    public static void handle(ServerPlayer player, TooltipRequest request) {
        int tick = player.server.getTickCount();
        Integer previous = LAST_REQUEST.get(player);
        if (previous != null && tick - previous < 2) return;
        var menu = player.containerMenu;
        if (menu.containerId != request.menuId() || request.slot() < 0 || request.slot() >= Math.min(4096, menu.slots.size())) return;
        ItemStack stack = menu.slots.get(request.slot()).getItem();
        UUID id = stack.get(Components.WEAPON_ID.get());
        if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(request.item()) || !Objects.equals(id, request.weapon())) return;
        var profile = CombatData.WEAPONS.weapon(stack);
        if (profile == null) return;
        LAST_REQUEST.put(player, tick);
        try { PacketDistributor.sendToPlayer(player, new TooltipPayload(request.item(), id, describe(player, stack, profile))); }
        catch (IllegalArgumentException exception) {
            // Invalid modifiers must not replace a valid cache entry or disconnect a hovering client.
        }
    }

    /** Authoritative item/menu inspection and panel snapshots share the same current-wielder calculation. */
    public static WeaponInspectionData describe(ServerPlayer player, ItemStack stack, WeaponProfile profile) {
        if (profile == null || stack.isEmpty()) throw new IllegalArgumentException("Missing weapon profile");
        UUID id = stack.get(Components.WEAPON_ID.get());
        WorldState state = WorldState.get(player.server);
        List<Map<String, Bonus>> sources = new ArrayList<>();
        if (id != null) sources.addAll(state.ledger.bonuses(id, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
        sources.add(state.bonuses(player.getUUID()));
        if (ModInfo.Category.MELEE.contains(profile.category())) sources = new ArrayList<>(ComboService.sources(player, id, sources));
        sources = new ArrayList<>(ConditionalModService.sources(player, stack, sources, null));
        return new WeaponInspectionData(PanelService.resolve(profile, sources, yagen.waitmydawn.yagens_attributes.config.ServerConfigs.COMBO_DURATION.get()), state.progress(id), state.ledger.usedCapacity(id, WeaponPolarityData.profile(stack, profile, state.ledger).slots()), player.isCreative());
    }
}
