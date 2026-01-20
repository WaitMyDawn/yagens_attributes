package yagen.waitmydawn.capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import static yagen.waitmydawn.gui.mod_operation.ModOperationMenu.NBT_KEY_ON_CHEST;

public class ReservoirsInventoryHandler extends ItemStackHandler {
    private final ItemStack stack;

    public ReservoirsInventoryHandler(ItemStack stack) {
        super(3);
        this.stack = stack;
        load();
    }

    private HolderLookup.Provider getRegistryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        if (FMLEnvironment.dist.isClient()) {
            return ClientRegistryHelper.getClientRegistry();
        }
        return null;
    }

    private static class ClientRegistryHelper {
        public static HolderLookup.Provider getClientRegistry() {
            if (Minecraft.getInstance().level != null) {
                return Minecraft.getInstance().level.registryAccess();
            }
            return null;
        }
    }

    private void load() {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("Inventory")) {
            HolderLookup.Provider provider = getRegistryAccess();
            if (provider != null) {
                this.deserializeNBT(provider, tag.getCompound("Inventory"));
            }
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        HolderLookup.Provider provider = null;

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            provider = ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        if (provider != null) {
            CompoundTag inventoryTag = this.serializeNBT(provider);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, currentTag -> {
                currentTag.put("Inventory", inventoryTag);
            });
        }
    }

    public static ItemStack getReservoirAtIndex(ItemStack chestplate, int index, Player player) {
        if (player.level().isClientSide) return ItemStack.EMPTY;

        CustomData chestData = chestplate.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag chestTag = chestData.copyTag();

        if (chestTag.contains(NBT_KEY_ON_CHEST)) {
            ItemStackHandler handler = new ItemStackHandler(3);
            HolderLookup.Provider provider = player.level().registryAccess();
            handler.deserializeNBT(provider, chestTag.getCompound(NBT_KEY_ON_CHEST));

            ItemStack stackInSlot = handler.getStackInSlot(index);

            if (!stackInSlot.isEmpty()) {
                return stackInSlot;

//                if (handler.getStackInSlot(index).isEmpty() || stackInSlot.getCount() != handler.getStackInSlot(index).getCount()) {
//                    CompoundTag newTag = handler.serializeNBT(provider);
//                    CustomData.update(DataComponents.CUSTOM_DATA, chestplate, currentTag -> {
//                        currentTag.put(NBT_KEY_ON_CHEST, newTag);
//                    });
//                }
            }
        }
        return ItemStack.EMPTY;
    }
}