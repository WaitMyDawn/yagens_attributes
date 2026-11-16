package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.PanelPayload;
import java.util.*;

public final class StatText {
    public static final ResourceLocation DAMAGE_FONT = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "damage_icons");
    private StatText() {}
    public static Component label(String stat) { return Component.translatable("stat.yagens_attributes." + stat); }
    public static String number(double value) { return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", ""); }
    public static String value(String stat, double value) {
        if (stat.equals(Stats.CRITICAL_CHANCE) || stat.equals(Stats.STATUS_CHANCE) || stat.equals(Stats.COMBO_COUNT_CHANCE)) return number(value * 100) + "%";
        if (stat.equals(Stats.CRITICAL_DAMAGE) || stat.equals(Stats.DAMAGE) || stat.equals(Stats.FIRE_RATE) || stat.equals(Stats.ATTACK_SPEED)) return number(value) + "x";
        if (stat.equals(Stats.STATUS_DURATION) || stat.equals(Stats.COMBO_DURATION)) return number(value) + " s";
        if (stat.equals(Stats.MELEE_RANGE)) return number(value) + " m";
        return number(value);
    }
    public static List<Component> base(WeaponProfile profile) {
        return keys(profile).stream().map(key -> line(key, Stats.base(profile, key))).toList();
    }
    public static List<String> keys(WeaponProfile profile) {
        var result = new ArrayList<>(List.of(Stats.CRITICAL_CHANCE, Stats.CRITICAL_DAMAGE, Stats.STATUS_CHANCE, Stats.STATUS_DURATION));
        if (ModInfo.Category.RANGED.contains(profile.category()) || profile.category() == ModInfo.Category.TRIDENT) result.addAll(List.of(Stats.FIRE_RATE, Stats.MULTISHOT));
        if (ModInfo.Category.MELEE.contains(profile.category())) result.addAll(List.of(Stats.ATTACK_SPEED, Stats.MELEE_RANGE, Stats.COMBO_DURATION, Stats.COMBO_COUNT_CHANCE));
        return List.copyOf(result);
    }
    private static Component line(String key, double value) { return label(key).copy().append(": " + value(key, value)); }

    public static Component damageLabel(DamageType type) {
        var icon = Component.literal(String.valueOf((char) (0xE000 + type.ordinal()))).withStyle(Style.EMPTY.withFont(DAMAGE_FONT).withColor(0xFFFFFF));
        return Component.empty().append(icon).append(" ").append(label("damage." + type.key()));
    }
    public static List<Component> composition(WeaponProfile profile, Map<String, Double> resolved) {
        List<Component> result = new ArrayList<>();
        for (DamageType type : DamageType.DISPLAY_ORDER) {
            double value = resolved.getOrDefault("damage." + type.key(), profile.baseDamage() * profile.distribution().getOrDefault(type, 0D));
            if (value > 0) result.add(damageLabel(type).copy().append(": " + number(value)));
        }
        return List.copyOf(result);
    }

    public static List<Component> bonuses(Map<String, PanelPayload.ModifierView> bonuses) {
        return bonuses(bonuses, List.of(), List.of());
    }
    public static List<Component> bonuses(Map<String, PanelPayload.ModifierView> bonuses, List<Component> positiveExtra, List<Component> negativeExtra) {
        List<Component> result = new ArrayList<>(), negative = new ArrayList<>();
        for (String stat : Stats.KEYS) {
            var bonus = bonuses.get(stat);
            if (bonus == null) continue;
            if (bonus.addin() != 0) (bonus.addin() < 0 ? negative : result).add(modifier(flat(stat, bonus.addin()), stat));
            if (bonus.inputAddin() != 0) (bonus.inputAddin() < 0 ? negative : result).add(modifier(signed(bonus.inputAddin() * 100) + "%", stat));
            if (bonus.inputAddition() != 0) (bonus.inputAddition() < 0 ? negative : result).add(modifier(signed(bonus.inputAddition() * 100) + "%", stat));
            if (bonus.addition() != 0) (bonus.addition() < 0 ? negative : result).add(modifier(signed(bonus.addition() * 100) + "%", stat));
            if (!bonus.multiply().isEmpty()) (bonus.multiplyNegative() ? negative : result).add(modifier("x" + bonus.multiply(), stat));
            if (bonus.addout() != 0) (bonus.addout() < 0 ? negative : result).add(modifier(flat(stat, bonus.addout()), stat));
        }
        result.addAll(positiveExtra); result.addAll(negative); result.addAll(negativeExtra); return result;
    }
    private static String signed(double value) { return (value >= 0 ? "+" : "") + number(value); }
    private static String flat(String stat, double value) {
        if (stat.equals(Stats.CRITICAL_CHANCE) || stat.equals(Stats.STATUS_CHANCE) || stat.equals(Stats.COMBO_COUNT_CHANCE)) return signed(value * 100) + "%";
        return signed(value) + (stat.equals(Stats.STATUS_DURATION) || stat.equals(Stats.COMBO_DURATION) ? " s" : stat.equals(Stats.MELEE_RANGE) ? " m" : "");
    }
    private static Component modifier(String value, String stat) {
        return Component.literal(value + " ").append(modifierLabel(stat));
    }
    public static Component modifierLabel(String stat) {
        return Stats.DAMAGE_COMPONENTS.contains(stat)
                ? Component.translatable("gui.yagens_attributes.damage_component", label(stat)) : label(stat);
    }
}
