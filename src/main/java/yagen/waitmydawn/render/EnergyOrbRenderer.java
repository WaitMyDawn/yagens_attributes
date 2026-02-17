package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.entity.EnergyOrbEntity;

public class EnergyOrbRenderer extends EntityRenderer<EnergyOrbEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/item/flow_armor_mod.png");

    public EnergyOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    protected int getBlockLightLevel(EnergyOrbEntity entity, BlockPos pos) {
        return 10;
    }

    @Override
    public void render(EnergyOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        int light = 15728880;

        float time = (float) entity.tickCount + partialTicks;
        poseStack.translate(0.0F, 0.3F + Mth.sin(time * 0.2F) * 0.03F, 0.0F);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        float scale;
        if (entity.isLarge()) {
            scale = 0.5F;
        } else {
            scale = 0.25F;
        }
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 2.0F));

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        PoseStack.Pose last = poseStack.last();
        Matrix4f poseMatrix = last.pose();

        vertex(vertexConsumer, poseMatrix, -0.5F, -0.5F, 0, 0, 1, light);
        vertex(vertexConsumer, poseMatrix, 0.5F, -0.5F, 0, 1, 1, light);
        vertex(vertexConsumer, poseMatrix, 0.5F, 0.5F, 0, 1, 0, light);
        vertex(vertexConsumer, poseMatrix, -0.5F, 0.5F, 0, 0, 0, light);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, int z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EnergyOrbEntity entity) {
        return TEXTURE;
    }
}