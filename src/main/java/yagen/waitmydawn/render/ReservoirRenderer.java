package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yagen.waitmydawn.entity.ReservoirEntity;
import yagen.waitmydawn.registries.ItemRegistry;

@OnlyIn(Dist.CLIENT)
public class ReservoirRenderer extends EntityRenderer<ReservoirEntity> {

    public ReservoirRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ReservoirEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0, 0.5 + Math.sin((entity.tickCount + partialTick) * 0.1) * 0.1, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 3));

        ItemStack stackToRender = switch (entity.getReservoirType()) {
            case 1 -> new ItemStack(ItemRegistry.PUMILUM);
            case 2 -> new ItemStack(ItemRegistry.HYDRANGEA);
            default -> new ItemStack(ItemRegistry.GRANDIFLORUM);
        };

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stackToRender,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                0
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ReservoirEntity entity) {
        return null;
    }
}
