package yagen.waitmydawn.yagens_attributes.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import yagen.waitmydawn.yagens_attributes.client.StatText;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.ui.StatusMarkerLayout;
import yagen.waitmydawn.yagens_attributes.network.StatusMarkersPayload;
import java.util.*;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class StatusMarkers {
    private static final Map<Integer, Marker> MARKERS = new LinkedHashMap<>();
    private static Object levelIdentity;
    private static final class Marker {
        final StatusMarkersPayload snapshot;
        final long received;
        int columns;
        List<Component> rows = List.of();
        Marker(StatusMarkersPayload snapshot, long received) { this.snapshot = snapshot; this.received = received; }
        List<Component> rows(int perRow) {
            if (columns != perRow) {
                rows = StatusMarkerLayout.rows(snapshot.counts(), perRow).stream().map(entries -> {
                    MutableComponent line = Component.empty();
                    for (var entry : entries) {
                        if (!line.getSiblings().isEmpty()) line.append("  ");
                        line.append(Component.literal(String.valueOf((char) (0xE000 + entry.type().ordinal()))).withStyle(Style.EMPTY.withFont(StatText.DAMAGE_FONT).withColor(0xFFFFFF)));
                        line.append(" " + entry.count());
                    }
                    return (Component) line;
                }).toList(); columns = perRow;
            }
            return rows;
        }
    }
    private StatusMarkers() {}
    public static void clear() { MARKERS.clear(); levelIdentity = null; }
    private static void world() {
        var level = Minecraft.getInstance().level;
        if (levelIdentity != level) { MARKERS.clear(); levelIdentity = level; }
    }
    public static void accept(StatusMarkersPayload payload) {
        world(); var level = Minecraft.getInstance().level; if (level == null) return;
        if (payload.counts().isEmpty()) {
            var old = MARKERS.get(payload.entityId());
            if (old != null && old.snapshot.entityUuid().equals(payload.entityUuid())) MARKERS.remove(payload.entityId());
            return;
        }
        if (!MARKERS.containsKey(payload.entityId()) && MARKERS.size() >= 4096) MARKERS.remove(MARKERS.keySet().iterator().next());
        MARKERS.put(payload.entityId(), new Marker(payload, level.getGameTime()));
    }
    public static void tick() {
        world(); var level = Minecraft.getInstance().level; if (level == null) return;
        MARKERS.values().removeIf(marker -> {
            var entity = level.getEntity(marker.snapshot.entityId());
            return entity == null ? level.getGameTime() - marker.received > 100
                    : !entity.getUUID().equals(marker.snapshot.entityUuid()) || entity.isRemoved() || !entity.isAlive();
        });
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui || !ClientConfigs.STATUS_MARKERS.get()) return;
        world(); if (MARKERS.isEmpty()) return;
        var camera = event.getCamera(); var buffers = minecraft.renderBuffers().bufferSource();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int range = ClientConfigs.STATUS_MARKER_RANGE.get();
        for (var marker : MARKERS.values()) {
            var entity = minecraft.level.getEntity(marker.snapshot.entityId());
            if (!(entity instanceof LivingEntity target) || !entity.getUUID().equals(marker.snapshot.entityUuid()) || !target.isAlive() || target.isInvisibleTo(minecraft.player)
                    || target.distanceToSqr(camera.getPosition()) > range * range || target == camera.getEntity() && minecraft.options.getCameraType().isFirstPerson()) continue;
            var anchor = target.getPosition(partial).add(0, StatusMarkerLayout.anchorHeight(target.getBbHeight(), ClientConfigs.STATUS_MARKER_HEIGHT_GAP.get()), 0);
            var pose = DamageNumberTransform.billboard(anchor, camera.getPosition(), camera.rotation(), 0, 0);
            var rows = marker.rows(ClientConfigs.STATUS_MARKERS_PER_ROW.get());
            for (int row = 0; row < rows.size(); row++) minecraft.font.drawInBatch(rows.get(row), -minecraft.font.width(rows.get(row)) / 2F,
                    StatusMarkerLayout.rowY(row, rows.size()), 0xFFFFFFFF, false, pose, buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        buffers.endBatch();
    }
}
