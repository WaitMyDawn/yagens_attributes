package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.network.ClientPackets;
import yagen.waitmydawn.yagens_attributes.text.ModText;

import java.util.*;

/** Read-only view of the synchronized rules for one Riven's exact weapon binding. */
public final class RivenRulesScreen extends Screen {
    private final ModPanelScreen parent;
    private final ModInfo info;
    private RivenRules rules;
    private WeaponProfile profile;
    private List<RivenRules.Factors> layouts = List.of();
    private final Map<RivenRules.Factors, RivenAnalysis.Result> analyses = new HashMap<>();
    private final List<PanelButton> layoutButtons = new ArrayList<>();
    private RivenAnalysis.Result result;
    private int layoutIndex, firstRow;
    private boolean scrolling;
    private String error = "rules_unavailable";

    public RivenRulesScreen(ModPanelScreen parent, ModInfo info) {
        super(text("rules_title"));
        this.parent = Objects.requireNonNull(parent); this.info = Objects.requireNonNull(info);
        Objects.requireNonNull(info.riven());
        readData();
    }

    private static Component text(String key, Object... args) { return Component.translatable("gui.yagens_attributes." + key, args); }
    private double scale() { return Math.min(width / (double) PanelGeometry.WIDTH, height / (double) PanelGeometry.HEIGHT); }
    private double originX() { return (width - PanelGeometry.WIDTH * scale()) / 2; }
    private double originY() { return (height - PanelGeometry.HEIGHT * scale()) / 2; }
    private double logicalX(double x) { return (x - originX()) / scale(); }
    private double logicalY(double y) { return (y - originY()) / scale(); }
    private int visibleRows() { return PanelGeometry.RIVEN_RULE_LIST.height() / PanelGeometry.RIVEN_RULE_ROW_HEIGHT; }
    private int rowCount() { return result == null ? 0 : result.outcomes().size(); }
    private int maximumScroll() { return Math.max(0, rowCount() - visibleRows()); }
    private RivenRules.Factors layout() { return layouts.isEmpty() ? null : layouts.get(layoutIndex); }

    private boolean readData() {
        var nextRules = ClientPackets.modCatalog().rivenRules();
        var nextProfile = ClientProfiles.get(ResourceLocation.parse(info.riven().weapon()));
        if (nextRules == rules && nextProfile == profile) return false;
        long positives = layout() == null ? info.riven().lines().stream().filter(line -> !line.negative()).count() : layout().positiveCount();
        long negatives = layout() == null ? info.riven().lines().stream().filter(RivenRoll.Line::negative).count() : layout().negativeCount();
        rules = nextRules; profile = nextProfile;
        layouts = rules == null ? List.of() : rules.layouts().stream().sorted(Comparator.comparingInt(RivenRules.Factors::negativeCount).thenComparingInt(RivenRules.Factors::positiveCount)).toList();
        layoutIndex = 0;
        for (int i = 0; i < layouts.size(); i++) if (layouts.get(i).positiveCount() == positives && layouts.get(i).negativeCount() == negatives) layoutIndex = i;
        analyses.clear(); firstRow = 0; recalculate(); return true;
    }

    private void recalculate() {
        result = null; error = "";
        if (rules == null || profile == null || layout() == null) { error = "rules_unavailable"; return; }
        try {
            result = analyses.computeIfAbsent(layout(), selected -> RivenAnalysis.analyze(rules, profile, selected, info.level()));
            if (!result.possible()) error = "rules_impossible";
        } catch (IllegalArgumentException exception) { error = "rules_invalid"; }
        firstRow = Math.min(firstRow, maximumScroll());
    }

    @Override protected void init() {
        readData(); layoutButtons.clear(); scrolling = false;
        for (int i = 0; i < layouts.size(); i++) {
            int index = i; var bounds = PanelGeometry.RIVEN_RULE_LAYOUTS.get(i); var factor = layouts.get(i);
            String label = factor.positiveCount() + "+" + (factor.negativeCount() == 0 ? "" : factor.negativeCount() + "−");
            var button = addRenderableWidget(new PanelButton(bounds.x(), bounds.y(), bounds.width(), bounds.height(), Component.literal(label), () -> selectLayout(index)).centered());
            button.selected(i == layoutIndex); layoutButtons.add(button);
        }
        var back = PanelGeometry.RIVEN_RULE_BACK;
        addRenderableWidget(new PanelButton(back.x(), back.y(), back.width(), back.height(), Component.translatable("gui.back"), this::onClose).centered());
    }

    private void selectLayout(int index) {
        if (layouts.isEmpty()) return;
        layoutIndex = Math.floorMod(index, layouts.size()); firstRow = 0; recalculate();
        for (int i = 0; i < layoutButtons.size(); i++) layoutButtons.get(i).selected(i == layoutIndex);
    }

    static String probability(double chance) {
        return chance == 0 ? "—" : chance < .0001 ? "<0.01%" : StatText.number(chance * 100) + "%";
    }
    static String range(RivenAnalysis.Range range, ModDefinition.Operation operation, double chance) {
        if (chance == 0) return "—";
        return amount(range.minimum(), operation) + " ～ " + amount(range.maximum(), operation);
    }
    private static String amount(double value, ModDefinition.Operation operation) {
        if (value == 0) value = 0;
        return (value >= 0 ? "+" : "") + StatText.number(operation == ModDefinition.Operation.ADDITION ? value * 100 : value)
                + (operation == ModDefinition.Operation.ADDITION ? "%" : " s");
    }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    @Override public boolean isPauseScreen() { return false; }
    @Override public void tick() { if (readData()) rebuildWidgets(); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xF504090E);
        int mx = (int) logicalX(mouseX), my = (int) logicalY(mouseY);
        graphics.pose().pushPose(); graphics.pose().translate(originX(), originY(), 0); graphics.pose().scale((float) scale(), (float) scale(), 1);
        var canvas = new PanelCanvas(graphics, font);
        PanelPainter.frame(canvas, PanelGeometry.RIVEN_RULES, 0xFF101C27, 0xFF8051A6, 9);
        canvas.centeredText(title.getString(), 550, 57, 18, 0xFFBB88E8, 900);
        canvas.centeredComponent(ModText.localizedName(info), 550, 86, 12, 0xFFBB88E8, 880);
        if (rules != null && profile != null) {
            canvas.centeredText(text("rules_context", info.level(), StatText.number(profile.rivenDisposition()), StatText.number(rules.variationMin()), StatText.number(rules.variationMax())).getString(), 550, 111, 10, PanelPainter.MUTED, 900);
            if (layout() != null) canvas.text(text("rules_factors", StatText.number(layout().positive()) + "×", layout().negativeCount() == 0 ? "—" : StatText.number(layout().negative()) + "×", probability(1D / rules.layouts().size())).getString(), 96, 180, 10, PanelPainter.GOLD, 900);
        }
        canvas.text(text("rules_layout").getString(), 96, 149, 11, PanelPainter.TEXT, 132);
        canvas.text(text("rules_trait").getString(), 100, 208, 10, PanelPainter.GOLD, 230);
        canvas.centeredText(text("rules_positive_chance").getString(), 355, 208, 9, PanelPainter.GOLD, 110);
        canvas.centeredText(text("rules_positive_range").getString(), 515, 208, 9, PanelPainter.GOLD, 200);
        canvas.centeredText(text("rules_negative_chance").getString(), 675, 208, 9, PanelPainter.GOLD, 110);
        canvas.centeredText(text("rules_negative_range").getString(), 860, 208, 9, PanelPainter.GOLD, 245);
        List<Component> tooltip = null;
        var area = PanelGeometry.RIVEN_RULE_LIST;
        for (int i = 0; i < Math.min(visibleRows(), rowCount() - firstRow); i++) {
            int index = firstRow + i, y = area.y() + i * PanelGeometry.RIVEN_RULE_ROW_HEIGHT;
            var row = result.outcomes().get(index);
            boolean hovered = mx >= area.x() && mx < area.x() + area.width() && my >= y && my < y + PanelGeometry.RIVEN_RULE_ROW_HEIGHT;
            canvas.fill(area.x(), y, area.width(), PanelGeometry.RIVEN_RULE_ROW_HEIGHT - 1, hovered ? 0xFF263442 : index % 2 == 0 ? 0xFF142430 : 0xFF101C27);
            canvas.component(StatText.modifierLabel(row.stat()), 100, y + 7, 10, PanelPainter.TEXT, 194);
            canvas.centeredText(probability(row.positiveChance()), 355, y + 7, 9, PanelPainter.TEXT, 100);
            canvas.centeredText(range(row.positiveRange(), row.operation(), row.positiveChance()), 515, y + 7, 9, 0xFF9ADBB5, 200);
            canvas.centeredText(probability(row.negativeChance()), 675, y + 7, 9, PanelPainter.TEXT, 100);
            canvas.centeredText(range(row.negativeRange(), row.operation(), row.negativeChance()), 860, y + 7, 9, 0xFFE8A0A0, 245);
            if (hovered) tooltip = List.of(StatText.modifierLabel(row.stat()), text("rules_positive_detail", probability(row.positiveChance()), range(row.positiveRange(), row.operation(), row.positiveChance())),
                    text("rules_negative_detail", probability(row.negativeChance()), range(row.negativeRange(), row.operation(), row.negativeChance())));
        }
        if (!error.isEmpty()) canvas.centeredText(text(error).getString(), 550, 312, 12, 0xFFE8A0A0, 880);
        if (rowCount() > visibleRows()) {
            int thumb = Math.max(14, area.height() * visibleRows() / rowCount());
            canvas.fill(area.x() + area.width() + 4, area.y(), 4, area.height(), 0xFF263943);
            canvas.fill(area.x() + area.width() + 4, area.y() + (area.height() - thumb) * firstRow / maximumScroll(), 4, thumb, PanelPainter.TEAL);
        }
        canvas.text(text("rules_probability_note").getString(), 96, 456, 9, PanelPainter.MUTED, 740);
        canvas.text(text("rules_rows", rowCount() == 0 ? 0 : firstRow + 1, Math.min(rowCount(), firstRow + visibleRows()), rowCount()).getString(), 845, 456, 9, PanelPainter.MUTED, 165);
        if (rules != null) canvas.centeredText(text("rules_formula", rules.maxLevel() + 1).getString(), 550, 478, 9, PanelPainter.MUTED, 900);
        canvas.centeredText(text("rules_navigation").getString(), 550, 497, 8, PanelPainter.MUTED, 900);
        for (var renderable : renderables) renderable.render(graphics, mx, my, partialTick);
        graphics.pose().popPose();
        if (tooltip != null) graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private void scrollTo(double y) {
        var area = PanelGeometry.RIVEN_RULE_LIST;
        int thumb = rowCount() == 0 ? area.height() : Math.max(14, area.height() * visibleRows() / rowCount());
        double fraction = (y - area.y() - thumb / 2D) / Math.max(1, area.height() - thumb);
        firstRow = (int) Math.round(Math.max(0, Math.min(1, fraction)) * maximumScroll());
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        double mx = logicalX(x), my = logicalY(y); var area = PanelGeometry.RIVEN_RULE_LIST;
        if (button == 0 && maximumScroll() > 0 && mx >= area.x() + area.width() && mx < area.x() + area.width() + 14 && my >= area.y() && my < area.y() + area.height()) {
            scrolling = true; scrollTo(my); setFocused(null); return true;
        }
        return super.mouseClicked(mx, my, button);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (scrolling && button == 0) { scrollTo(logicalY(y)); return true; }
        return super.mouseDragged(logicalX(x), logicalY(y), button, dx / scale(), dy / scale());
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (scrolling && button == 0) { scrolling = false; return true; }
        return super.mouseReleased(logicalX(x), logicalY(y), button);
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        firstRow = Math.max(0, Math.min(maximumScroll(), firstRow + (dy > 0 ? -3 : dy < 0 ? 3 : 0))); return true;
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT) { selectLayout(layoutIndex + (key == GLFW.GLFW_KEY_LEFT ? -1 : 1)); return true; }
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_PAGE_DOWN) {
            int amount = key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_PAGE_DOWN ? visibleRows() : 1;
            firstRow = Math.max(0, Math.min(maximumScroll(), firstRow + (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_PAGE_UP ? -amount : amount))); return true;
        }
        if (key == GLFW.GLFW_KEY_HOME || key == GLFW.GLFW_KEY_END) { firstRow = key == GLFW.GLFW_KEY_HOME ? 0 : maximumScroll(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void onClose() { minecraft.setScreen(parent); parent.resumeCycle(); }
}
