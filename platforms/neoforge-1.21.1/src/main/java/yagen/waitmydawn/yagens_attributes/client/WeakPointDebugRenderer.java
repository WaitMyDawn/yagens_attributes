package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;
import yagen.waitmydawn.yagens_attributes.data.WeakPointGeometry;
import yagen.waitmydawn.yagens_attributes.network.WeakPointProfilesPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WeakPointDebugRenderer {
    private static Map<ResourceLocation, List<WeakPointRegion>> profiles = Map.of();
    private static Map<ResourceLocation, List<WeakPointRegion>> pending;

    private WeakPointDebugRenderer() {}

    public static void accept(WeakPointProfilesPayload payload) {
        if (payload.first()) pending = new HashMap<>();
        if (pending == null) return;
        pending.putAll(payload.profiles());
        if (payload.last()) {
            profiles = Map.copyOf(pending);
            pending = null;
        }
    }

    public static void clear() {
        profiles = Map.of();
        pending = null;
    }

    public static void render(PoseStack pose, VertexConsumer lines, Entity entity, float partialTick) {
        if (!(entity instanceof LivingEntity living)) return;
        var regions = profiles.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
        if (regions == null) return;
        for (var region : regions) {
            var box = WeakPointGeometry.box(living, region, partialTick);
            if (box == null) continue;
            pose.pushPose();
            pose.translate(box.offset().x, box.offset().y, box.offset().z);
            if (box.frame() != null) pose.mulPose(box.frame());
            if (box.yaw() != 0) pose.mulPose(Axis.YP.rotationDegrees(-box.yaw()));
            LevelRenderer.renderLineBox(pose, lines, box.bounds(), 1F, 0.72F, 0.24F, 1F);
            pose.popPose();
        }
    }
}
