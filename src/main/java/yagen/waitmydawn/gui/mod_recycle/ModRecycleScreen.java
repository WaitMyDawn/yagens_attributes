package yagen.waitmydawn.gui.mod_recycle;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class ModRecycleScreen extends AbstractContainerScreen<ModRecycleMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/mod_recycle/mod_recycle.png");

    private static final int RECYCLE_BUTTON_X = 83;
    private static final int RECYCLE_BUTTON_Y = 37;
    private static final int TRANSFORM_BUTTON_X = 83;
    private static final int TRANSFORM_BUTTON_Y = 55;

    protected Button recycleButton;
    protected Button transformButton;

    public ModRecycleScreen(ModRecycleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
    }

    @Override
    protected void init() {
        super.init();
        recycleButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169824_) -> this.onRecycle()).bounds(0, 0, 14, 14).build()
        );
        transformButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169825_) -> this.onTransform()).bounds(0, 0, 14, 14).build()
        );
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiHelper, int mouseX, int mouseY, float delta) {
        try {
            super.render(guiHelper, mouseX, mouseY, delta);
            renderTooltip(guiHelper, mouseX, mouseY);
        } catch (Exception ignore) {
            onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        recycleButton.active = isValidRecycle();
        transformButton.active = isValidTransform();
        renderRecycleButtons(guiHelper, mouseX, mouseY);
        renderTransformButtons(guiHelper, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                8, this.imageHeight - 94, 0x404040, false);
    }

    private void onRecycle() {
        if (!isValidRecycle()) return;

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
    }

    private void onTransform() {
        if (!isValidTransform()) return;

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -2);
    }

    private void renderRecycleButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        recycleButton.setX(leftPos + RECYCLE_BUTTON_X);
        recycleButton.setY(topPos + RECYCLE_BUTTON_Y);
        if (recycleButton.active) {
            if (isHovering(recycleButton.getX(), recycleButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, recycleButton.getX(), recycleButton.getY(), 28, 184, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, recycleButton.getX(), recycleButton.getY(), 14, 184, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, recycleButton.getX(), recycleButton.getY(), 0, 184, 14, 14);
        }
    }

    private void renderTransformButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        transformButton.setX(leftPos + TRANSFORM_BUTTON_X);
        transformButton.setY(topPos + TRANSFORM_BUTTON_Y);
        if (transformButton.active) {
            if (isHovering(transformButton.getX(), transformButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, transformButton.getX(), transformButton.getY(), 28, 198, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, transformButton.getX(), transformButton.getY(), 14, 198, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, transformButton.getX(), transformButton.getY(), 0, 198, 14, 14);
        }
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private boolean isValidRecycle() {
        for (Slot slot : menu.getModSlots())
            if (slot != null && slot.hasItem() && slot.getItem().is(ItemRegistry.MOD.get()))
                return true;
        return false;
    }

    private boolean isValidTransform() {
        boolean hasEmptyResult = false;
        for (Slot slot : menu.getResultSlots()) {
            if (slot == null || !slot.hasItem()) {
                hasEmptyResult = true;
                break;
            }
        }
        if (!hasEmptyResult) return false;

        int modCount = 0;
        for (Slot slot : menu.getModSlots()) {
            if (slot != null && slot.hasItem() && slot.getItem().is(ItemRegistry.MOD.get())) {
                modCount += slot.getItem().getCount();
                if (modCount >= 4) break;
            }
        }
        return modCount >= 4;
    }

}
