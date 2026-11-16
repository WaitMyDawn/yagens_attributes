package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import yagen.waitmydawn.yagens_attributes.data.WeaponEligibility;

/** Eligibility follows synchronized server profiles; filtering never changes relative hotbar order. */
public final class HotbarWeapons {
    private HotbarWeapons() {}
    public static boolean operable(ItemStack stack) { return WeaponEligibility.operable(stack, ClientProfiles.get(stack)); }
    public static List<Integer> slots(List<ItemStack> inventory) {
        return WeaponEligibility.hotbar(inventory, ClientProfiles::get);
    }
    public static boolean select(Minecraft minecraft, int slot) {
        if (minecraft.player == null || minecraft.getConnection() == null || slot < 0 || slot >= 9
                || !operable(minecraft.player.getInventory().getItem(slot))) return false;
        if (minecraft.player.getInventory().selected == slot) return false;
        if (minecraft.player.isUsingItem() && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND) minecraft.player.stopUsingItem();
        minecraft.player.getInventory().selected = slot;
        // Send before the panel refresh on the same connection; the server handles native selection first.
        minecraft.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        return true;
    }
}
