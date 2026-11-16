package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class CatalystInteractions {
    private CatalystInteractions() {}
    public static InteractionResult apply(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !player.getOffhandItem().is(ModItems.OROKIN_CATALYST.get())) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        var weapon = player.getMainHandItem();
        String error = !WeaponEligibility.operable(weapon, CombatData.WEAPONS.weapon(weapon)) ? "invalid" : weapon.getOrDefault(Components.CATALYZED.get(), false) ? "already" : "";
        if (!error.isEmpty()) { player.displayClientMessage(Component.translatable("item.yagens_attributes.orokin_catalyst." + error), true); return InteractionResult.FAIL; }
        weapon.set(Components.CATALYZED.get(), true); player.getOffhandItem().consume(1, player);
        player.getInventory().setChanged(); player.containerMenu.broadcastChanges(); player.inventoryMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable("item.yagens_attributes.orokin_catalyst.success"), true);
        return InteractionResult.CONSUME;
    }
    private static void handle(PlayerInteractEvent event, java.util.function.Consumer<InteractionResult> result, Runnable cancel) {
        var outcome = apply(event.getEntity(), event.getHand()); if (outcome != InteractionResult.PASS) { result.accept(outcome); cancel.run(); }
    }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void item(PlayerInteractEvent.RightClickItem event) { handle(event, event::setCancellationResult, () -> event.setCanceled(true)); }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void block(PlayerInteractEvent.RightClickBlock event) { handle(event, event::setCancellationResult, () -> event.setCanceled(true)); }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void entity(PlayerInteractEvent.EntityInteract event) { handle(event, event::setCancellationResult, () -> event.setCanceled(true)); }
    @SubscribeEvent(priority = EventPriority.HIGH) public static void specific(PlayerInteractEvent.EntityInteractSpecific event) { handle(event, event::setCancellationResult, () -> event.setCanceled(true)); }
}
