package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.config.StatusVisualOptions.Option;
import yagen.waitmydawn.yagens_attributes.core.DamageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatusSettingsScreen extends Screen {
    private static final int FIRST_ROW = 101;
    private static final int ROW_HEIGHT = 24;
    private final boolean countsOnly;
    private final Map<Option<?>, String> original;
    private final Map<Option<?>, String> draft = new LinkedHashMap<>();
    private DamageType selected;
    private int page;
    private Button done;
    private Option<?> invalid;
    private boolean saveFailed, rebuild;

    public StatusSettingsScreen(boolean countsOnly) {
        super(statusText(countsOnly ? "marker_title" : "title"));
        this.countsOnly = countsOnly;
        options(null).forEach(this::read);
        for (DamageType type : DamageType.values()) options(type).forEach(this::read);
        original = Map.copyOf(draft);
    }

    private static Component statusText(String key, Object... args) {
        return Component.translatable("gui.yagens_attributes.status_settings." + key, args);
    }

    private void read(Option<?> option) { draft.put(option, option.get().toString()); }
    private List<Option<?>> options(DamageType type) {
        var status = ClientConfigs.STATUS;
        if (type == null) return countsOnly ? status.markerGeneral : status.general;
        var effect = status.effect(type);
        return countsOnly ? List.of(effect.marker()) : effect.options();
    }
    private List<Option<?>> options() { return options(selected); }

    private int contentWidth() { return Math.min(360, width - 16); }
    private int left() { return (width - contentWidth()) / 2; }
    private int rowsPerPage() { return Math.max(1, (height - 60 - FIRST_ROW) / ROW_HEIGHT); }
    private int pages() { return (options().size() + rowsPerPage() - 1) / rowsPerPage(); }
    private Component heading() { return selected == null ? statusText("general") : StatText.damageLabel(selected); }

    @Override protected void init() {
        page = Math.clamp(page, 0, pages() - 1);
        int cell = contentWidth() / 7;
        for (int i = 0; i <= DamageType.DISPLAY_ORDER.size(); i++) {
            var type = i == 0 ? null : DamageType.DISPLAY_ORDER.get(i - 1);
            Component label = type == null ? statusText("general_short") : Component.literal(String.valueOf((char)(0xE000 + type.ordinal())))
                    .withStyle(Style.EMPTY.withFont(StatText.DAMAGE_FONT));
            var tab = addRenderableWidget(Button.builder(label, button -> {
                selected = type;
                page = 0;
                rebuild = true;
            }).bounds(left() + (i % 7) * cell, 26 + (i / 7) * 23, cell - 3, 20).build());
            tab.active = selected != type;
            tab.setTooltip(Tooltip.create(type == null ? statusText("general") : StatText.damageLabel(type)));
        }
        int bottom = height - 28, third = contentWidth() / 3;
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
            options().forEach(option -> draft.put(option, option.defaultValue().toString()));
            saveFailed = false;
            rebuild = true;
        }).bounds(left(), bottom, third - 4, 20).tooltip(Tooltip.create(statusText("reset_section"))).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left() + third, bottom, third - 4, 20).build());
        done = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> save())
                .bounds(left() + third * 2, bottom, third, 20).build());
        if (pages() > 1) {
            int center = width / 2;
            var previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; rebuild = true; })
                    .bounds(center - 63, height - 54, 20, 20).tooltip(Tooltip.create(statusText("previous"))).build());
            var next = addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; rebuild = true; })
                    .bounds(center + 43, height - 54, 20, 20).tooltip(Tooltip.create(statusText("next"))).build());
            previous.active = page > 0;
            next.active = page + 1 < pages();
        }
        var options = options();
        int first = page * rowsPerPage();
        for (int i = first; i < Math.min(options.size(), first + rowsPerPage()); i++)
            addControl(options.get(i), FIRST_ROW + (i - first) * ROW_HEIGHT);
        updateValidity();
    }

    private void addControl(Option<?> option, int y) {
        int inputWidth = 74, x = left() + contentWidth() - inputWidth;
        Component name = statusText(option.label());
        if (option.defaultValue() instanceof Boolean) {
            addRenderableWidget(Button.builder(toggleText(option), button -> {
                draft.put(option, Boolean.toString(!Boolean.parseBoolean(draft.get(option))));
                button.setMessage(toggleText(option));
                updateValidity();
            }).bounds(x, y, inputWidth, 20).tooltip(Tooltip.create(name)).build());
        } else {
            var input = new EditBox(font, x, y, inputWidth, 20, name);
            input.setMaxLength(24);
            input.setValue(draft.get(option));
            input.setTextColor(valid(option) ? 0xE0E0E0 : 0xFF7777);
            input.setTooltip(Tooltip.create(statusText("allowed_range", name, option.range())));
            input.setResponder(value -> {
                draft.put(option, value);
                input.setTextColor(valid(option) ? 0xE0E0E0 : 0xFF7777);
                updateValidity();
            });
            addRenderableWidget(input);
        }
    }

    private Component toggleText(Option<?> option) {
        return Component.translatable(Boolean.parseBoolean(draft.get(option)) ? "options.on" : "options.off");
    }

    private boolean valid(Option<?> option) {
        try {
            option.parse().apply(draft.get(option).trim());
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void updateValidity() {
        saveFailed = false;
        invalid = draft.keySet().stream().filter(option -> !valid(option)).findFirst().orElse(null);
        done.active = invalid == null;
    }

    private void save() {
        if (invalid != null) return;
        var rollback = new ArrayList<Runnable>();
        boolean effectsChanged = false;
        try {
            for (var entry : draft.entrySet()) {
                var option = entry.getKey();
                var value = entry.getValue();
                if (value.equals(original.get(option))) continue;
                if (write(option, value, rollback) && !ClientConfigs.STATUS.isMarkerOption(option)) effectsChanged = true;
            }
            if (!rollback.isEmpty()) ClientConfigs.SPEC.save();
        } catch (RuntimeException exception) {
            rollback.forEach(Runnable::run);
            YagensAttributes.LOGGER.error("Could not save status visual settings", exception);
            saveFailed = true;
            return;
        }
        if (effectsChanged) StatusEffectsRenderer.clearTransient();
        onClose();
    }

    private static <T> boolean write(Option<T> option, String input, List<Runnable> rollback) {
        T previous = option.get();
        T next = option.parse().apply(input.trim());
        if (previous.equals(next)) return false;
        rollback.add(() -> option.config().set(previous));
        option.config().set(next);
        return true;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void tick() {
        if (!rebuild) return;
        rebuild = false;
        rebuildWidgets();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFE1CE90);
        graphics.drawString(font, heading(), left(), 79, 0xFFE1CE90);
        int first = page * rowsPerPage();
        var options = options();
        for (int i = first; i < Math.min(options.size(), first + rowsPerPage()); i++) {
            String label = statusText(options.get(i).label()).getString();
            graphics.drawString(font, font.plainSubstrByWidth(label, contentWidth() - 80),
                    left(), FIRST_ROW + (i - first) * ROW_HEIGHT + 6, 0xFFE0E0E0);
        }
        if (pages() > 1) graphics.drawCenteredString(font, (page + 1) + " / " + pages(), width / 2, height - 48, 0xFFE0E0E0);
        if (invalid != null) graphics.drawCenteredString(font, statusText("invalid_value", statusText(invalid.label())), width / 2, 90, 0xFFFF7777);
        if (saveFailed) graphics.drawCenteredString(font, Component.translatable("gui.yagens_attributes.hud_save_failed"),
                width / 2, 90, 0xFFFF7777);
    }
}
