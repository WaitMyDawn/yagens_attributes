package yagen.waitmydawn.gui.mod_operation;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.api.mods.RivenMod;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.player.ClientRenderCache;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

import static yagen.waitmydawn.api.mods.RivenModPool.getDisposition;

public class ModOperationScreen extends AbstractContainerScreen<ModOperationMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/mod_operation/mod_operation.png");
    //button locations
    private static final int OPERATION_BUTTON_X = 81;
    private static final int OPERATION_BUTTON_Y = 41;
    private static final int UPGRADE_BUTTON_X = 63;
    private static final int UPGRADE_BUTTON_Y = 59;
    private static final int POLARITY_BUTTON_X = 98;
    private static final int POLARITY_BUTTON_Y = 59;
    private static final int CYCLE_BUTTON_X = 98;
    private static final int CYCLE_BUTTON_Y = 59;
    //slot indexes (vanilla inventory has 36 slots)
    private static final int ITEM_SLOT = 36 + 0;
    private static final int MOD_SLOT = 36 + 1;
    private static final int EXTRACTION_SLOT = 36 + 2;
    //locations to draw mod icons
    private static final int MOD_BG_X = 133;
    private static final int MOD_BG_Y = 7;
    private static final int MOD_BG_WIDTH = 36;
    private static final int MOD_BG_HEIGHT = 72;

    private static final int LORE_PAGE_X = 176;
    private static final int LORE_PAGE_WIDTH = 80;
    private boolean isDirty;
    protected Button operationButton;
    protected Button upgradeButton;
    protected Button polarityButton;
    protected Button cycleButton;
    private ItemStack lastItemItem = ItemStack.EMPTY;
    protected ArrayList<ModSlotInfo> modSlots;
    private int selectedModIndex = -1;
    private int operationErrorCode = 0;

    public ModOperationScreen(ModOperationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        operationButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169820_) -> this.onOperation()).bounds(0, 0, 14, 14).build()
        );
        upgradeButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169821_) -> this.onUpgrade()).bounds(0, 0, 14, 14).build()
        );
        polarityButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169822_) -> this.onPolarity()).bounds(0, 0, 14, 14).build()
        );
        cycleButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169823_) -> this.onCycle()).bounds(0, 0, 14, 14).build()
        );
        modSlots = new ArrayList<>();
        generateModSlots();
    }

    @Override
    public void onClose() {
        super.onClose();
        resetSelectedMod();
    }

    @Override
    public void render(GuiGraphics guiHelper, int mouseX, int mouseY, float delta) {
        try {
            super.render(guiHelper, mouseX, mouseY, delta);
            renderTooltip(guiHelper, mouseX, mouseY);
        } catch (Exception ignore) {
            System.out.println("Mod operation screen render error:" + ignore);
            onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        operationButton.active = isValidOperation() && operationErrorCode == 0;
        upgradeButton.active = isValidUpgrade() && operationErrorCode == 0;
        polarityButton.active = isValidPolarity() && operationErrorCode == 0;
        cycleButton.active = isValidCycle() && operationErrorCode == 0;
        renderButtons(guiHelper, mouseX, mouseY);

        if (selectedModIndex >= 0 && selectedModIndex < modSlots.size() && modSlots.get(selectedModIndex).hasMod()) {
            renderUpgradeButtons(guiHelper, mouseX, mouseY);
            renderUpgradeInfo(guiHelper, mouseX, mouseY);
        }

        if (selectedModIndex >= 0 && selectedModIndex < modSlots.size() && menu.getModSlot().getItem().is(ItemRegistry.FORMA.get())) {
            renderPolarityButtons(guiHelper, mouseX, mouseY);
        } else if (selectedModIndex >= 0 && selectedModIndex < modSlots.size() && menu.getModSlot().getItem().is(ItemRegistry.MOD.get())) {
            boolean isRiven = IModContainer.get(menu.getModSlot().getItem()).getModAtIndex(0).getMod().getUniqueInfo(1).isEmpty();
            if (isRiven) {
                renderCycleButtons(guiHelper, mouseX, mouseY);
                renderCycleInfo(guiHelper, mouseX, mouseY);
            }
        }


        if (menu.slots.get(ITEM_SLOT).getItem() != lastItemItem) {
            onItemSlotChanged();
            lastItemItem = menu.slots.get(ITEM_SLOT).getItem();
        }

        renderMods(guiHelper, mouseX, mouseY);
        renderLorePage(guiHelper, partialTick, mouseX, mouseY);
        if (isItemSlotted()) renderItemInfo(guiHelper, mouseX, mouseY);

        if (menu.slots.get(ITEM_SLOT).hasItem())
            operationErrorCode = getErrorCode();
        else
            operationErrorCode = 0;

        if (operationErrorCode > 0) {
            guiHelper.blit(TEXTURE, leftPos + 35, topPos + 51, 0, 213, 28, 22);
            if (isHovering(leftPos + 35, topPos + 51, 28, 22, mouseX, mouseY)) {
                guiHelper.renderTooltip(font, getErrorMessage(operationErrorCode), mouseX, mouseY);
            }
        }
    }

    private int getErrorCode() {
        return 0;
    }

    private Component getErrorMessage(int code) {
        if (code == 1)
            return Component.translatable("ui.yagens_attributes.mod_operation_rarity_error");
        else
            return Component.empty();
    }

    private void renderMods(GuiGraphics guiHelper, int mouseX, int mouseY) {
        if (isDirty) {
            generateModSlots();
        }
        Vec2 center = new Vec2(MOD_BG_X + leftPos + MOD_BG_WIDTH / 2, MOD_BG_Y + topPos + MOD_BG_HEIGHT / 2);
        List<String> polarities = new ArrayList<>(ComponentRegistry.getPolarities(menu.getItemSlot().getItem()));
        if (modSlots.size() == 12) {
            center = new Vec2(MOD_BG_X - 9 + leftPos + MOD_BG_WIDTH / 2, MOD_BG_Y + topPos + MOD_BG_HEIGHT / 2);
        }
        for (int i = 0; i < modSlots.size(); i++) {
            var modSlot = modSlots.get(i).button;
            var pos = modSlots.get(i).relativePosition.add(center);
            modSlot.setX((int) pos.x);
            modSlot.setY((int) pos.y);
            String polarity = i < polarities.size() ? polarities.get(i) : "";
            renderModSlot(guiHelper, pos, mouseX, mouseY, i, modSlots.get(i), polarity);
            //modSlot.render(poseStack,mouseX,mouseY,1f);
        }
    }

    private void renderButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        operationButton.setX(leftPos + OPERATION_BUTTON_X);
        operationButton.setY(topPos + OPERATION_BUTTON_Y);
        if (operationButton.active) {
            if (isHovering(operationButton.getX(), operationButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, operationButton.getX(), operationButton.getY(), 28, 184, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, operationButton.getX(), operationButton.getY(), 14, 184, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, operationButton.getX(), operationButton.getY(), 0, 184, 14, 14);
        }
    }

    private void renderUpgradeButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        upgradeButton.setX(leftPos + UPGRADE_BUTTON_X);
        upgradeButton.setY(topPos + UPGRADE_BUTTON_Y);
        if (upgradeButton.active) {
            if (isHovering(upgradeButton.getX(), upgradeButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, upgradeButton.getX(), upgradeButton.getY(), 28, 198, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, upgradeButton.getX(), upgradeButton.getY(), 14, 198, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, upgradeButton.getX(), upgradeButton.getY(), 0, 198, 14, 14);
        }
    }

    private void renderPolarityButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        polarityButton.setX(leftPos + POLARITY_BUTTON_X);
        polarityButton.setY(topPos + POLARITY_BUTTON_Y);
        if (polarityButton.active) {
            if (isHovering(polarityButton.getX(), polarityButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, polarityButton.getX(), polarityButton.getY(), 28, 212, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, polarityButton.getX(), polarityButton.getY(), 14, 212, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, polarityButton.getX(), polarityButton.getY(), 0, 212, 14, 14);
        }
    }

    private void renderCycleButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        cycleButton.setX(leftPos + CYCLE_BUTTON_X);
        cycleButton.setY(topPos + CYCLE_BUTTON_Y);
        if (cycleButton.active) {
            if (isHovering(cycleButton.getX(), cycleButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, cycleButton.getX(), cycleButton.getY(), 28, 226, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, cycleButton.getX(), cycleButton.getY(), 14, 226, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, cycleButton.getX(), cycleButton.getY(), 0, 226, 14, 14);
        }
    }

    private void renderItemInfo(GuiGraphics guiHelper, int mouseX, int mouseY) {
        var poseStack = guiHelper.pose();
        float scale = 0.8F;

        ComponentRegistry.UpgradeData data = ComponentRegistry.getUpgrade(menu.getItemSlot().getItem());
        Component itemInfo = Component.translatable(
                "ui.yagens_attributes.item_level_polarity",
                data.level(), data.level() * 2 - getAllModCost(), data.level() * 2
        ).withStyle(ChatFormatting.GRAY);

        Component itemExpInfo1 = Component.translatable(
                "ui.yagens_attributes.item_exp_info1",
                data.exp()
        ).withStyle(ChatFormatting.GRAY);
        Component itemExpInfo2 = Component.translatable(
                "ui.yagens_attributes.item_exp_info2",
                data.nextLevelExpNeed()
        ).withStyle(ChatFormatting.GRAY);
        Component itemMaxLevel = Component.translatable("ui.yagens_attributes.item_max_level")
                .withStyle(ChatFormatting.GRAY);

        //int lineWidth = font.width(itemInfo);
        int lineX = (int) ((leftPos + 7) / scale);
        int lineY = (int) ((topPos + 15) / scale);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        guiHelper.drawString(font, itemInfo, lineX, lineY, 0xFFFFFF, true);
        lineY += (int) (font.lineHeight * scale + 2);
        if (data.level() >= 30) {
            guiHelper.drawString(font, itemMaxLevel, lineX, lineY, 0xFFFFFF, true);
        } else {
            guiHelper.drawString(font, itemExpInfo1, lineX, lineY, 0xFFFFFF, true);
            lineY += (int) (font.lineHeight * scale + 2);
            guiHelper.drawString(font, itemExpInfo2, lineX, lineY, 0xFFFFFF, true);
        }
        poseStack.popPose();
    }

    private int getAllModCost() {
        int total = 0;
        List<String> polarities = new ArrayList<>(ComponentRegistry.getPolarities(menu.getItemSlot().getItem()));
        int slotCount = modSlots.size();
        while (polarities.size() < slotCount) {
            polarities.add("");
        }
        for (var slotInfo : modSlots) {
            if (!slotInfo.hasMod()) continue;
            float multiply = 1f;
            String polarity = polarities.get(slotInfo.modSlot.index());

            String modPolarity = slotInfo.modSlot.getMod().getModPolarity();
            if (slotInfo.modSlot.getMod().getUniqueInfo(1).isEmpty()) {
                modPolarity = menu.getItemSlot().getItem().get(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
            }
            if (polarity.equals(modPolarity)) multiply = 0.5f;
            else if (!polarity.isEmpty()) multiply = 1.25f;
            total = total + (int) Math.ceil((slotInfo.modSlot.getMod().getBaseCapacityCost() + slotInfo.modSlot.getLevel()) * multiply - 0.001);
        }
        return total;
    }

    private void renderUpgradeInfo(GuiGraphics guiHelper, int mouseX, int mouseY) {
        if (selectedModIndex < 0 || selectedModIndex >= modSlots.size() || !modSlots.get(selectedModIndex).hasMod())
            return;
        int modEssenceCost = getModEssenceCost(modSlots.get(selectedModIndex).modSlot);
        int modEssenceCount = getPlayerModEssenceCount();

        var poseStack = guiHelper.pose();
        float scale = 0.8F;
        float reverse = 1.0F / scale;

        Component needHave = Component.translatable(
                "ui.yagens_attributes.upgrade_need_have",
                modEssenceCost, modEssenceCount
        ).withStyle(modEssenceCount >= modEssenceCost
                ? ChatFormatting.GREEN : ChatFormatting.RED);
        int lineWidth = font.width(needHave);
        int lineX = leftPos + 7 + (int) ((54 * reverse - lineWidth) / 2); // left width
        int lineY = topPos + UPGRADE_BUTTON_Y + (int) ((14 - font.lineHeight) * reverse / 2);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        guiHelper.drawString(font, needHave, (int) (lineX / scale), (int) (lineY / scale), 0xFFFFFF, true);
        poseStack.popPose();
    }

    private void renderCycleInfo(GuiGraphics guiHelper, int mouseX, int mouseY) {
        int kuvaCount = getPlayerKuvaCount();

        var poseStack = guiHelper.pose();
        float scale = 0.8F;
        float reverse = 1.0F / scale;

        Component needHave = Component.translatable(
                "ui.yagens_attributes.cycle_need_have",
                4, kuvaCount
        ).withStyle(kuvaCount >= 4
                ? ChatFormatting.GREEN : ChatFormatting.RED);
        int lineWidth = font.width(needHave);
        int lineX = leftPos + 7 + (int) ((64 * reverse - lineWidth) / 2); // left width
        int lineY = topPos + OPERATION_BUTTON_Y + (int) ((14 - font.lineHeight) * reverse / 2);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        guiHelper.drawString(font, needHave, (int) (lineX / scale), (int) (lineY / scale), 0xFFFFFF, true);
        poseStack.popPose();
    }

    private void renderModSlot(GuiGraphics guiHelper, Vec2 pos, int mouseX, int mouseY, int index, ModSlotInfo slot, String polarity) {
        //setTexture(TEXTURE);
        boolean hovering = isHovering((int) pos.x, (int) pos.y, 18, 18, mouseX, mouseY);
        int iconToDrawX = 18;
        int iconToDrawY = 166;
        if (polarity.isEmpty()) {
            iconToDrawX = hovering ? 36 : 18;
        } else {
            switch (polarity) {
                case "Cth" -> {
                    iconToDrawX = hovering ? 238 : 220;
                }
                case "Nya" -> {
                    iconToDrawX = hovering ? 238 : 220;
                    iconToDrawY += 18;
                }
                case "Nod" -> {
                    iconToDrawX = hovering ? 238 : 220;
                    iconToDrawY += 36;
                }
            }
        }

        guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, iconToDrawX, iconToDrawY, 18, 18);
        if (slot.hasMod()) {
            drawModIcon(guiHelper, pos, slot);
            if (hovering && !slot.modSlot.modData().canRemove())
                guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, 72, iconToDrawY, 18, 18);
            //guiHelper.blit(slot.modSlot.getMod().getModIconResource(), (int) pos.x + 1, (int) pos.y + 1, 0, 0, 16, 16, 16, 16);

        }
        if (index == selectedModIndex)
            guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, 54, 166, 18, 18);
    }

    private void drawModIcon(GuiGraphics guiHelper, Vec2 pos, ModSlotInfo slot) {
        //setTexture(slot.containedMod.getModType().getResourceLocation());
        guiHelper.blit(slot.modSlot.getMod().getModIconResource(), (int) pos.x + 1, (int) pos.y + 1, 0, 0, 16, 16, 16, 16);
    }

    private final int LINES_PER_PAGE = 9;
    private int currentPage = 0;
    private List<FormattedCharSequence> lines = List.of();

    private void renderLorePage(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + LORE_PAGE_X;
        int y = topPos;
        var textColor = Style.EMPTY.withColor(0x000000);
//        var poseStack = guiHelper.pose();
        //
        // Title
        //
        boolean modSelectd = selectedModIndex >= 0 && selectedModIndex < modSlots.size() && modSlots.get(selectedModIndex).hasMod();
        var title = selectedModIndex < 0
                ? Component.translatable("ui.yagens_attributes.no_selection") : modSelectd
                ? modSlots.get(selectedModIndex).modSlot.getMod().getDisplayName(Minecraft.getInstance().player)
                : Component.translatable("ui.yagens_attributes.empty_slot");

        var titleLines = font.split(title.withStyle(ChatFormatting.UNDERLINE).withStyle(textColor), LORE_PAGE_WIDTH);
        int titleY = topPos + 10;

        for (FormattedCharSequence line : titleLines) {
            int titleWidth = font.width(line);
            int titleX = x + (LORE_PAGE_WIDTH - titleWidth) / 2;
            guiHelper.drawString(font, line, titleX, titleY, 0xFFFFFF, false);

            titleY += font.lineHeight;
        }

        if (selectedModIndex < 0 || selectedModIndex >= modSlots.size() || !modSlots.get(selectedModIndex).hasMod()) {
            return;
        }

        var mod = modSlots.get(selectedModIndex).modSlot.getMod();
        var modLevel = modSlots.get(selectedModIndex).modSlot.getLevel();
        int descLine = titleY + 2;

        String modPolarity = mod.getModPolarity();
        double disposition = 0.01;

        if (mod.getUniqueInfo(modLevel).isEmpty()) {
            modPolarity = menu.getItemSlot().getItem().getOrDefault(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), "Riven");
            disposition = getDisposition(menu.getItemSlot().getItem().getItem());
        }
        Component uiModInfo1 = Component.translatable("tooltip.yagens_attributes.polarity", modPolarity).withStyle(ChatFormatting.GOLD)
                .append(" ")
                .append(Component.translatable("tooltip.yagens_attributes.rarity", mod.getRarity().getDisplayName()).withStyle(mod.getRarity().getDisplayName().getStyle()));

        Component uiModInfo2 = Component.translatable("tooltip.yagens_attributes.level", modLevel).withStyle(ChatFormatting.GRAY)
                .append(" ")
                .append(Component.translatable("tooltip.yagens_attributes.capacity_cost", mod.getBaseCapacityCost() + modLevel).withStyle(ChatFormatting.RED));
        // disposition
        Component uiModInfo3 = Component.translatable("tooltip.yagens_attributes.disposition", (float) disposition).withStyle(Style.EMPTY.withColor(0x8A2BE2));
        //
        //  Mod Info
        //
        drawTextWithShadow(font, guiHelper, uiModInfo1, x + (LORE_PAGE_WIDTH - font.width(uiModInfo1)) / 2, descLine, 0xFFFFFF, 1);
        descLine += font.lineHeight;

        drawTextWithShadow(font, guiHelper, uiModInfo2, x + (LORE_PAGE_WIDTH - font.width(uiModInfo2)) / 2, descLine, 0xFFFFFF, 1);
        descLine += font.lineHeight;

        if (disposition != 0.01) {
            drawTextWithShadow(font, guiHelper, uiModInfo3, x + (LORE_PAGE_WIDTH - font.width(uiModInfo3)) / 2, descLine, 0xFFFFFF, 1);
            descLine += font.lineHeight;
        }

        //
        //  Unique Info
        //
        List<MutableComponent> uniqueInfo;
        if (mod.getUniqueInfo(modLevel).isEmpty()) {
            uniqueInfo= RivenMod.getRivenUniqueInfo(menu.slots.get(ITEM_SLOT).getItem(), modLevel);
        } else {
            uniqueInfo = mod.getUniqueInfo(modLevel);
        }
        FormattedText wholeText = uniqueInfo.stream()
                .map(line -> Component.literal("• ").append(line))
                .reduce(Component.empty(),
                        (a, b) -> a.equals(Component.empty()) ? b : a.copy().append("\n").append(b));

        lines = font.split(wholeText, LORE_PAGE_WIDTH - 4);

        for (int i = currentPage * LINES_PER_PAGE; i < Math.min(lines.size(), (currentPage + 1) * LINES_PER_PAGE); i++) {
            guiHelper.drawString(font, lines.get(i), x + 2, descLine, textColor.getColor().getValue(), false);
            descLine += font.lineHeight;
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int areaX0 = leftPos + LORE_PAGE_X;
        int areaY0 = topPos + 50;
        int areaX1 = areaX0 + LORE_PAGE_WIDTH;
        int areaY1 = areaY0 + LINES_PER_PAGE * font.lineHeight;
        if (mouseX >= areaX0 && mouseX <= areaX1 &&
                mouseY >= areaY0 && mouseY <= areaY1) {

            int maxPage = Math.max(0, (lines.size() - 1) / LINES_PER_PAGE);
            if (deltaY > 0)
                currentPage = Math.max(0, currentPage - 1);
            else if (deltaY < 0)
                currentPage = Math.min(maxPage, currentPage + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void drawTextWithShadow(Font font, GuiGraphics guiHelper, Component text, int x, int y, int color, float scale) {
        x /= scale;
        y /= scale;
        guiHelper.drawString(font, text, x, y, color);
    }

    private int drawText(Font font, GuiGraphics guiHelper, Component text, int x, int y, int color, float scale) {
        x /= scale;
        y /= scale;
        guiHelper.drawWordWrap(font, text, x, y, LORE_PAGE_WIDTH, color);
        return font.wordWrapHeight(text, LORE_PAGE_WIDTH);

    }

    private int drawStatText(Font font, GuiGraphics guiHelper, int x, int y, String translationKey, Style textStyle, MutableComponent stat, Style statStyle, float scale) {
//        x /= scale;
//        y /= scale;
        return drawText(font, guiHelper, Component.translatable(translationKey, stat.withStyle(statStyle)).withStyle(textStyle), x, y, 0xFFFFFF, scale);
    }

    private void generateModSlots() {
        /*
         Reset Mods of Item
         */
        for (ModSlotInfo s : modSlots)
            removeWidget(s.button);
        modSlots.clear();
        if (!isItemSlotted())
            return;

        var itemSlot = menu.slots.get(ITEM_SLOT);
        var itemItemStack = itemSlot.getItem();

        var itemContainer = IModContainer.get(itemItemStack);
        if (itemContainer == null) {
            return;
        }

        var storedMods = itemContainer.getAllMods();
        int modCount = itemContainer.getMaxModCount();
        if (modCount > 12) {
            modCount = 12;
        }
        if (modCount <= 0) {
            return;
        }
        /*
         Calculate and save mod slot positions on the screen
         */
        int boxSize = 18;
        int[] rowCounts = ClientRenderCache.getRowCounts(modCount);
        int[] row1 = new int[rowCounts[0]];
        int[] row2 = new int[rowCounts[1]];
        int[] row3 = new int[rowCounts[2]];
        int[] row4 = new int[rowCounts[3]];

        int[] rowWidth = {
                boxSize * row1.length,
                boxSize * row2.length,
                boxSize * row3.length,
                boxSize * row4.length

        };
        int[] rowHeight = {
                row1.length > 0 ? boxSize : 0,
                row2.length > 0 ? boxSize : 0,
                row3.length > 0 ? boxSize : 0,
                row4.length > 0 ? boxSize : 0
        };

        int overallHeight = rowHeight[0] + rowHeight[1] + rowHeight[2] + rowHeight[3];


        int[][] display = {row1, row2, row3, row4};
        int index = 0;
        for (int row = 0; row < display.length; row++) {
            for (int column = 0; column < display[row].length; column++) {
                int offset = -rowWidth[row] / 2;
                Vec2 location = new Vec2(offset + column * boxSize, (row) * boxSize - (overallHeight / 2));
                location.add(-9);
                int temp_index = index;
                modSlots.add(new ModSlotInfo(storedMods[index],
                        location,
                        this.addWidget(
                                Button.builder(Component.translatable(temp_index + ""), (p_169820_) -> this.setSelectedIndex(temp_index)).pos((int) location.x, (int) location.y).size(boxSize, boxSize).build()
                        )
                ));
                index++;
            }
        }
        /*
         Unflag as Dirty
         */
        isDirty = false;
    }

    private void onItemSlotChanged() {
        isDirty = true;
        var itemStack = menu.slots.get(ITEM_SLOT).getItem();
        if (IModContainer.isModContainer(itemStack)) {
            var itemContainer = IModContainer.get(itemStack);
            if (itemContainer.getMaxModCount() <= selectedModIndex) {
                resetSelectedMod();
            }
        } else {
            resetSelectedMod();
        }
    }

    private void onOperation() {
        if (IModContainer.isModContainer(menu.getItemSlot().getItem()) && menu.getModSlot().getItem().getItem() instanceof Mod mod) {
            if (modSlots.isEmpty())
                return;

            var modContainer = IModContainer.get(menu.getModSlot().getItem());
            var modSlot = modContainer.getModAtIndex(0);

            boolean isHomo = modSlot.getMod().getModName().contains("general_mod");
            switch (ModCompat.validLocation(menu.getItemSlot().getItem().getItem())) {
                case 1 -> {
                    if (modSlot.getMod().getModName().contains("tool_mod")) isHomo = true;
                }
                case 2 -> {
                    if (modSlot.getMod().getModName().contains("armor_mod")) isHomo = true;
                }
                case 3 -> {
                    if (modSlot.getMod().getModName().contains("shield_mod")) isHomo = true;
                }
            }
            if (!isHomo) return;

            //  Quick OPERATION
            if (selectedModIndex < 0 || modSlots.get(selectedModIndex).hasMod()) {
                for (int i = selectedModIndex + 1; i < modSlots.size(); i++) {
                    if (!modSlots.get(i).hasMod()) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }
            //sanitize
            setSelectedIndex(Mth.clamp(selectedModIndex, 0, modSlots.size() - 1));
            //  Is this slot already taken?
            if (modSlots.get(selectedModIndex).hasMod()) {
                return;
            }
            //
            //  Good to OPERATION
            //

            isDirty = true;
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
        }
    }

    private void setSelectedIndex(int index) {
        selectedModIndex = index;
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);

//        Messages.sendToServer(new ServerboundModOperationSelectMod(this.menu.blockEntity.getBlockPos(), selectedModIndex));
    }

    private void onUpgrade() {
        if (!isValidUpgrade()) return;

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -2);
    }

    private void onPolarity() {
        if (!isValidPolarity()) return;

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -3);
    }

    private void onCycle() {
        if (!isValidCycle()) return;

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -4);
    }

    private void resetSelectedMod() {
        setSelectedIndex(-1);
    }

    private boolean isValidOperation() {
        if (!isItemSlotted() || !isModSlotted()) return false;
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) return true;

        ComponentRegistry.UpgradeData data = ComponentRegistry.getUpgrade(menu.getItemSlot().getItem());
        var modContainer = IModContainer.get(menu.getModSlot().getItem());
        var modSlot = modContainer.getModAtIndex(0);
        int capacity = data.level() * 2;
        int cost = getAllModCost();
        int newModCost = modSlot.getLevel() + modSlot.getMod().getBaseCapacityCost();
        List<String> polarities = new ArrayList<>(ComponentRegistry.getPolarities(menu.getItemSlot().getItem()));
        float multiply = 1f;
        if (selectedModIndex < 0 || selectedModIndex >= polarities.size()) return false;
        String polarity = polarities.get(selectedModIndex);

        String modPolarity = modSlot.getMod().getModPolarity();
        if (modSlot.getMod().getUniqueInfo(1).isEmpty()) {
            modPolarity = menu.getModSlot().getItem().get(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
        }

        if (polarity.equals(modPolarity)) multiply = 0.5f;
        else if (!polarity.isEmpty()) multiply = 1.25f;
        cost = cost + (int) Math.ceil(newModCost * multiply - 0.001);
        return cost <= capacity;
    }

    private boolean isValidUpgrade() {
        if (selectedModIndex < 0 || selectedModIndex >= modSlots.size()) return false;
        if (!modSlots.get(selectedModIndex).hasMod()) return false;
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) return true;
        int cost = getModEssenceCost(modSlots.get(selectedModIndex).modSlot);
        int count = getPlayerModEssenceCount();
        return count >= cost;
    }

    private boolean isValidPolarity() {
        if (selectedModIndex < 0 || selectedModIndex >= modSlots.size()) return false;
        ComponentRegistry.UpgradeData data = ComponentRegistry.getUpgrade(menu.getItemSlot().getItem());
        if (Minecraft.getInstance().player != null && menu.getModSlot().getItem().is(ItemRegistry.FORMA.get()) && Minecraft.getInstance().player.isCreative())
            return true;
        return menu.getModSlot().getItem().is(ItemRegistry.FORMA.get()) && data.level() >= 30;
    }

    private boolean isValidCycle() {
        if (!menu.getModSlot().getItem().is(ItemRegistry.MOD.get())) return false;
        boolean isRiven = IModContainer.get(menu.getModSlot().getItem()).getModAtIndex(0).getMod().getUniqueInfo(1).isEmpty();
        if (!isRiven) return false;
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) return true;
        return getPlayerKuvaCount() >= 4;
    }

    private int getModEssenceCost(ModSlot modSlot) {
        var modRarity = modSlot.getMod().getRarity();
        var modLevel = modSlot.getLevel();
        if (modLevel == modSlot.getMod().getMaxLevel()) return 0;
        return (int) ((modRarity.getValue() + 1) * Math.pow(2, modLevel - 1));
    }

    private int getPlayerModEssenceCount() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ItemRegistry.MOD_ESSENCE.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int getPlayerKuvaCount() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ItemRegistry.KUVA.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean isValidExtraction() {
        return selectedModIndex >= 0 && modSlots.get(selectedModIndex).hasMod() && !menu.slots.get(EXTRACTION_SLOT).hasItem();
    }

    private boolean isItemSlotted() {
        return IModContainer.isModContainer(menu.slots.get(ITEM_SLOT).getItem());
    }

    private boolean isModSlotted() {
        //is "hasItem" necessary? at what point does null break this?
        return menu.slots.get(MOD_SLOT).hasItem() && menu.slots.get(MOD_SLOT).getItem().getItem() instanceof Mod;
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private final int[][] LAYOUT = ClientRenderCache.MOD_LAYOUT;

    private static class ModSlotInfo {
        public ModSlot modSlot;
        public Vec2 relativePosition;
        public Button button;

        ModSlotInfo(ModSlot modSlot, Vec2 relativePosition, Button button) {
            this.modSlot = modSlot;
            this.relativePosition = relativePosition;
            this.button = button;
        }

        public boolean hasMod() {
            return modSlot != null; //&& !modSlot.modData().equals(ModData.EMPTY);
        }
    }

}