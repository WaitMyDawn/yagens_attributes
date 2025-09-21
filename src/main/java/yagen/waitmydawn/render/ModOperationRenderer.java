package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.entity.ModOperationTableEntity;
import yagen.waitmydawn.registries.ItemRegistry;

public class ModOperationRenderer implements BlockEntityRenderer<ModOperationTableEntity> {
    private static final ItemStack STACK = new ItemStack(ItemRegistry.ORDIS.get());

    public ModOperationRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ModOperationTableEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 插值计算当前帧的状态
        float yaw = entity.prevYaw + (entity.yaw - entity.prevYaw) * partialTick;
        float offsetY = entity.prevOffsetY + (entity.offsetY - entity.prevOffsetY) * partialTick;
        float approach = entity.prevApproach + (entity.approach - entity.prevApproach) * partialTick;

        // 插值计算方向向量 - 这是关键修复
        float dirX = entity.prevPlayerDirX + (entity.playerDirX - entity.prevPlayerDirX) * partialTick;
        float dirZ = entity.prevPlayerDirZ + (entity.playerDirZ - entity.prevPlayerDirZ) * partialTick;

        pose.pushPose();

        // 基础位置 - 方块上方
        pose.translate(0.5, 1.2 + offsetY, 0.5);

        // 应用接近玩家的偏移 - 使用平滑后的方向向量
        float approachOffset = approach * 0.5f;
        pose.translate(
                dirX * approachOffset,
                0,
                dirZ * approachOffset
        );

        // 应用旋转
        pose.mulPose(Axis.YP.rotation(-yaw));

        // 让物品正面朝前
        pose.mulPose(Axis.YP.rotationDegrees(90));

        // 渲染物品
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(STACK, ItemDisplayContext.GROUND,
                        packedLight, OverlayTexture.NO_OVERLAY, pose, buffer, entity.getLevel(), 0);

        pose.popPose();
    }
}