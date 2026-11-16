package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

public final class ChargeService {
    private record Budget(int tick, int count) {}
    private static final Map<ServerPlayer, Budget> BUDGETS = new WeakHashMap<>();
    private ChargeService() {}
    public static void handle(ServerPlayer player, ChargeRequest request) {
        if (request.sequence() < 0) return;
        int tick = player.server.getTickCount(); var budget = BUDGETS.get(player);
        int used = budget == null || tick - budget.tick() >= 20 ? 0 : budget.count();
        if (used >= 20) return;
        BUDGETS.put(player, new Budget(used == 0 ? tick : budget.tick(), used + 1));
        var stack = player.getItemInHand(request.hand());
        if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(request.item()) || !Objects.equals(stack.get(Components.WEAPON_ID.get()), request.weapon())) return;
        var snapshot = RangedShooting.snapshot(player, stack);
        PacketDistributor.sendToPlayer(player, new ChargePayload(request, RangedUseEvents.activeRate(player, stack, snapshot == null ? 1 : snapshot.fireRate())));
    }
}
