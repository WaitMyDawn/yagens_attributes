package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Conjunctive groups of disjunctive tags, evaluated against individual ledger instances before grouping. */
public record ModFilter(List<List<Tag>> groups) {
    public static final int MAX_GROUPS = 12, MAX_TAGS = 32;
    public static final ModFilter NONE = new ModFilter(List.of());
    public enum Tag {
        COMMON, UNCOMMON, RARE, LEGENDARY, RIVEN, GALVANIZED,
        OCCUPIED, AVAILABLE, LEVELED, UNRANKED, MAX_RANK, BELOW_MAX_RANK;
        public String key() { return name().toLowerCase(Locale.ROOT); }
        public ModInfo.Rarity rarity() {
            return switch (this) {
                case COMMON -> ModInfo.Rarity.COMMON; case UNCOMMON -> ModInfo.Rarity.UNCOMMON;
                case RARE -> ModInfo.Rarity.RARE; case LEGENDARY -> ModInfo.Rarity.LEGENDARY;
                case RIVEN -> ModInfo.Rarity.RIVEN; case GALVANIZED -> ModInfo.Rarity.GALVANIZED;
                default -> null;
            };
        }
        public boolean matches(ModLedger.Entry entry) {
            return switch (this) {
                case COMMON -> entry.info().rarity() == ModInfo.Rarity.COMMON;
                case UNCOMMON -> entry.info().rarity() == ModInfo.Rarity.UNCOMMON;
                case RARE -> entry.info().rarity() == ModInfo.Rarity.RARE;
                case LEGENDARY -> entry.info().rarity() == ModInfo.Rarity.LEGENDARY;
                case RIVEN -> entry.info().rarity() == ModInfo.Rarity.RIVEN;
                case GALVANIZED -> entry.info().rarity() == ModInfo.Rarity.GALVANIZED;
                case OCCUPIED -> entry.weapon() != null;
                case AVAILABLE -> entry.weapon() == null;
                case LEVELED -> entry.info().level() > 0;
                case UNRANKED -> entry.info().level() == 0;
                case MAX_RANK -> entry.info().defined() && entry.info().level() == entry.info().details().maxLevel();
                case BELOW_MAX_RANK -> entry.info().defined() && entry.info().level() < entry.info().details().maxLevel();
            };
        }
    }
    public ModFilter {
        Objects.requireNonNull(groups);
        if (groups.size() > MAX_GROUPS) throw new IllegalArgumentException("Too many filter groups");
        int count = 0;
        var copy = new ArrayList<List<Tag>>();
        for (var group : groups) {
            var tags = List.copyOf(group);
            if (tags.isEmpty() || tags.size() > Tag.values().length || new HashSet<>(tags).size() != tags.size()) throw new IllegalArgumentException("Invalid filter group");
            count += tags.size(); copy.add(tags);
        }
        if (count > MAX_TAGS) throw new IllegalArgumentException("Too many filter tags");
        groups = List.copyOf(copy);
    }
    public boolean matches(ModLedger.Entry entry) {
        for (var group : groups) if (group.stream().noneMatch(tag -> tag.matches(entry))) return false;
        return true;
    }
}
