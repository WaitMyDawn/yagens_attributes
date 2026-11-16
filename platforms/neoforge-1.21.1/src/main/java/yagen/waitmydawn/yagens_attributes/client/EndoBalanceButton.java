package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;
import java.util.Locale;

public final class EndoBalanceButton extends AbstractButton {
    private final Runnable action;
    private final ItemStack icon;
    private final PanelGeometry.Rect bounds;
    private final boolean kuva;
    private long balance = -1;
    private String formatted = "0";
    public EndoBalanceButton(Runnable action) {
        this(PanelGeometry.ENDO, false, action);
    }
    public EndoBalanceButton(PanelGeometry.Rect bounds, boolean kuva, Runnable action) {
        super(bounds.x(), bounds.y(), bounds.width(), bounds.height(), Component.empty());
        this.bounds = bounds; this.kuva = kuva; this.icon = new ItemStack(kuva ? ModItems.KUVA.get() : ModItems.ENDO.get());
        this.action = action; balance(0);
    }
    public void balance(long value) {
        if (balance == value) return;
        balance = value; formatted = String.format(Locale.ROOT, "%,d", value);
        setMessage(Component.translatable("gui.yagens_attributes." + (kuva ? "kuva_balance" : "endo_balance"), value));
    }
    @Override public void onPress() { action.run(); }
    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var canvas = new PanelCanvas(graphics, Minecraft.getInstance().font);
        PanelPainter.frame(canvas, bounds, 0xD0111D26, isHoveredOrFocused() ? PanelPainter.TEAL : 0xFF607883, 4);
        graphics.renderItem(icon, getX() + 5, getY() + 4);
        canvas.text(formatted, getX() + 27, getY() + 7, 10, active ? PanelPainter.GOLD : PanelPainter.MUTED, width - 34);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
