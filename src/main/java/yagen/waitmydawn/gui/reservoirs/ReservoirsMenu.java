package yagen.waitmydawn.gui.reservoirs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.compat.ISSCompat;
import yagen.waitmydawn.registries.MenuRegistry;
import yagen.waitmydawn.util.SupportedMod;

public class ReservoirsMenu extends AbstractContainerMenu {
    private final int lockedSlotIndex;

    public ReservoirsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new ItemStackHandler(3));
    }

    public ReservoirsMenu(int containerId, Inventory playerInventory, ItemStackHandler inventory) {
        super(MenuRegistry.RESERVOIRS_MENU.get(), containerId);
        this.lockedSlotIndex = playerInventory.selected;

        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotItemHandler(inventory, i, 62 + i * 18, 13) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if (IModContainer.isModContainer(stack))
                        return IModContainer.get(stack).getModAtIndex(0).getMod().isReservoir();
                    else if (ModList.get().isLoaded(SupportedMod.IRONS_SPELLBOOKS.getValue()))
                        return ISSCompat.isReservoir(stack);
                    return false;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addPlayerSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18);
            }
        }

        for (int col = 0; col < 9; col++) {
            addPlayerSlot(playerInventory, col, 8 + col * 18, 142);
        }
    }

    private void addPlayerSlot(Inventory playerInventory, int index, int x, int y) {
        Slot slot = new Slot(playerInventory, index, x, y);

        if (index == lockedSlotIndex) {
            slot = new Slot(playerInventory, index, x, y) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
        }
        this.addSlot(slot);
    }

    public ItemStack getItemStack() {
        return this.slots.get(lockedSlotIndex + 30).getItem();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem() && slot.getSlotIndex() != lockedSlotIndex) {
            ItemStack itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(itemStack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack1, 0, 3, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !player.getMainHandItem().isEmpty() && player.getInventory().selected == lockedSlotIndex;
    }
}
