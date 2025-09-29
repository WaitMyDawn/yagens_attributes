package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.entity.ModOperationTableEntity;
import yagen.waitmydawn.registries.ItemRegistry;

public class ModOperationRenderer implements BlockEntityRenderer<ModOperationTableEntity> {
    private static final ItemStack STACK = new ItemStack(ItemRegistry.ORDIS.get());

    public ModOperationRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ModOperationTableEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float yaw = entity.prevYaw + (entity.yaw - entity.prevYaw) * partialTick;
        float offsetY = entity.prevOffsetY + (entity.offsetY - entity.prevOffsetY) * partialTick;
        float approach = entity.prevApproach + (entity.approach - entity.prevApproach) * partialTick;

        float dirX = entity.prevPlayerDirX + (entity.playerDirX - entity.prevPlayerDirX) * partialTick;
        float dirZ = entity.prevPlayerDirZ + (entity.playerDirZ - entity.prevPlayerDirZ) * partialTick;

        pose.pushPose();

        pose.translate(0.5, 1.2 + offsetY, 0.5);

        float approachOffset = approach * 0.5f;
        pose.translate(
                dirX * approachOffset,
                0,
                dirZ * approachOffset
        );

        pose.mulPose(Axis.YP.rotation(-yaw));

        pose.mulPose(Axis.YP.rotationDegrees(90));

        Minecraft.getInstance().getItemRenderer()
                .renderStatic(STACK, ItemDisplayContext.GROUND,
                        packedLight, OverlayTexture.NO_OVERLAY, pose, buffer, entity.getLevel(), 0);

        pose.popPose();
    }
}