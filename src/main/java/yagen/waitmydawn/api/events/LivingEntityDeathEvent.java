package yagen.waitmydawn.api.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import yagen.waitmydawn.YagensAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class LivingEntityDeathEvent {
    @SubscribeEvent
    public static void onPlayerDeathWithTotem(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        if(player.level().isClientSide) return;
        ItemStack totem = findTotem(player);
        if(totem.isEmpty()) return;

        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);
        totem.shrink(1);

        event.setCanceled(true);
    }

    private static ItemStack findTotem(Player player) {
//        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))
//            return player.getOffhandItem();
//        if (player.getMainHandItem().is(Items.TOTEM_OF_UNDYING))
//            return player.getMainHandItem();

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
