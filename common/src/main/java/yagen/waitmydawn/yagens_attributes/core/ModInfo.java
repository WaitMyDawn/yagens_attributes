package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public record ModInfo(String name, Category category, Rarity rarity, Polarity polarity, int capacityCost,
                      Details details) {
    public ModInfo(String name, Category category, Rarity rarity, Polarity polarity, int capacityCost) {
        this(name, category, rarity, polarity, capacityCost, null);
    }

    public ModInfo(String name, Category category, Rarity rarity, Polarity polarity) {
        this(name, category, rarity, polarity, 1);
    }

    public static final ModInfo DEFAULT = new ModInfo("", Category.WEAPON, Rarity.COMMON, Polarity.NONE);

    public ModInfo {
        Objects.requireNonNull(name);
        Objects.requireNonNull(category);
        Objects.requireNonNull(rarity);
        Objects.requireNonNull(polarity);
        if (name.length() > 80 || name.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid Mod name");
        if (category == Category.ALL) throw new IllegalArgumentException("ALL is a browser filter, not a Mod category");
        if (capacityCost < 0 || capacityCost > 1_000_000)
            throw new IllegalArgumentException("Invalid Mod capacity cost");
        if (details != null && details.riven() != null && (rarity != Rarity.RIVEN || polarity != details.riven().polarity() || category != details.riven().category()))
            throw new IllegalArgumentException("Riven metadata disagrees with its specialization");
    }

    public record Details(String type, int level, int maxLevel, Set<Category> applicability, String texture,
                          String symbol, int color, int order, long upgradeCost, RivenRoll riven, ModStyle style, List<ConditionalModifier> conditional) {
        public Details(String type, int level, int maxLevel, Set<Category> applicability, String texture, String symbol, int color, int order, long upgradeCost, RivenRoll riven) {
            this(type, level, maxLevel, applicability, texture, symbol, color, order, upgradeCost, riven, ModStyle.DEFAULT, List.of());
        }
        public Details(String type, int level, int maxLevel, Set<Category> applicability, String texture, String symbol, int color, int order, long upgradeCost) {
            this(type, level, maxLevel, applicability, texture, symbol, color, order, upgradeCost, null);
        }
        public Details {
            Objects.requireNonNull(style);
            conditional = List.copyOf(conditional);
            if (conditional.size() > ModDefinition.MAX_EFFECTS) throw new IllegalArgumentException("Too many conditional modifiers");
            Objects.requireNonNull(type);
            Objects.requireNonNull(texture);
            Objects.requireNonNull(symbol);
            if (!type.isEmpty()) ModDefinition.id(type);
            ModDefinition.id(texture);
            applicability = Set.copyOf(applicability);
            if (riven != null && (!type.equals(RivenRules.TYPE) || maxLevel != 8 || !applicability.equals(Set.of(riven.category()))))
                throw new IllegalArgumentException("Invalid Riven definition details");
            if (symbol.length() > 8 || level < 0 || maxLevel < level || maxLevel > 60
                    || applicability.isEmpty() || applicability.contains(Category.ALL) || upgradeCost < 0 || color < 0 || color > 0xFFFFFF)
                throw new IllegalArgumentException("Invalid Mod details");
        }
    }

    public boolean defined() {
        return details != null && !details.type().isEmpty();
    }
    public ModStyle style() { return details == null ? ModStyle.DEFAULT : details.style(); }
    public boolean conflicts(ModInfo other) {
        return conflicts(other, null);
    }
    public boolean conflicts(ModInfo other, Category weapon) {
        return defined() && other.defined() && (details.type().equals(other.details.type()) || style().conflicts(other.style(), weapon));
    }
    public RivenRoll riven() { return details == null ? null : details.riven(); }
    public boolean compatibleItem(String item) { return riven() == null || riven().weapon().equals(item); }

    public Set<Category> applicability() {
        return details == null ? Set.of(category) : details.applicability();
    }

    public boolean compatible(Category weapon) {
        return applicability().stream().anyMatch(value -> value.compatible(weapon));
    }

    public boolean matches(Category filter) {
        return applicability().stream().anyMatch(value -> filter.contains(value) || value.contains(filter));
    }

    public int color() {
        return details == null ? rarity.color() : details.color();
    }

    public int qualityOrder() {
        return details == null ? rarity.ordinal() * 10 : details.order();
    }

    public String polaritySymbol() {
        return details == null ? polarity.symbol() : details.symbol();
    }

    public int level() {
        return details == null ? 0 : details.level();
    }

    public enum Category {
        ALL(null), WARFRAME(ALL), WEAPON(ALL), MELEE(WEAPON), RANGED(WEAPON),
        SWORD(MELEE), AXE(MELEE), TRIDENT(MELEE), MACE(MELEE), BOW(RANGED), CROSSBOW(RANGED);
        private final Category parent;

        Category(Category parent) {
            this.parent = parent;
        }

        public Category parent() {
            return parent;
        }

        public boolean contains(Category other) {
            for (Category node = other; node != null; node = node.parent) if (node == this) return true;
            return false;
        }

        public boolean compatible(Category weapon) {
            return this != ALL && this != WARFRAME && contains(weapon);
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Rarity {
        COMMON(0xAD8263), UNCOMMON(0xB5C9D5), RARE(0xE2C36D), LEGENDARY(0xCAA3E7), RIVEN(0x8A2BE2), GALVANIZED(0x636A8E);
        private final int color;

        Rarity(int color) {
            this.color = color;
        }

        public int color() {
            return color;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Polarity {
        NONE, MADURAI, VAZARIN, NARAMON, ZENURIK, UNAIRU, PENJAGA, UMBRA, OMNI;

        public boolean matches(Polarity mod) {
            return this == mod || this == OMNI && switch (mod) {
                case MADURAI, VAZARIN, NARAMON, ZENURIK, UNAIRU, PENJAGA, OMNI -> true;
                case NONE, UMBRA -> false;
            };
        }

        public String symbol() {
            return String.valueOf((char) (yagen.waitmydawn.yagens_attributes.core.ui.PolarityGlyphs.FIRST + ordinal()));
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
