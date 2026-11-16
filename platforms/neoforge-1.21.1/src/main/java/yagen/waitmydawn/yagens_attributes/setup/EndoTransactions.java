package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.EndoCurrency;
import yagen.waitmydawn.yagens_attributes.data.WorldState;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;
import java.util.*;

/** Transfers run on the server thread and preflight the entire main inventory before debiting. */
public final class EndoTransactions {
    private EndoTransactions() {}
    private static List<Integer> room(ServerPlayer player, ItemStack token) {
        var inventory = player.getInventory();
        List<Integer> result = new ArrayList<>();
        int maximum = Math.min(token.getMaxStackSize(), inventory.getMaxStackSize());
        for (ItemStack stack : inventory.items) result.add(stack.isEmpty() ? maximum
                : ItemStack.isSameItemSameComponents(stack, token) ? Math.max(0, maximum - stack.getCount()) : 0);
        return List.copyOf(result);
    }
    public static int availableSpace(ServerPlayer player) {
        return availableSpace(player, false);
    }
    public static int availableSpace(ServerPlayer player, boolean kuva) { return room(player, new ItemStack(kuva ? ModItems.KUVA.get() : ModItems.ENDO.get())).stream().mapToInt(Integer::intValue).sum(); }
    public static void withdraw(ServerPlayer player, WorldState state, long amount) {
        withdraw(player, state, amount, false);
    }
    public static void withdraw(ServerPlayer player, WorldState state, long amount, boolean kuva) {
        var token = new ItemStack(kuva ? ModItems.KUVA.get() : ModItems.ENDO.get());
        var plan = EndoCurrency.withdraw(kuva ? state.kuva(player.getUUID()) : state.endo(player.getUUID()), amount, room(player, token));
        var inventory = player.getInventory();
        for (int slot = 0; slot < plan.additions().size(); slot++) {
            int count = plan.additions().get(slot);
            if (count == 0) continue;
            ItemStack stack = inventory.items.get(slot);
            if (stack.isEmpty()) inventory.items.set(slot, token.copyWithCount(count)); else stack.grow(count);
        }
        if (kuva) state.kuva(player.getUUID(), plan.balance()); else state.endo(player.getUUID(), plan.balance());
        inventory.setChanged(); player.containerMenu.broadcastChanges();
    }
}
