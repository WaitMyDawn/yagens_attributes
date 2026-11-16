package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.Combo;
import yagen.waitmydawn.yagens_attributes.network.ComboPayload;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.Objects;

@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
public final class ComboHud {
    private static ComboPayload snapshot;
    private static int remaining;
    private ComboHud() {}
    public static void accept(ComboPayload payload) { snapshot = payload; remaining = payload.remaining(); }
    public static void clear() { snapshot = null; remaining = 0; }
    public static void tick() { if (remaining > 0 && !Minecraft.getInstance().isPaused()) remaining--; }
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof HudEditorScreen editor && editor.edits(HudElement.COMBO)) return;
        if (snapshot == null || snapshot.count() == 0 || remaining <= 0 || minecraft.player == null || minecraft.options.hideGui || !ClientConfigs.COMBO_HUD.get()
                || !Objects.equals(snapshot.weapon(), minecraft.player.getMainHandItem().get(Components.WEAPON_ID.get()))) return;
        var graphics = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth(), height = minecraft.getWindow().getGuiScaledHeight();
        var box = HudElement.COMBO.place(width, height, HudElement.COMBO.settings());
        int tier = new Combo.Rules(snapshot.hitsPerTier(), snapshot.maxTier()).tier(snapshot.count());
        graphics.pose().pushPose(); graphics.pose().translate(box.x(), box.y(), 0); graphics.pose().scale(box.scale(), box.scale(), 1);
        draw(graphics, snapshot.count(), tier, remaining / (float) Math.max(1, snapshot.duration()));
        graphics.pose().popPose();
    }
    static void draw(GuiGraphics graphics, int count, int tier, float fraction) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("hud.yagens_attributes.combo", count, tier), 0, 0, 0xFFE1CE90, false);
        graphics.fill(0, 17, 112, 20, 0x70425059);
        graphics.fill(0, 17, Math.round(112F * Math.max(0, Math.min(1, fraction))), 20, 0xFFBAC7C9);
    }
}
