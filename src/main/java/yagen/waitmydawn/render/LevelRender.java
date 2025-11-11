package yagen.waitmydawn.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class LevelRender {
    @SubscribeEvent
    public static void levelRender(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)
                || !(event.getEntity() instanceof Monster))
            return;
        AttributeInstance entityLevel = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.ENTITY_LEVEL.get()));
        if (entityLevel == null) return;
        int level = (int) entityLevel.getValue();
        Component text = Component.literal("Lv. " + level);
        event.setContent(text);
        event.setCanRender(TriState.TRUE);
    }
}
