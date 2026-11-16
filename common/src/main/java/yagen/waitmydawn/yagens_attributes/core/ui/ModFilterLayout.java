package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.ModFilter;
import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.Rect;

public final class ModFilterLayout {
    public static final Rect WINDOW = new Rect(40, 34, 1020, 532), BANK = new Rect(64, 128, 260, 354), ACTIVE = new Rect(344, 128, 692, 354);
    public static final Rect CLEAR = new Rect(64, 526, 112, 26), CANCEL = new Rect(790, 526, 110, 26), APPLY = new Rect(914, 526, 122, 26);
    public record Chip(int group, int index, ModFilter.Tag tag, Rect bounds) {}
    public record Link(int group, int splitAt, Rect bounds) {}
    /** A negative group inserts a new AND group at index; otherwise index inserts a tag into an OR group. */
    public record Drop(int group, int index) {}
    public record Group(int index, Rect bounds) {}
    public final List<Chip> chips;
    public final List<Link> links;
    public final List<Group> groups;
    public final int maximumScroll;
    public ModFilterLayout(ModFilter filter, int scroll) {
        var chips = new ArrayList<Chip>(); var links = new ArrayList<Link>(); var groups = new ArrayList<Group>();
        int y = ACTIVE.y() + 12;
        for (int g = 0; g < filter.groups().size(); g++) {
            var tags = filter.groups().get(g); int height = ((tags.size() + 3) / 4) * 40 + 16;
            int columns = Math.min(4, tags.size());
            var bounds = new Rect(ACTIVE.x() + 10, y - scroll, 52 + columns * 132 + (columns - 1) * 24, height); groups.add(new Group(g, bounds));
            for (int i = 0; i < tags.size(); i++) {
                int x = bounds.x() + 26 + (i % 4) * 156, cy = bounds.y() + 8 + (i / 4) * 40;
                chips.add(new Chip(g, i, tags.get(i), new Rect(x, cy, 132, 28)));
                if (i > 0) links.add(new Link(g, i, new Rect(x - 22, cy, 20, 28)));
            }
            y += height + 24;
        }
        this.chips = List.copyOf(chips); this.links = List.copyOf(links); this.groups = List.copyOf(groups);
        maximumScroll = Math.max(0, y - 24 + 12 - (ACTIVE.y() + ACTIVE.height()));
    }
    public static List<Chip> palette() {
        var result = new ArrayList<Chip>();
        for (int i = 0; i < ModFilter.Tag.values().length; i++) {
            int row = i < 6 ? i / 2 : (i - 6) / 2;
            result.add(new Chip(-1, i, ModFilter.Tag.values()[i], new Rect(BANK.x() + 12 + (i % 2) * 124, BANK.y() + (i < 6 ? 42 : 202) + row * 42, 112, 28)));
        }
        return List.copyOf(result);
    }
    public Chip chip(double x, double y) { return ACTIVE.contains(x, y) ? chips.stream().filter(c -> c.bounds().contains(x, y)).findFirst().orElse(null) : null; }
    public Link link(double x, double y) { return ACTIVE.contains(x, y) ? links.stream().filter(c -> c.bounds().contains(x, y)).findFirst().orElse(null) : null; }
    public Drop drop(double x, double y) {
        if (!ACTIVE.contains(x, y)) return null;
        for (var group : groups) {
            var r = group.bounds();
            if (y >= r.y() - 3 && y < r.y() + r.height() + 3) {
                Chip nearest = null; double distance = Double.POSITIVE_INFINITY;
                for (var chip : chips) if (chip.group() == group.index()) {
                    var c = chip.bounds();
                    if (x >= c.x() - 12 && x <= c.x() + c.width() + 12 && y >= c.y() - 8 && y < c.y() + c.height() + 8) {
                        double dx = x - c.x() - c.width() / 2D, dy = y - c.y() - c.height() / 2D, next = dx * dx + dy * dy;
                        if (next < distance) { distance = next; nearest = chip; }
                    }
                }
                if (nearest != null) return new Drop(group.index(), nearest.index() + (x < nearest.bounds().x() + nearest.bounds().width() / 2D ? 0 : 1));
                return new Drop(-1, group.index() + 1);
            }
            if (y < r.y()) return new Drop(-1, group.index());
        }
        return new Drop(-1, groups.size());
    }
}
