package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class MagneticLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "textures/effect/status/magnetic_shell.png");
    private MagneticLayer(RenderLayerParent<T, M> parent) { super(parent); }
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event) {
        var seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<EntityRenderer<?>, Boolean>());
        for (var type : event.getEntityTypes()) add(event.getRenderer(type), seen);
        for (var skin : event.getSkins()) add(event.getSkin(skin), seen);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void add(EntityRenderer<?> renderer, java.util.Set<EntityRenderer<?>> seen) {
        if (renderer instanceof LivingEntityRenderer living && seen.add(renderer)) living.addLayer(new MagneticLayer(living));
    }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float swing, float amount,
                                 float partial, float age, float yaw, float pitch) {
        if (!StatusEffectsRenderer.magnetic(entity)) return;
        float time = entity.tickCount + partial;
        pose.pushPose(); pose.scale(1.055F, 1.012F, 1.055F);
        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.energySwirl(TEXTURE, time * .008F % 1, time * .004F % 1)),
                0xF000F0, OverlayTexture.NO_OVERLAY, 0x8FFFFFFF);
        pose.popPose();
    }
}
