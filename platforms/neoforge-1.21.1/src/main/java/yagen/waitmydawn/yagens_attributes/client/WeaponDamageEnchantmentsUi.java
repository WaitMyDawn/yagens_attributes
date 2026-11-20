package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.ui.PanelPainter;
import yagen.waitmydawn.yagens_attributes.data.MeleeEnchantments;

import java.util.List;

final class WeaponDamageEnchantmentsUi {
    private static final ResourceLocation VILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private static final ResourceLocation ZOMBIE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private static final ResourceLocation SPIDER = ResourceLocation.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final ResourceLocation SPIDER_EYES = ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png");

    private WeaponDamageEnchantmentsUi() {}

    static Component render(PanelCanvas canvas, String label, String damage, List<MeleeEnchantments.Preview> bonuses,
                            int y, int mouseX, int mouseY) {
        var font = canvas.font();
        var graphics = canvas.graphics();
        int labelWidth = Math.min(123, (int) Math.ceil(font.width(label) * 10F / 9F));
        canvas.text(label, 32, y, 10, PanelPainter.MUTED, labelWidth);
        int damageWidth = font.width(damage);
        int closeWidth = font.width(")");
        var tokens = bonuses.stream().map(bonus -> "(+" + StatText.number(bonus.amount())).toList();
        int groupWidth = damageWidth;
        for (String token : tokens) groupWidth += 4 + font.width(token) + 9 + closeWidth;
        float scale = Math.min(1F, (216 - (32 + labelWidth + 6)) / (float) groupWidth);
        float startX = Math.min(162, 216 - groupWidth * scale);
        float top = y + 1;
        graphics.pose().pushPose();
        graphics.pose().translate(startX, top, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, damage, 0, 0, PanelPainter.GOLD, false);
        int x = damageWidth;
        Component hovered = null;
        for (int i = 0; i < bonuses.size(); i++) {
            var bonus = bonuses.get(i);
            var token = tokens.get(i);
            int left = x += 4;
            graphics.drawString(font, token, x, 0, PanelPainter.TEXT, false);
            x += font.width(token);
            face(graphics, bonus.kind(), x + 1);
            x += 9;
            graphics.drawString(font, ")", x, 0, PanelPainter.TEXT, false);
            x += closeWidth;
            if (mouseX >= startX + left * scale && mouseX < startX + x * scale && mouseY >= top - 1 && mouseY < top + 10 * scale)
                hovered = Component.translatable(bonus.kind().key().location().toLanguageKey("enchantment"));
        }
        graphics.pose().popPose();
        return hovered;
    }

    private static void face(GuiGraphics graphics, MeleeEnchantments.Kind kind, int x) {
        switch (kind) {
            case SHARPNESS -> {
                graphics.blit(VILLAGER, x, -1, 8, 10, 8F, 8F, 8, 10, 64, 64);
                graphics.blit(VILLAGER, x + 3, 4, 2, 4, 26F, 2F, 2, 4, 64, 64);
            }
            case SMITE -> graphics.blit(ZOMBIE, x, 0, 8, 8, 8F, 8F, 8, 8, 64, 64);
            case BANE_OF_ARTHROPODS -> {
                graphics.blit(SPIDER, x, 0, 8, 8, 40F, 12F, 8, 8, 64, 32);
                graphics.blit(SPIDER_EYES, x, 0, 8, 8, 40F, 12F, 8, 8, 64, 32);
            }
        }
    }
}
