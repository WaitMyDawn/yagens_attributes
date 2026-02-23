package yagen.waitmydawn.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.registries.ComponentRegistry;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"), cancellable = true)
    private void handleSilentUpgradeUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!ClientConfigs.IF_FIX_GAIN_XP.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (!minecraft.isSameThread()) {
            return;
        }
        Player player = minecraft.player;

        if (packet.getContainerId() == 0) {

            int slot = packet.getSlot();
            ItemStack existingStack = player.inventoryMenu.getSlot(slot).getItem();
            ItemStack newStack = packet.getItem();

            if (!existingStack.isEmpty() && !newStack.isEmpty() &&
                    existingStack.getItem() == newStack.getItem() &&
                    existingStack.has(ComponentRegistry.UPGRADE_DATA.get()) &&
                    newStack.has(ComponentRegistry.UPGRADE_DATA.get())) {

                ItemStack existingCopy = existingStack.copy();
                ItemStack newCopy = newStack.copy();
                existingCopy.remove(ComponentRegistry.UPGRADE_DATA.get());
                newCopy.remove(ComponentRegistry.UPGRADE_DATA.get());

                if (ItemStack.matches(existingCopy, newCopy)) {
                    existingStack.applyComponents(newStack.getComponentsPatch());
                    ci.cancel();
                }
            }
        }
    }
}