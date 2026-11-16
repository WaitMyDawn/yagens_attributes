package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import java.util.function.Function;

public final class ModInventory {
    public enum Order {QUALITY, QUANTITY, NAME}

    public record Query(String search, ModInfo.Category category, Order order, ModFilter filter) {
        public Query(String search, ModInfo.Category category, Order order) { this(search, category, order, ModFilter.NONE); }
        public static final Query DEFAULT = new Query("", ModInfo.Category.ALL, Order.QUALITY);

        public Query {
            Objects.requireNonNull(search);
            Objects.requireNonNull(category);
            Objects.requireNonNull(order);
            Objects.requireNonNull(filter);
            if (search.length() > 80 || search.chars().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Invalid inventory query");
        }
    }

    public record Group(ModLedger.Entry representative, int quantity, int available, String name) {
    }

    private record Variant(ModInfo info, Map<String, Bonus> bonuses, List<String> order, UUID occupiedInstance) {
    }

    private ModInventory() {
    }

    public static boolean sameVariant(ModLedger.Entry a, ModLedger.Entry b) {
        return a.info().equals(b.info()) && a.bonuses().equals(b.bonuses()) && List.copyOf(a.bonuses().keySet()).equals(List.copyOf(b.bonuses().keySet()));
    }

    public static List<Group> browse(Collection<ModLedger.Entry> entries, Query query, ModInfo.Category weapon,
                                     Function<ModLedger.Entry, String> names) {
        return browse(entries, query, weapon, names, String.CASE_INSENSITIVE_ORDER);
    }

    public static List<Group> browse(Collection<ModLedger.Entry> entries, Query query, ModInfo.Category weapon,
                                     Function<ModLedger.Entry, String> names, Comparator<String> collation) {
        Map<Variant, List<ModLedger.Entry>> groups = new LinkedHashMap<>();
        String search = query.search().strip().toLowerCase(Locale.ROOT);
        for (var entry : entries) {
            var category = entry.info().category();
            if (weapon != null && (entry.weapon() != null || !entry.info().compatible(weapon))) continue;
            if (!entry.info().matches(query.category())) continue;
            if (!query.filter().matches(entry)) continue;
            String name = names.apply(entry);
            if (!search.isEmpty() && !(name + " " + entry.info().name() + " " + String.join(" ", entry.bonuses().keySet())).toLowerCase(Locale.ROOT).contains(search))
                continue;
            // Every reservation is individually addressable; only free copies are interchangeable.
            groups.computeIfAbsent(new Variant(entry.info(), entry.bonuses(), List.copyOf(entry.bonuses().keySet()), entry.weapon() == null ? null : entry.id()), key -> new ArrayList<>()).add(entry);
        }
        List<Group> result = new ArrayList<>();
        for (var copies : groups.values()) {
            copies.sort(Comparator.<ModLedger.Entry, Boolean>comparing(entry -> entry.weapon() != null).thenComparing(entry -> entry.id().toString()));
            var entry = copies.get(0);
            result.add(new Group(entry, copies.size(), (int) copies.stream().filter(copy -> copy.weapon() == null).count(), names.apply(entry)));
        }
        Comparator<Group> byName = Comparator.comparing(Group::name, collation);
        Comparator<Group> order = switch (query.order()) {
            case QUALITY ->
                    Comparator.<Group, Integer>comparing(group -> group.representative().info().qualityOrder()).reversed().thenComparing(byName).thenComparingInt(group -> group.representative().info().level());
            case QUANTITY -> Comparator.comparingInt(Group::quantity).reversed().thenComparing(byName);
            case NAME -> byName;
        };
        result.sort(order.thenComparing(group -> group.representative().id().toString()));
        return List.copyOf(result);
    }
}
