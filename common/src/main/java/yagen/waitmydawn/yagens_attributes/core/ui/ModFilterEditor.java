package yagen.waitmydawn.yagens_attributes.core.ui;

import yagen.waitmydawn.yagens_attributes.core.ModFilter;
import java.util.*;

/** Draft edits publish only complete, validated expressions; rejected drops leave the source intact. */
public final class ModFilterEditor {
    private ModFilter value;
    public ModFilterEditor(ModFilter value) { this.value = Objects.requireNonNull(value); }
    public ModFilter value() { return value; }
    public void clear() { value = ModFilter.NONE; }
    public boolean drop(ModFilter.Tag tag, int sourceGroup, int sourceIndex, int targetGroup, int targetIndex) {
        var groups = mutable();
        try {
            if (sourceGroup >= 0) {
                if (groups.get(sourceGroup).get(sourceIndex) != tag) return false;
                groups.get(sourceGroup).remove(sourceIndex);
                if (sourceGroup == targetGroup && sourceIndex < targetIndex) targetIndex--;
            }
            if (targetGroup < 0) groups.add(targetIndex, new ArrayList<>(List.of(tag)));
            else {
                var target = groups.get(targetGroup);
                if (target.contains(tag)) return false;
                target.add(targetIndex, tag);
            }
            groups.removeIf(List::isEmpty);
            value = new ModFilter(groups); return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) { return false; }
    }
    public void remove(int group, int index) {
        var groups = mutable(); groups.get(group).remove(index); groups.removeIf(List::isEmpty); value = new ModFilter(groups);
    }
    public boolean split(int group, int at) {
        var groups = mutable(); var tags = groups.get(group);
        if (at <= 0 || at >= tags.size() || groups.size() == ModFilter.MAX_GROUPS) return false;
        groups.set(group, new ArrayList<>(tags.subList(0, at)));
        groups.add(group + 1, new ArrayList<>(tags.subList(at, tags.size())));
        value = new ModFilter(groups); return true;
    }
    private ArrayList<List<ModFilter.Tag>> mutable() {
        var groups = new ArrayList<List<ModFilter.Tag>>();
        for (var group : value.groups()) groups.add(new ArrayList<>(group));
        return groups;
    }
}
