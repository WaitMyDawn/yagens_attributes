package yagen.waitmydawn.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class XpChangeEvent {
    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int gained = event.getAmount();
        int cost = 0;
        if (gained <= 0) return;
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack s : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (!s.isEmpty() && IModContainer.isModContainer(s) && !s.is(ItemRegistry.MOD)) stacks.add(s);
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && IModContainer.isModContainer(chest) && !chest.is(ItemRegistry.MOD)) stacks.add(chest);
        if (stacks.isEmpty()) return;
        gained = (int) Math.ceil((float) gained / stacks.size());
        if (gained <= 0) return;

        ComponentRegistry.UpgradeData data;
        for (ItemStack itemStack : stacks) {
            //if (!IModContainer.isModContainer(itemStack)) return;
            //if (!itemStack.isEmpty()) {
            data = ComponentRegistry.getUpgrade(itemStack);
            if (data.level() >= 30) continue;
            data = data.withExp(gained);
            cost = cost + gained;
            ComponentRegistry.setUpgrade(itemStack, data);
            while (data.exp() >= data.nextLevelExpNeed()) {
                if (data.level() >= 30) break;
                data = data.withLevel(data.level() + 1, data.exp() - data.nextLevelExpNeed());
                ComponentRegistry.setUpgrade(itemStack, data);
            }
            //}
        }
        event.setAmount(Math.max(event.getAmount() - cost, 0));
    }
}
