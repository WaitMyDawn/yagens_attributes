package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.Tags;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ClientConfigs;

import static yagen.waitmydawn.api.mission.MissionHandler.isBoss;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class LevelRender {
    @SubscribeEvent
    public static void levelRender(RenderLivingEvent.Post<?, ?> event) {
        if (!ClientConfigs.LV_CONTENT.get()) return;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Enemy)) return;

        AttributeInstance entityLevel = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;

        int level = (int) entityLevel.getValue();
        Component text = Component.literal("Lv. " + level);

        Minecraft instance = Minecraft.getInstance();
        if (instance.getCameraEntity() == null) return;

        AABB boundingBox = livingEntity.getBoundingBox();
        double height = boundingBox.getYsize();

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers =
                instance.renderBuffers().bufferSource();
        Vec3 camera = instance.gameRenderer.getMainCamera().getPosition();
        double dx = livingEntity.getX() - camera.x;
        double dz = livingEntity.getZ() - camera.z;

        pose.pushPose();
        pose.translate(0, height * 1.2F, 0);

        float yRot = (float) Mth.atan2(dx, dz) * Mth.RAD_TO_DEG;
        pose.mulPose(Axis.YP.rotationDegrees(yRot));
        pose.mulPose(Axis.XP.rotationDegrees(instance.gameRenderer.getMainCamera().getXRot()));
        pose.mulPose(Axis.ZP.rotationDegrees(180F));
        float scale = 0.025F;

        if (ClientConfigs.LV_CONTENT_ENLARGE.get()) {
            float enlarge = (float) Math.pow(height * boundingBox.getXsize() * boundingBox.getZsize() / 0.7, 1.0 / 3.0);
            if (enlarge > 1)
                scale = (float) Math.min(0.125, (scale * (1 + (enlarge - 1) * 0.5)));
        }

        pose.scale(scale, scale, scale);

        Font font = instance.font;
        int color = 0xFFFFFF;
        Font.DisplayMode mode = Font.DisplayMode.NORMAL;
        if (livingEntity.getType().is(Tags.EntityTypes.BOSSES) || isBoss(livingEntity.getType())) {
            color = ClientConfigs.LV_BOSS_COLOR.get();
            if (ClientConfigs.BOSS_LV_SEE_THROUGH.get()) mode = Font.DisplayMode.SEE_THROUGH;
        }

        font.drawInBatch(text,
                -font.width(text) / 2f,
                0,
                color,
                false,
                pose.last().pose(),
                event.getMultiBufferSource(),
                mode,
                0,
                event.getPackedLight());

        pose.popPose();
        buffers.endBatch();
    }
}
