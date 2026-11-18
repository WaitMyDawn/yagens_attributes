package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Explicit presentation and installation families; neither is inferred from a registry name. */
public record ModStyle(Visual visual, Set<String> exclusiveGroups, Map<ModInfo.Category, Set<String>> scopedExclusiveGroups) {
    public static final int MAX_GROUPS = 16;
    public ModStyle(Visual visual, Set<String> exclusiveGroups) { this(visual, exclusiveGroups, Map.of()); }
    public enum Visual { NONE, OROKIN, GALVANIZED }
    public static final ModStyle DEFAULT = new ModStyle(Visual.NONE, Set.of());
    public ModStyle {
        Objects.requireNonNull(visual);
        exclusiveGroups = Set.copyOf(exclusiveGroups);
        if (exclusiveGroups.size() > MAX_GROUPS) throw new IllegalArgumentException("Too many exclusion groups");
        exclusiveGroups.forEach(ModDefinition::id);
        var scoped = new EnumMap<ModInfo.Category, Set<String>>(ModInfo.Category.class);
        scopedExclusiveGroups.forEach((category, groups) -> {
            if (!ModInfo.Category.WEAPON.contains(category) || groups.size() > MAX_GROUPS) throw new IllegalArgumentException("Invalid scoped exclusion group");
            var copy = Set.copyOf(groups); copy.forEach(ModDefinition::id); scoped.put(category, copy);
        });
        scopedExclusiveGroups = Map.copyOf(scoped);
    }
    public static ModStyle family(String group) { return new ModStyle(Visual.NONE, Set.of(group)); }
    public boolean conflicts(ModStyle other) { return conflicts(other, null); }
    public boolean conflicts(ModStyle other, ModInfo.Category weapon) {
        var own = groups(weapon); var theirs = other.groups(weapon);
        return own.stream().anyMatch(theirs::contains);
    }
    private Set<String> groups(ModInfo.Category weapon) {
        if (weapon == null || scopedExclusiveGroups.isEmpty()) return exclusiveGroups;
        var result = new HashSet<>(exclusiveGroups);
        scopedExclusiveGroups.forEach((scope, groups) -> { if (scope.contains(weapon)) result.addAll(groups); });
        return result;
    }
    public String texture(String mod, String base) {
        if (visual == Visual.NONE) return base;
        ModDefinition.id(mod);
        int colon = mod.indexOf(':');
        return mod.substring(0, colon) + ":item/mods/effects/" + mod.substring(colon + 1);
    }
}
