package yagen.waitmydawn.yagens_attributes.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.DamagePayload;
import yagen.waitmydawn.yagens_attributes.core.ui.DamageScatter;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
public final class DamageNumbers {
    private static final ResourceLocation ICON_FONT = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "damage_icons");
    private static final class Number {
        final int targetId;
        Vec3 anchor;
        final Component text;
        final int color;
        final long start;
        final int lifetime;
        final DamageScatter scatter;
        final int textWidth;

        Number(DamagePayload payload, Component text, int color, long start, int lifetime, DamageScatter scatter, int textWidth) {
            this.targetId = payload.targetId(); this.anchor = payload.position(); this.text = text;
            this.color = color; this.start = start; this.lifetime = lifetime; this.scatter = scatter;
            this.textWidth = textWidth;
        }
    }
    private static final Deque<Number> NUMBERS = new ArrayDeque<>();
    private static Object levelIdentity;
    private static long sequence;
    private DamageNumbers() {}

    @EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
    public static final class Setup {
        @SubscribeEvent public static void setup(FMLClientSetupEvent event) { event.enqueueWork(() -> DamagePayload.clientSink(DamageNumbers::add)); }
    }

    private static void add(DamagePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !ClientConfigs.DAMAGE_NUMBERS.get()) return;
        if (!payload.attacker().equals(minecraft.player.getUUID()) && !ClientConfigs.SEE_OTHERS.get()) return;
        if (levelIdentity != minecraft.level) { NUMBERS.clear(); levelIdentity = minecraft.level; }
        int tier = payload.tier();
        int color = tier == 0 ? 0xFFFFFF : tier == 1 ? 0xFFFF00 : tier == 2 ? 0xFF8800 : 0xFF0000;
        MutableComponent text = Component.literal(String.format(Locale.ROOT, "%.1f", payload.amount()) + "!".repeat(Math.max(0, tier - 3)));
        for (DamageType type : DamageType.DISPLAY_ORDER) {
            if ((payload.procMask() & (1 << type.ordinal())) != 0)
                text.append(Component.literal(" " + (char) (0xE000 + type.ordinal())).withStyle(Style.EMPTY.withFont(ICON_FONT).withColor(0xFFFFFF)));
        }
        while (NUMBERS.size() >= ClientConfigs.MAX_NUMBERS.get()) NUMBERS.removeFirst();
        int textWidth = minecraft.font.width(text);
        DamageScatter scatter = DamageScatter.sample(sequence++, minecraft.level.random.nextDouble(), minecraft.level.random.nextDouble()).forTextWidth(textWidth);
        NUMBERS.addLast(new Number(payload, text, color, minecraft.level.getGameTime(), ClientConfigs.LIFETIME.get(), scatter, textWidth));
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { NUMBERS.clear(); levelIdentity = null; }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != levelIdentity) { NUMBERS.clear(); return; }
        long tick = minecraft.level.getGameTime();
        NUMBERS.removeIf(number -> tick - number.start >= number.lifetime);
        if (!ClientConfigs.DAMAGE_NUMBERS.get() || NUMBERS.isEmpty()) return;
        var buffers = minecraft.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        for (Number number : NUMBERS) {
            float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            float age = tick - number.start + partial;
            var target = minecraft.level.getEntity(number.targetId);
            if (target != null && !target.isRemoved()) number.anchor = target.getPosition(partial).add(0, target.getBbHeight() * 0.65, 0);
            var scatter = number.scatter;
            var pose = DamageNumberTransform.billboard(number.anchor, camera, event.getCamera().rotation(), age * 0.01F,
                    scatter.x() + scatter.velocityX() * age, scatter.y() + scatter.velocityY() * age);
            int alpha = Math.max(4, (int) (255 * Math.min(1, (number.lifetime - age) / 8)));
            minecraft.font.drawInBatch(number.text, -number.textWidth / 2F, 0,
                    (alpha << 24) | number.color, false, pose, buffers, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        }
        buffers.endBatch();
    }
}
