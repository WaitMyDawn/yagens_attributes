package yagen.waitmydawn.jei;

import net.minecraft.world.item.ItemStack;

public record ModLootWrapper(ClientLootLoader.LootInfo info, ItemStack resultStack) {
}
