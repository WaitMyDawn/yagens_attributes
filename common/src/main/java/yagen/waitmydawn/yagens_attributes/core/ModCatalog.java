package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/**
 * Immutable reload snapshot; each type/rank is compiled once rather than during attacks.
 */
public final class ModCatalog {
    public record Quality(ModInfo.Rarity type, String name, int color, int baseEndo, int order, String description) {
        public Quality {
            Objects.requireNonNull(type);
            Objects.requireNonNull(name);
            Objects.requireNonNull(description);
            if (color < 0 || color > 0xFFFFFF || baseEndo < 1 || baseEndo > 1_000_000 || description.isBlank())
                throw new IllegalArgumentException("Invalid Mod quality");
        }
    }

    public record Polarity(ModInfo.Polarity type, String name, String symbol) {
        public Polarity {
            Objects.requireNonNull(type);
            Objects.requireNonNull(name);
            if (symbol == null || symbol.isBlank() || symbol.length() > 8)
                throw new IllegalArgumentException("Invalid polarity");
        }
    }

    public record Card(String id, int level, ModInfo info, Map<String, Bonus> bonuses) {
    }

    public static final ModCatalog EMPTY = new ModCatalog(Map.of(), Map.of(), Map.of());
    private final Map<String, ModDefinition> definitions;
    private final Map<ModInfo.Rarity, Quality> qualities;
    private final Map<ModInfo.Polarity, Polarity> polarities;
    private final Map<String, List<Card>> cards;
    private final List<Card> creative;
    private final RivenRules rivenRules;
    private final Map<RivenRoll, List<Card>> rolled = new LinkedHashMap<>();

    public ModCatalog(Map<String, ModDefinition> definitions, Map<ModInfo.Rarity, Quality> qualities, Map<ModInfo.Polarity, Polarity> polarities) {
        this(definitions, qualities, polarities, null);
    }
    public ModCatalog(Map<String, ModDefinition> definitions, Map<ModInfo.Rarity, Quality> qualities, Map<ModInfo.Polarity, Polarity> polarities, RivenRules rivenRules) {
        this.rivenRules = rivenRules;
        this.definitions = Map.copyOf(definitions);
        this.qualities = Map.copyOf(qualities);
        this.polarities = Map.copyOf(polarities);
        if (definitions.size() > 1024) throw new IllegalArgumentException("Too many Mod definitions");
        var compiled = new HashMap<String, List<Card>>();
        var all = new ArrayList<Card>();
        definitions.forEach((id, definition) -> {
            ModDefinition.id(id);
            var variants = compile(id, definition, null);
            all.addAll(variants);
            compiled.put(id, variants);
        });
        cards = Map.copyOf(compiled);
        all.sort(Comparator.comparingInt((Card card) -> card.info().qualityOrder()).thenComparing(Card::id).thenComparingInt(Card::level));
        creative = List.copyOf(all);
    }
    private List<Card> compile(String id, ModDefinition definition, RivenRoll roll) {
            Quality quality = Objects.requireNonNull(qualities.get(definition.rarity()), "Missing quality");
            Polarity polarity = Objects.requireNonNull(polarities.get(definition.polarity()), "Missing polarity");
            ModInfo.Category primary = ModInfo.Category.ALL;
            for (var category : ModInfo.Category.values())
                if (definition.applicability().stream().allMatch(category::contains)) primary = category;
            var variants = new ArrayList<Card>();
            for (int level = 0; level <= definition.maxLevel(); level++) {
                long cost = level == definition.maxLevel() ? 0 : Math.multiplyExact((long) quality.baseEndo(), 1L << level);
                var details = new ModInfo.Details(id, level, definition.maxLevel(), definition.applicability(), definition.texture(), polarity.symbol(), quality.color(), quality.order(), cost, roll, definition.style(), definition.conditional(level));
                var info = new ModInfo(definition.name(), primary, definition.rarity(), definition.polarity(), definition.baseCapacity() + definition.capacityPerLevel() * level, details);
                var card = new Card(id, level, info, definition.bonuses(level));
                variants.add(card);
            }
            return List.copyOf(variants);
    }
    public RivenRules rivenRules() { return rivenRules; }
    public synchronized Card rivenCard(RivenRoll roll, int level) {
        if (rivenRules == null || level < 0 || level > rivenRules.maxLevel()) throw new IllegalArgumentException("Invalid Riven rank or missing rules");
        var variants = rolled.get(roll);
        if (variants == null) {
            variants = compile(RivenRules.TYPE, rivenRules.definition(roll), roll);
            if (rolled.size() >= 512) rolled.remove(rolled.keySet().iterator().next());
            rolled.put(roll, variants);
        }
        return variants.get(level);
    }
    public Card card(ModInfo info, int level) {
        return info.riven() == null ? card(info.details().type(), level) : rivenCard(info.riven(), level);
    }

    public Map<String, ModDefinition> definitions() {
        return definitions;
    }

    public Map<ModInfo.Rarity, Quality> qualities() {
        return qualities;
    }

    public Map<ModInfo.Polarity, Polarity> polarities() {
        return polarities;
    }

    public List<Card> creative() {
        return creative;
    }

    public String qualityName(ModInfo.Rarity type) {
        var quality = qualities.get(type);
        return quality == null ? "rarity.yagens_attributes." + type.key() : quality.name();
    }

    public String polarityName(ModInfo.Polarity type) {
        var polarity = polarities.get(type);
        return polarity == null ? "polarity.yagens_attributes." + type.key() : polarity.name();
    }

    public String polaritySymbol(ModInfo.Polarity type) {
        var polarity = polarities.get(type);
        return polarity == null ? type.symbol() : polarity.symbol();
    }

    public Card card(String id, int level) {
        var variants = cards.get(id);
        if (variants == null || level < 0 || level >= variants.size())
            throw new IllegalArgumentException("Unknown Mod type or level: " + id + " / " + level);
        return variants.get(level);
    }

    public ModInfo legacyInfo(ModInfo old) {
        var quality = qualities.get(old.rarity());
        var polarity = polarities.get(old.polarity());
        if (quality == null || polarity == null) return old;
        var details = new ModInfo.Details("", 0, 0, old.applicability(), "yagens_attributes:item/mod", polarity.symbol(), quality.color(), quality.order(), 0);
        return new ModInfo(old.name(), old.category(), old.rarity(), old.polarity(), old.capacityCost(), details);
    }
}
