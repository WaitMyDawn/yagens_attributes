package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import yagen.waitmydawn.api.item.FormaType;

public enum ModRarity {
    COMMON(0),
    UNCOMMON(1),
    RARE(2),
    LEGENDARY(3),
    RIVEN(4),
    WARFRAME(5);

    private final int value;

    ModRarity(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return this.value;
    }

    public MutableComponent getDisplayName() {
        return DISPLAYS[getValue()];
    }

    public static ModRarity fromInteger(Integer integer) {
        for (ModRarity t : values()) {
            if (t.getValue()==integer) return t;
        }
        throw new IllegalArgumentException("Unknown ModRarity: " + integer);
    }

    public int compareRarity(ModRarity other) {
        return Integer.compare(this.getValue(), other.getValue());
    }

    public Style getChatFormatting() {
        return switch (this) {
            case COMMON -> Style.EMPTY.withColor(0xB87333);
            case UNCOMMON -> Style.EMPTY.withColor(0xC0C0C0);
            case RARE -> Style.EMPTY.withColor(0xFFD700);
            case LEGENDARY -> Style.EMPTY.withColor(0xFF4500);
            case RIVEN -> Style.EMPTY.withColor(0x8A2BE2);
            case WARFRAME -> Style.EMPTY.withColor(0xFF1493);
        };
    }

    private final MutableComponent[] DISPLAYS = {
            Component.translatable("rarity.yagens_attributes.common")
                    .withStyle(Style.EMPTY.withColor(0xB87333)),
            Component.translatable("rarity.yagens_attributes.uncommon")
                    .withStyle(Style.EMPTY.withColor(0xC0C0C0)),
            Component.translatable("rarity.yagens_attributes.rare")
                    .withStyle(Style.EMPTY.withColor(0xFFD700)),
            Component.translatable("rarity.yagens_attributes.legendary")
                    .withStyle(Style.EMPTY.withColor(0xFF4500)),
            Component.translatable("rarity.yagens_attributes.riven")
                    .withStyle(Style.EMPTY.withColor(0x8A2BE2)),
            Component.translatable("rarity.yagens_attributes.warframe")
                    .withStyle(Style.EMPTY.withColor(0xFF1493))
    };
}
