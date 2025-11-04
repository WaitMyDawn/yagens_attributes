package yagen.waitmydawn.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class LevelRender {
    @SubscribeEvent
    public static void levelRender(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)
                || !(event.getEntity() instanceof Monster))
            return;
        Player player = Minecraft.getInstance().player;
        if (!player.level().isClientSide) return;
        AttributeInstance entityLevel = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;
        int level = (int) entityLevel.getValue();

        Component text = Component.literal("Lv. " + level)
                .withStyle(ChatFormatting.LIGHT_PURPLE);

        Minecraft instance = Minecraft.getInstance();
        Font font = instance.font;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        float yOffset = livingEntity.getBbHeight() + 0.5F;

        pose.pushPose();
        pose.translate(0, yOffset, 0);

        pose.mulPose(instance.gameRenderer.getMainCamera().rotation());

        float scale = 0.025F;
        pose.scale(-scale, -scale, scale);

        float bgOpacity = instance.options.getBackgroundOpacity(0.25F);
        int light = event.getPackedLight();
        font.drawInBatch(
                text,
                -font.width(text) / 2F, 0,
                0xFFFFFF,
                false,
                pose.last().pose(),
                buffers,
                Font.DisplayMode.SEE_THROUGH,
                (int) (bgOpacity * 0xFF) << 24,
                light
        );
        pose.popPose();
    }
}
