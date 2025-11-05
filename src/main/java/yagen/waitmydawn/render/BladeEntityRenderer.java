package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yagen.waitmydawn.entity.BladeEntity;

@OnlyIn(Dist.CLIENT)
public class BladeEntityRenderer extends EntityRenderer<BladeEntity> {

    private final ItemRenderer itemRenderer;

    public BladeEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(BladeEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int packedLight) {
        pose.pushPose();

        Vec3 motion = entity.getDeltaMovement();
        float xRot = -(float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (180F / Math.PI));
        float yRot = -(float) (Mth.atan2(motion.z, motion.x) * (180F / Math.PI));
        pose.mulPose(Axis.YP.rotationDegrees(yRot));
        pose.mulPose(Axis.XP.rotationDegrees(xRot));
        pose.mulPose(Axis.ZP.rotationDegrees((entity.tickCount) * 60));

        pose.scale(1.5F, 1.5F, 1.5F);
        itemRenderer.renderStatic(
                entity.getItem(),
                ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY,
                pose, buffer, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BladeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public Vec3 getRenderOffset(BladeEntity entity, float partialTick) {
        return Vec3.ZERO;
    }
}
