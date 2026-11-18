package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;

final class FrostLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final RenderType ICE = RenderType.entityTranslucent(ElementStatusVisuals.texture("frost_shell"));

    FrostLayer(RenderLayerParent<T, M> parent) { super(parent); }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float swing, float amount,
                       float partial, float age, float yaw, float pitch) {
        if (!ElementStatusVisuals.frozen(entity)) return;
        int alpha = (int)Math.round(ClientConfigs.STATUS.frostOpacity.get() * 255);
        if (alpha == 0) return;
        pose.pushPose();
        pose.scale(1.07F, 1.025F, 1.07F);
        getParentModel().renderToBuffer(pose, buffers.getBuffer(ICE), light, OverlayTexture.NO_OVERLAY, alpha << 24 | 0xFFFFFF);
        pose.popPose();
    }
}
