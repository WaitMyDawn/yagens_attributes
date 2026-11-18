package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;
import yagen.waitmydawn.yagens_attributes.data.WeakPointDefinitions;
import yagen.waitmydawn.yagens_attributes.network.WeakPointProfilesPayload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class WeakPointSync {
    private WeakPointSync() {}

    @SubscribeEvent public static void reload(AddReloadListenerEvent event) {
        event.addListener(WeakPointDefinitions.INSTANCE);
    }

    @SubscribeEvent public static void tags(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) WeakPointDefinitions.INSTANCE.invalidate();
    }

    @SubscribeEvent public static void sync(OnDatapackSyncEvent event) {
        var profiles = WeakPointDefinitions.INSTANCE.profiles();
        var batches = new ArrayList<WeakPointProfilesPayload>();
        var chunk = new LinkedHashMap<ResourceLocation, List<WeakPointRegion>>();
        int remaining = profiles.size();
        for (var entry : profiles.entrySet()) {
            chunk.put(entry.getKey(), entry.getValue());
            remaining--;
            if (chunk.size() == WeakPointProfilesPayload.CHUNK_SIZE || remaining == 0) {
                batches.add(new WeakPointProfilesPayload(batches.isEmpty(), remaining == 0, Map.copyOf(chunk)));
                chunk.clear();
            }
        }
        if (batches.isEmpty()) batches.add(new WeakPointProfilesPayload(true, true, Map.of()));
        event.getRelevantPlayers().forEach(player -> batches.forEach(payload -> PacketDistributor.sendToPlayer(player, payload)));
    }
}
