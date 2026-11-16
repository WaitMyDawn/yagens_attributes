package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.ModFilter;
import java.util.function.*;
import static yagen.waitmydawn.yagens_attributes.core.ui.PanelPainter.*;

public final class ModFilterPainter {
    public record State(int selectedGroup, int selectedIndex, boolean bankFocused, int bankIndex,
                        ModFilterLayout.Link split, ModFilterLayout.Drop drop, ModFilter.Tag dragging, int mx, int my) {}
    private ModFilterPainter() {}
    public static void backdrop(Canvas c, Function<String, String> text) {
        background(c, "", ""); frame(c, ModFilterLayout.WINDOW, 0xFF0C1720, TEAL, 8);
        c.text(text.apply("filter_title"), 64, 54, 18, GOLD, 650);
        c.text(text.apply("filter_explanation"), 64, 87, 10, TEXT, 950);
        c.text(text.apply("filter_palette"), 64, 108, 11, MUTED, 260);
        c.text(text.apply("filter_groups"), 344, 108, 11, TEAL, 692);
        frame(c, ModFilterLayout.BANK, 0xFF101F2A, 0xFF3D5867, 5);
        frame(c, ModFilterLayout.ACTIVE, 0xFF09131C, 0xFF3D5867, 5);
        c.text(text.apply("filter_quality"), 76, 144, 10, GOLD, 220);
        c.text(text.apply("filter_state"), 76, 304, 10, GOLD, 220);
        c.text(text.apply("filter_drag_help"), 64, 494, 9, MUTED, 970);
        c.text(text.apply("filter_keyboard_help"), 64, 509, 8, MUTED, 970);
    }
    public static void palette(Canvas c, Function<ModFilter.Tag, String> names, ToIntFunction<ModFilter.Tag> color, State state) {
        for (var chip : ModFilterLayout.palette()) chip(c, chip, names, color, state.bankFocused() && state.bankIndex() == chip.index(), chip.bounds().contains(state.mx(), state.my()));
    }
    public static void groups(Canvas c, ModFilter filter, ModFilterLayout layout, Function<ModFilter.Tag, String> names,
                              Function<String, String> text, ToIntFunction<ModFilter.Tag> color, State state) {
        if (filter.groups().isEmpty()) c.centeredText(text.apply("filter_empty"), 690, 280, 12, MUTED, 610);
        for (var group : layout.groups) {
            boolean target = state.drop() != null && state.drop().group() == group.index();
            frame(c, group.bounds(), target ? 0xFF183239 : 0xFF111F2B, target ? GOLD : state.selectedGroup() == group.index() ? TEAL : 0xFF344D5B, 5);
            if (group.index() > 0) c.centeredText(text.apply("filter_and"), group.bounds().x() + 92, group.bounds().y() - 18, 9, TEAL, 150);
        }
        for (var chip : layout.chips) chip(c, chip, names, color, !state.bankFocused() && chip.group() == state.selectedGroup() && chip.index() == state.selectedIndex(), chip.bounds().contains(state.mx(), state.my()));
        for (var link : layout.links) {
            boolean splitting = link.equals(state.split());
            if (!splitting) { c.fill(link.bounds().x(), link.bounds().y() + 14, 4, 1, MUTED); c.fill(link.bounds().x() + 16, link.bounds().y() + 14, 4, 1, MUTED); }
            c.centeredText(text.apply(splitting ? "filter_and" : "filter_or"), link.bounds().x() + 10, link.bounds().y() + 9, 9, splitting ? 0xFFFF8A79 : MUTED, 20);
            if (splitting) { c.fill(link.bounds().x() + 9, link.bounds().y() - 4, 2, 8, 0xFFFF8A79); c.fill(link.bounds().x() + 9, link.bounds().y() + 24, 2, 8, 0xFFFF8A79); }
        }
        if (state.drop() != null && state.drop().group() < 0) {
            int at = state.drop().index(); int y = at < layout.groups.size() ? layout.groups.get(at).bounds().y() - 12 : layout.groups.isEmpty() ? 164 : layout.groups.get(layout.groups.size() - 1).bounds().y() + layout.groups.get(layout.groups.size() - 1).bounds().height() + 12;
            c.fill(360, y, 184, 2, GOLD); c.text(text.apply("filter_and"), 530, y - 12, 8, GOLD, 20);
        }
    }
    public static void chip(Canvas c, ModFilterLayout.Chip chip, Function<ModFilter.Tag, String> names, ToIntFunction<ModFilter.Tag> colors, boolean selected, boolean hover) {
        int color = colors.applyAsInt(chip.tag());
        frame(c, chip.bounds(), hover ? 0xFF263844 : 0xFF182732, selected ? GOLD : color, 4);
        c.centeredText(names.apply(chip.tag()), chip.bounds().x() + chip.bounds().width() / 2, chip.bounds().y() + 8, 10, color, chip.bounds().width() - 12);
    }
}
