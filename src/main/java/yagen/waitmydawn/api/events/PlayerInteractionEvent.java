package yagen.waitmydawn.api.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.registries.MobEffectRegistry;

import static yagen.waitmydawn.effect.NourishEffect.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerInteractionEvent {
    @SubscribeEvent
    public static void onPlayerInteractMeatShredder(PlayerInteractEvent.RightClickItem event) {
        if (!(ModList.get().isLoaded("cataclysm"))) return;
        if (!ServerConfigs.BAN_MEAT_SHREDDER_OFFHAND.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (itemStack.getItem() == LEndersCataclysmItem.MEAT_SHREDDER.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }

    @SubscribeEvent
    public static void onSplashLingering(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        Item item = event.getItemStack().getItem();
        Player player = event.getEntity();

        if ((item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) || player.isCreative()) return;

        if (player.getCooldowns().isOnCooldown(item)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        player.getCooldowns().addCooldown(item, ServerConfigs.SPLASH_POTION_COOLDOWN.get());
    }

    @SubscribeEvent
    public static void EatAndDrink(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack itemStack = event.getItem();

        if (itemStack.getUseAnimation() != UseAnim.EAT && itemStack.getUseAnimation() != UseAnim.DRINK) return;
        if (player.hasEffect(MobEffectRegistry.NOURISH)) {
            event.setDuration(Math.max(0, event.getDuration() - (int) (16 * getNourishEnhance(player))));
        }
    }


    @SubscribeEvent
    public static void nourishCount(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (!(player.hasEffect(MobEffectRegistry.NOURISH))) return;
        ItemStack itemStack = event.getItem();
        if (!(itemStack.has(DataComponents.FOOD))) return;
        FoodProperties food = itemStack.get(DataComponents.FOOD);
        if (food == null) return;
        addNourishCount(player, Math.min(NOURISH_NEED, getNourishCount(player) + food.nutrition() * food.saturation()));
    }
}
