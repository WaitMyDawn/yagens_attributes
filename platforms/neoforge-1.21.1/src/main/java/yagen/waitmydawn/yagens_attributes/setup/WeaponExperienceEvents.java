package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeaponProgress;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.network.ProgressPayload;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class WeaponExperienceEvents {
    private WeaponExperienceEvents() {}
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void gain(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        WorldState state = WorldState.get(player.server);
        Set<UUID> eligible = new LinkedHashSet<>();
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (!Components.absorbsExperience(stack) || !WeaponEligibility.operable(stack, CombatData.WEAPONS.weapon(stack))) continue;
            UUID id = Components.identify(stack);
            if (state.progress(id).level() < WeaponProgress.MAX_LEVEL) eligible.add(id);
        }
        if (eligible.isEmpty()) return;
        var allocation = WeaponProgress.distribute(event.getAmount(), eligible.stream().map(state::progress).toList()); int index = 0;
        for (UUID id : eligible) {
            var gain = allocation.gains().get(index++);
            if (gain.consumed() == 0) continue;
            state.progress(id, gain.progress());
            PacketDistributor.sendToPlayer(player, new ProgressPayload(id, gain.progress()));
        }
        event.setAmount(allocation.remaining());
    }
}
