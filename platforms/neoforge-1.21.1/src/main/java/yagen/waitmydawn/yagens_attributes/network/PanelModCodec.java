package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;
import yagen.waitmydawn.yagens_attributes.core.ModDefinition;
import yagen.waitmydawn.yagens_attributes.core.ModInfo;
import yagen.waitmydawn.yagens_attributes.core.ModStyle;
import yagen.waitmydawn.yagens_attributes.core.RivenRoll;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** Panel metadata uses bounded fields independently of data-pack descriptions and saved JSON. */
final class PanelModCodec {
    private PanelModCodec() {}

    static void write(RegistryFriendlyByteBuf buffer, ModInfo info) {
        buffer.writeUtf(info.name(), 80).writeEnum(info.category()).writeEnum(info.rarity()).writeEnum(info.polarity());
        buffer.writeVarInt(info.capacityCost());
        var details = info.details();
        buffer.writeBoolean(details != null);
        if (details == null) return;

        buffer.writeUtf(details.type(), ModDefinition.MAX_ID_LENGTH).writeVarInt(details.level()).writeVarInt(details.maxLevel());
        buffer.writeVarInt(details.applicability().size());
        details.applicability().forEach(buffer::writeEnum);
        buffer.writeUtf(details.texture(), ModDefinition.MAX_ID_LENGTH).writeUtf(details.symbol(), 8);
        buffer.writeInt(details.color()).writeVarInt(details.order()).writeVarLong(details.upgradeCost());
        writeRiven(buffer, details.riven());
        writeStyle(buffer, details.style());
        buffer.writeVarInt(details.conditional().size());
        for (var modifier : details.conditional()) {
            var condition = modifier.condition();
            buffer.writeUtf(modifier.stat(), 64).writeDouble(modifier.addition());
            buffer.writeEnum(condition.trigger()).writeDouble(condition.durationSeconds()).writeVarInt(condition.maxStacks());
            buffer.writeBoolean(condition.perTargetStatus()).writeEnum(condition.decay()).writeBoolean(condition.scaleDurationWithRank());
        }
    }

    static ModInfo read(RegistryFriendlyByteBuf buffer) {
        String name = buffer.readUtf(80);
        var category = buffer.readEnum(ModInfo.Category.class);
        var rarity = buffer.readEnum(ModInfo.Rarity.class);
        var polarity = buffer.readEnum(ModInfo.Polarity.class);
        int capacity = buffer.readVarInt();
        if (!buffer.readBoolean()) return new ModInfo(name, category, rarity, polarity, capacity);

        var type = buffer.readUtf(ModDefinition.MAX_ID_LENGTH);
        int level = buffer.readVarInt(), maximum = buffer.readVarInt();
        var applicability = EnumSet.noneOf(ModInfo.Category.class);
        int scopeSize = CombatCodecs.count(buffer, ModInfo.Category.values().length);
        for (int i = 0; i < scopeSize; i++) {
            if (!applicability.add(buffer.readEnum(ModInfo.Category.class))) throw new IllegalArgumentException("Duplicate Mod scope");
        }
        String texture = buffer.readUtf(ModDefinition.MAX_ID_LENGTH), symbol = buffer.readUtf(8);
        int color = buffer.readInt(), order = buffer.readVarInt();
        long upgradeCost = buffer.readVarLong();
        var riven = readRiven(buffer);
        var style = readStyle(buffer);
        int count = CombatCodecs.count(buffer, ModDefinition.MAX_EFFECTS);
        var conditional = new ArrayList<ConditionalModifier>(count);
        for (int i = 0; i < count; i++) {
            String stat = buffer.readUtf(64);
            double addition = buffer.readDouble();
            var condition = new ConditionalModifier.Condition(buffer.readEnum(ConditionalModifier.Trigger.class), buffer.readDouble(),
                    buffer.readVarInt(), buffer.readBoolean(), buffer.readEnum(ConditionalModifier.Decay.class), buffer.readBoolean());
            conditional.add(new ConditionalModifier(stat, addition, condition));
        }
        return new ModInfo(name, category, rarity, polarity, capacity,
                new ModInfo.Details(type, level, maximum, applicability, texture, symbol, color, order, upgradeCost, riven, style, conditional));
    }

    private static void writeStyle(RegistryFriendlyByteBuf buffer, ModStyle style) {
        buffer.writeEnum(style.visual());
        writeGroups(buffer, style.exclusiveGroups());
        buffer.writeVarInt(style.scopedExclusiveGroups().size());
        style.scopedExclusiveGroups().forEach((category, groups) -> {
            buffer.writeEnum(category);
            writeGroups(buffer, groups);
        });
    }

    private static ModStyle readStyle(RegistryFriendlyByteBuf buffer) {
        var visual = buffer.readEnum(ModStyle.Visual.class);
        var groups = readGroups(buffer);
        var scoped = new EnumMap<ModInfo.Category, Set<String>>(ModInfo.Category.class);
        int count = CombatCodecs.count(buffer, ModInfo.Category.values().length);
        for (int i = 0; i < count; i++) {
            var category = buffer.readEnum(ModInfo.Category.class);
            if (scoped.put(category, readGroups(buffer)) != null) throw new IllegalArgumentException("Duplicate scoped Mod group");
        }
        return new ModStyle(visual, groups, scoped);
    }

    private static void writeGroups(RegistryFriendlyByteBuf buffer, Set<String> groups) {
        buffer.writeVarInt(groups.size());
        groups.forEach(group -> buffer.writeUtf(group, ModDefinition.MAX_ID_LENGTH));
    }

    private static Set<String> readGroups(RegistryFriendlyByteBuf buffer) {
        int count = CombatCodecs.count(buffer, ModStyle.MAX_GROUPS);
        var groups = new HashSet<String>();
        for (int i = 0; i < count; i++) {
            if (!groups.add(buffer.readUtf(ModDefinition.MAX_ID_LENGTH))) throw new IllegalArgumentException("Duplicate Mod group");
        }
        return groups;
    }

    private static void writeRiven(RegistryFriendlyByteBuf buffer, RivenRoll roll) {
        buffer.writeBoolean(roll != null);
        if (roll == null) return;
        buffer.writeUtf(roll.weapon(), ModDefinition.MAX_ID_LENGTH).writeEnum(roll.category()).writeEnum(roll.polarity());
        buffer.writeDouble(roll.disposition()).writeVarInt(roll.cycles()).writeVarInt(roll.lines().size());
        for (var line : roll.lines()) buffer.writeUtf(line.trait(), 80).writeDouble(line.quality()).writeBoolean(line.negative());
    }

    private static RivenRoll readRiven(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) return null;
        String weapon = buffer.readUtf(ModDefinition.MAX_ID_LENGTH);
        var category = buffer.readEnum(ModInfo.Category.class);
        var polarity = buffer.readEnum(ModInfo.Polarity.class);
        double disposition = buffer.readDouble();
        int cycles = buffer.readVarInt(), count = CombatCodecs.count(buffer, 4);
        var lines = new ArrayList<RivenRoll.Line>(count);
        for (int i = 0; i < count; i++) lines.add(new RivenRoll.Line(buffer.readUtf(80), buffer.readDouble(), buffer.readBoolean()));
        return new RivenRoll(weapon, category, polarity, disposition, lines, cycles);
    }
}
