package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

import java.util.*;

public record RivenUniqueInfo(String key, int weight, double baseValue) {

    public MutableComponent format(int modLevel) {
        double finalValue = baseValue * modLevel;
        return Component.translatable(key, finalValue);
    }

    public static final List<RivenUniqueInfo> MELEE_POSITIVE = List.of(
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addition", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.entity_interaction_range_add", 1, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.block_interaction_range_add", 2, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_chance_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_damage_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_chance_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_duration_multibase", 3, 12.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.attack_speed_multibase", 3, 6.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.movement_speed_multibase", 3, 3.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.combo_duration_add", 3, 1.25),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_penetration_add", 2, 1.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_toughness_penetration_add", 2, 1),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_penetration_percent_add", 2, 0.05),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_toughness_penetration_percent_add", 2, 0.04),
            new RivenUniqueInfo("tooltips.yagens_attributes.sweeping_damage_ratio_multibase", 2, 6.25)
    );

    public static final List<RivenUniqueInfo> MELEE_NEGATIVE = List.of(
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.entity_interaction_range_addneg", 1, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.block_interaction_range_addneg", 2, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_chance_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_damage_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_chance_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_duration_multibaseneg", 3, 12.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.attack_speed_multibaseneg", 3, 6.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.movement_speed_multibaseneg", 3, 3.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.combo_duration_addneg", 3, 1.25),
            new RivenUniqueInfo("tooltips.yagens_attributes.sweeping_damage_ratio_multibaseneg", 2, 6.25)

    );

    public static final List<RivenUniqueInfo> PROJECTILE_POSITIVE = List.of(
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addition", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.cold_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.heat_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.electricity_addition", 3, 11.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addfirst", 1, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addlast", 1, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.toxin_addition", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.entity_interaction_range_add", 1, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.block_interaction_range_add", 2, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_chance_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_damage_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_chance_multibase", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_duration_multibase", 3, 12.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.movement_speed_multibase", 3, 3.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.shoot_spread_multibase", 2, 6.25),
            new RivenUniqueInfo("tooltips.yagens_attributes.firerate_multibase", 2, 7.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_penetration_add", 2, 1.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_toughness_penetration_add", 2, 1),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_penetration_percent_add", 2, 0.05),
            new RivenUniqueInfo("tooltips.yagens_attributes.armor_toughness_penetration_percent_add", 2, 0.04),
            new RivenUniqueInfo("tooltips.yagens_attributes.multishot_multibase", 3, 11.0)
    );

    public static final List<RivenUniqueInfo> PROJECTILE_NEGATIVE = List.of(
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.slash_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.puncture_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addfirstneg", 3, 0.375),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_addlastneg", 2, 0.625),
            new RivenUniqueInfo("tooltip.yagens_attributes.impact_additionneg", 3, 15.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.entity_interaction_range_addneg", 1, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.block_interaction_range_addneg", 2, 0.15),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_chance_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.critical_damage_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_chance_multibaseneg", 3, 11.0),
            new RivenUniqueInfo("tooltips.yagens_attributes.status_duration_multibaseneg", 3, 12.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.movement_speed_multibaseneg", 3, 3.75),
            new RivenUniqueInfo("tooltips.yagens_attributes.shoot_spread_multibaseneg", 2, 6.25),
            new RivenUniqueInfo("tooltips.yagens_attributes.firerate_multibaseneg", 2, 7.5),
            new RivenUniqueInfo("tooltips.yagens_attributes.multishot_multibaseneg", 3, 11.0)
    );

    public static List<RivenUniqueInfo> draw(List<RivenUniqueInfo> pool, int n, RandomSource rnd) {
        if (n <= 0) return List.of();

        List<RivenUniqueInfo> weighted = new ArrayList<>();
        for (RivenUniqueInfo info : pool) {
            for (int i = 0; i < info.weight(); i++) {
                weighted.add(info);
            }
        }
        Collections.shuffle(weighted, new Random(rnd.nextLong()));
        return weighted.stream().distinct().limit(n).toList();
    }
}