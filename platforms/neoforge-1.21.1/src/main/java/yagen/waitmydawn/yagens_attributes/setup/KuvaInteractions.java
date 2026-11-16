package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class KuvaInteractions {
    private KuvaInteractions() {}
    public static boolean clean(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !player.getOffhandItem().is(ModItems.KUVA.get())
                || player.getMainHandItem().isEmpty() || player.getMainHandItem().getOrDefault(DataComponents.REPAIR_COST, 0) <= 0) return false;
        if (!player.level().isClientSide()) {
            player.getMainHandItem().set(DataComponents.REPAIR_COST, 0);
            player.getOffhandItem().consume(1, player);
            player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.translatable("item.yagens_attributes.kuva.cleansed"), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, .6F, 1.2F);
        }
        return true;
    }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void item(PlayerInteractEvent.RightClickItem event) {
        if (clean(event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide())); event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void block(PlayerInteractEvent.RightClickBlock event) {
        if (clean(event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide())); event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void entity(PlayerInteractEvent.EntityInteract event) {
        if (clean(event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide())); event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void specific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (clean(event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide())); event.setCanceled(true);
        }
    }
}
