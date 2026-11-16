package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.core.RangedStats;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

public final class ClientRangedUse {
    private static int sequence;
    private static ChargeRequest active;
    private static RangedStats.ChargeClock clock;
    private static boolean requested;
    private ClientRangedUse() {}
    public static void start(LivingEntityUseItemEvent.Start event) {
        var minecraft = Minecraft.getInstance();
        if (event.getEntity() != minecraft.player || !RangedShooting.supported(event.getItem())) return;
        var profile = ClientProfiles.get(event.getItem());
        clock = new RangedStats.ChargeClock(profile == null ? 1 : profile.fireRate());
        sequence = sequence == Integer.MAX_VALUE ? 0 : sequence + 1;
        active = new ChargeRequest(sequence, BuiltInRegistries.ITEM.getKey(event.getItem().getItem()), event.getItem().get(Components.WEAPON_ID.get()), event.getHand());
        requested = false;
    }
    public static void tick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() == Minecraft.getInstance().player && clock != null && active != null) {
            // Send after the vanilla use packet so the reply can reflect the server's active charge session.
            if (!requested && Minecraft.getInstance().getConnection() != null) { requested = true; PacketDistributor.sendToServer(active); }
            event.setDuration(clock.beforeTick(event.getDuration()));
        }
    }
    public static void accept(ChargePayload payload) { if (payload.request().equals(active) && clock != null) clock.rate(payload.rate()); }
    public static void stop(LivingEntity entity) { if (entity == Minecraft.getInstance().player) clear(); }
    public static void clear() { active = null; clock = null; }
}
