package yagen.waitmydawn.gui.reservoirs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReservoirsScreen extends AbstractContainerScreen<ReservoirsMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/reservoirs/reservoirs_table.png");

    private static final int ATTRIBUTE_START_X = 68;
    private static final int ATTRIBUTE_START_Y = 55;
    private static final int ATTRIBUTE_WIDTH = 11;
    private static final int ATTRIBUTE_HEIGHT = 6;
    private static final int ATTRIBUTE_DISTANCE = 15;


    public ReservoirsScreen(ReservoirsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        renderReservoirsAttributes(guiGraphics, mouseX, mouseY);
    }

    private void renderReservoirsAttributes(GuiGraphics guiHelper, int mouseX, int mouseY) {
        ComponentRegistry.ReservoirsAttributes reservoirsAttributes =
                this.menu.getItemStack().get(ComponentRegistry.RESERVOIRS_ATTRIBUTES.get());
        if (reservoirsAttributes == null) return;
        int duration = (int) Math.round(reservoirsAttributes.duration());
        int strength = (int) Math.round(reservoirsAttributes.strength());
        int range = (int) Math.round(reservoirsAttributes.range());
        if (duration > 0) {
            guiHelper.blit(TEXTURE, leftPos + ATTRIBUTE_START_X, topPos + ATTRIBUTE_START_Y,
                    (duration - 1) * ATTRIBUTE_WIDTH, this.imageHeight, ATTRIBUTE_WIDTH, ATTRIBUTE_HEIGHT);
        }
        if (strength > 0) {
            guiHelper.blit(TEXTURE, leftPos + ATTRIBUTE_START_X + ATTRIBUTE_DISTANCE, topPos + ATTRIBUTE_START_Y,
                    (strength - 1) * ATTRIBUTE_WIDTH, this.imageHeight + ATTRIBUTE_HEIGHT, ATTRIBUTE_WIDTH, ATTRIBUTE_HEIGHT);
        }
        if (range > 0) {
            guiHelper.blit(TEXTURE, leftPos + ATTRIBUTE_START_X + ATTRIBUTE_DISTANCE * 2, topPos + ATTRIBUTE_START_Y,
                    (range - 1) * ATTRIBUTE_WIDTH, this.imageHeight + ATTRIBUTE_HEIGHT * 2, ATTRIBUTE_WIDTH, ATTRIBUTE_HEIGHT);
        }
        if (isHovering(leftPos + ATTRIBUTE_START_X, topPos + ATTRIBUTE_START_Y,
                ATTRIBUTE_DISTANCE * 2 + ATTRIBUTE_WIDTH, ATTRIBUTE_HEIGHT, mouseX, mouseY)) {
            Component lineB = Component.translatable("tooltip.yagens_attributes.reservoirs.2"
                    , String.format("%.1f", 6 * reservoirsAttributes.duration())).withStyle(ChatFormatting.BLUE);
            Component lineR = Component.translatable("tooltip.yagens_attributes.reservoirs.1"
                    , String.format("%.1f", 6 * reservoirsAttributes.strength())).withStyle(ChatFormatting.RED);
            Component lineG = Component.translatable("tooltip.yagens_attributes.reservoirs.3"
                    , String.format("%.1f", 20 * reservoirsAttributes.range())).withStyle(ChatFormatting.GREEN);
            List<Component> lines = Arrays.asList(lineB, lineR, lineG);
            guiHelper.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
