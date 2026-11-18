package yagen.waitmydawn.yagens_attributes.text;

import net.minecraft.network.chat.*;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;
import yagen.waitmydawn.yagens_attributes.core.ModInfo;
import yagen.waitmydawn.yagens_attributes.client.StatText;
import yagen.waitmydawn.yagens_attributes.network.PanelPayload;

import java.util.*;

/**
 * Shared item, card-tooltip and detail-pane metadata presentation.
 */
public final class ModText {
    private ModText() {
    }

    public static boolean maxLevel(ModInfo info) {
        return info.defined() && info.level() == info.details().maxLevel();
    }

    public static Component rank(ModInfo info) {
        var result = Component.translatable("gui.yagens_attributes.mod_level", info.level());
        if (info.riven() != null && info.riven().cycles() > 0) result.append("  ").append(Component.literal("\uE109").withStyle(Style.EMPTY.withFont(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("yagens_attributes", "mod_symbols")))).append(" " + info.riven().cycles());
        return maxLevel(info) ? result.withColor(0x55FFFF) : result;
    }

    public static Component name(ModInfo info, String name) {
        return info.riven() == null ? Component.literal(name).withColor(info.color()) : localizedName(info);
    }
    public static Component localizedName(ModInfo info) {
        var result = Component.translatable(info.name()).withColor(info.color());
        if (info.riven() != null) result.append(" · ").append(weaponName(info.riven().weapon()));
        return result;
    }
    private static Component weaponName(String id) {
        var key = net.minecraft.resources.ResourceLocation.parse(id);
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(key)
                ? net.minecraft.core.registries.BuiltInRegistries.ITEM.get(key).getDefaultInstance().getHoverName() : Component.literal(id);
    }

    public static List<Component> bonuses(ModInfo info, Map<String, PanelPayload.ModifierView> bonuses) {
        var positive = new ArrayList<Component>(); var negative = new ArrayList<Component>();
        if (info.details() != null) for (var entry : info.details().conditional()) {
            String amount = (entry.addition() >= 0 ? "+" : "") + StatText.number(entry.addition() * 100) + "%";
            var rule = entry.condition();
            var stat = StatText.label(entry.stat());
            var time = StatText.number(rule.durationSeconds());
            var line = switch (rule.trigger()) {
                case TARGET_STATUS_TYPES -> Component.translatable("gui.yagens_attributes.per_status_type", amount, stat);
                case RANGED_WEAK_POINT_HITS -> Component.translatable("gui.yagens_attributes."
                        + (rule.maxStacks() == 1 ? "on_weak_point_hit" : "on_weak_point_hit_stacks"), amount, stat, time, rule.maxStacks());
                case RANGED_WEAK_POINT_KILLS -> Component.translatable("gui.yagens_attributes.on_weak_point_kill",
                        amount, stat, time, rule.maxStacks());
                case MELEE_KILLS, RANGED_KILLS -> Component.translatable("gui.yagens_attributes."
                        + (rule.trigger() == ConditionalModifier.Trigger.RANGED_KILLS ? "per_ranged_kill" : "per_melee_kill")
                        + (rule.perTargetStatus() ? "_per_status_type" : ""), amount, stat, time, rule.maxStacks());
                case ALWAYS -> throw new IllegalStateException("Unconditional effect in conditional tooltip");
            };
            if (rule.decay() == ConditionalModifier.Decay.INDEPENDENT)
                line = line.copy().append(Component.translatable("gui.yagens_attributes.independent_stacks"));
            (entry.addition() < 0 ? negative : positive).add(line);
        }
        return StatText.bonuses(bonuses, positive, negative).stream().map(line -> (Component) line.copy().withColor(info.color())).toList();
    }

    public static List<Component> properties(ModInfo info, Map<String, PanelPayload.ModifierView> bonuses) {
        var result = new ArrayList<Component>();
        if (info.riven() == null) result.add(scope(info));
        result.add(metadata(info));
        result.addAll(bonuses(info, bonuses));
        return List.copyOf(result);
    }

    public static List<Component> tooltip(ModInfo info, String name, Map<String, PanelPayload.ModifierView> bonuses, boolean shared, UUID weapon, boolean expanded) {
        var result = new ArrayList<Component>();
        result.add(name(info, name));
        result.addAll(properties(info, bonuses));
        if (expanded) {
            result.add(Component.translatable("gui.yagens_attributes." + (shared ? "shared" : "private")));
            if (weapon != null)
                result.add(Component.translatable("gui.yagens_attributes.bound_weapon", weapon.toString()));
        }
        return List.copyOf(result);
    }

    public static Component scope(ModInfo info) {
        MutableComponent result = Component.empty();
        for (var category : info.applicability().stream().sorted().toList()) {
            if (!result.getSiblings().isEmpty()) result.append(" ");
            result.append(Component.translatable("category.yagens_attributes." + category.key()));
        }
        return result;
    }

    public static Component metadata(ModInfo info) {
        var symbol = Component.literal(info.polaritySymbol());
        if (!info.polaritySymbol().isEmpty() && info.polaritySymbol().chars().allMatch(ch -> yagen.waitmydawn.yagens_attributes.core.ui.PolarityGlyphs.type((char) ch) != null))
            symbol.withStyle(Style.EMPTY.withFont(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("yagens_attributes", "polarity_icons")));
        return Component.empty().append(rank(info))
                .append("  ").append(symbol)
                .append("  ").append(Component.translatable("gui.yagens_attributes.capacity_cost", info.capacityCost()));
    }
}
