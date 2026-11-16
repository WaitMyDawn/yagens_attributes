package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.WeaponInspectionData;
import java.util.*;

/** Inventory values come from an authoritative inspection; template previews are explicitly identified. */
public final class WeaponTooltip {
    private WeaponTooltip() {}
    public static boolean complete(WeaponProfile profile, WeaponInspectionData data) {
        return data != null && data.resolved().keySet().containsAll(StatText.keys(profile))
                && profile.distribution().keySet().stream().allMatch(type -> data.resolved().containsKey("damage." + type.key()));
    }
    public static List<Component> lines(WeaponProfile profile, WeaponInspectionData inspection, boolean preview) {
        var result = new ArrayList<Component>();
        result.add(Component.translatable(preview ? "tooltip.yagens_attributes.preview" : "gui.yagens_attributes.base").withStyle(ChatFormatting.GOLD));
        if (!preview && !complete(profile, inspection)) {
            result.add(Component.translatable("tooltip.yagens_attributes.syncing").withStyle(ChatFormatting.DARK_GRAY));
            return List.copyOf(result);
        }
        Map<String, Double> values = preview ? Map.of() : inspection.resolved();
        var progress = preview ? WeaponProgress.ZERO : inspection.progress();
        result.add(Component.translatable("gui.yagens_attributes.weapon_level", progress.level()).withStyle(ChatFormatting.GRAY));
        if (progress.level() < WeaponProgress.MAX_LEVEL) result.add(Component.translatable("gui.yagens_attributes.weapon_experience", progress.experience(), progress.nextLevelExperience()).withStyle(ChatFormatting.DARK_GRAY));
        for (String key : StatText.keys(profile)) {
            double value = preview ? Stats.base(profile, key) : values.get(key);
            result.add(StatText.label(key).copy().append(": " + StatText.value(key, value)).withStyle(ChatFormatting.GRAY));
        }
        result.add(Component.translatable("gui.yagens_attributes.composition").withStyle(ChatFormatting.GOLD));
        for (var line : StatText.composition(profile, values)) result.add(line.copy().withStyle(ChatFormatting.GRAY));
        double total = 0;
        for (var type : DamageType.values()) total += preview ? profile.baseDamage() * profile.distribution().getOrDefault(type, 0D) : values.getOrDefault("damage." + type.key(), 0D);
        result.add(Component.translatable("gui.yagens_attributes.total_damage").append(": " + StatText.number(total)).withStyle(ChatFormatting.GOLD));
        return List.copyOf(result);
    }
}
