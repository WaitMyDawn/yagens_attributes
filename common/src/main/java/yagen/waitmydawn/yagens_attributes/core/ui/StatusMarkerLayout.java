package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.*;

/** Rows grow upward from a fixed bottom anchor, independently of stack count or entity height. */
public final class StatusMarkerLayout {
    private StatusMarkerLayout() {}
    public record Entry(DamageType type, int count) {}
    public static List<List<Entry>> rows(Map<DamageType, Integer> counts, int perRow) {
        if (perRow < 1 || perRow > DamageType.values().length) throw new IllegalArgumentException("Invalid statuses per row");
        var entries = new ArrayList<Entry>();
        for (var type : DamageType.DISPLAY_ORDER) {
            int count = counts.getOrDefault(type, 0);
            if (count < 0 || count > 8192) throw new IllegalArgumentException("Invalid status count");
            if (count > 0) entries.add(new Entry(type, count));
        }
        var rows = new ArrayList<List<Entry>>();
        for (int i = 0; i < entries.size(); i += perRow) rows.add(List.copyOf(entries.subList(i, Math.min(entries.size(), i + perRow))));
        return List.copyOf(rows);
    }
    public static float rowY(int row, int rows) { return -(rows - 1 - row) * 12 - 9; }
    public static double anchorHeight(double height, double gap) { return height * (1 + gap); }
}
