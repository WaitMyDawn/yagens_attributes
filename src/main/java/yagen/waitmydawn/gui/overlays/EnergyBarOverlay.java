package yagen.waitmydawn.gui.overlays;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

public class EnergyBarOverlay implements LayeredDraw.Layer {
    public static final EnergyBarOverlay instance = new EnergyBarOverlay();
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/energy_bar.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 18;

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        if (!shouldShowEnergyBar(player))
            return;

        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
        Font font = Minecraft.getInstance().font;

        int barX = ClientConfigs.ENERGY_BAR_X.get() == -1 ? (screenWidth - 120) : ClientConfigs.ENERGY_BAR_X.get();
        int barY = ClientConfigs.ENERGY_BAR_Y.get() == -1 ? (screenHeight - 30) : ClientConfigs.ENERGY_BAR_Y.get();

        double maxEnergy = player.getAttributeValue(YAttributes.MAX_ENERGY);
        double energy = DataAttachmentRegistry.getEnergy(player);
        int curWidth = (int) (energy / maxEnergy * WIDTH);
        Component text = Component.literal(String.format("%d", (int) energy));

        guiHelper.blit(TEXTURE, barX, barY, 0, HEIGHT / 2, curWidth, HEIGHT/2, WIDTH, HEIGHT);
        guiHelper.blit(TEXTURE, barX + curWidth, barY, curWidth, 0, WIDTH - curWidth, HEIGHT / 2, WIDTH, HEIGHT);
        guiHelper.drawString(font, text, barX + (WIDTH - font.width(text)) / 2, barY + (HEIGHT / 2 - font.lineHeight) / 2, 0xFFFFFF);
    }

    public static boolean shouldShowEnergyBar(Player player) {
        boolean isValidWarframe = ModCompat.isValidWarframeAbility(player.getItemBySlot(EquipmentSlot.CHEST));
        boolean maxEnergy = DataAttachmentRegistry.getEnergy(player) >= player.getAttributeValue(YAttributes.MAX_ENERGY);
        return isValidWarframe && !maxEnergy;
    }
}
