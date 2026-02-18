package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_OFFHAND;
    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_COMBO;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_LEVEL_BONUS;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_BULLET_JUMP;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_AIR_BRAKE;
    public static final ModConfigSpec.ConfigValue<Integer> SPLASH_POTION_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> MAX_COMBO_LEVEL;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_LEVEL_NEEDED_COUNT;

    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_REACH;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_THE_EYE_OF_PAIN;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_TORTUROUS_ENTANGLEMENT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_SHARP_HORN_OF_RHISOIL;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_NEPHILA_SILK_BELT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_RESERVED_VITALITY;
    public static final ModConfigSpec.ConfigValue<Double> MOD_COMMON_BEAR_WING;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_ELEMENT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_BLADE_STORM;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_BONE_FINGER_BOX;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_FURY;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_ANTIQUATED_GLASSES;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_CLOUD_PIERCING_BOOTS;
    public static final ModConfigSpec.ConfigValue<Double> MOD_UNCOMMON_STRETCH;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_STATUS_ELEMENT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_PHYSICAL;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_HARVEST_TIME;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_NAMELESS_GLOVE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_FLAME_FEATHER;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_SCOPE;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_RARE_SCOPE_DURATION;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_MULTIPLY_SHOT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_SCATTER_SHOT_MULTISHOT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_SCATTER_SHOT_SPREAD;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_SCATTER_SHOT_DAMAGE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_GRACEFULLY_SERPENTINE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_THORN_AURA_INCREASE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_THORN_AURA_DECREASE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_SPRING_SHOES;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_BOUNTY_HUNTER;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_BERSERKER_FURY;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_RARE_BERSERKER_FURY_DURATION;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_RARE_BERSERKER_FURY_STACK;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_FLOW;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_ANCIENT_STABILIZER;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_OVEREXTENDED_RANGE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_OVEREXTENDED_STRENGTH;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_CONTINUITY;
    public static final ModConfigSpec.ConfigValue<Double> MOD_RARE_MEDITATION_RUNE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_EDGE_DISC;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_STATUS_HEAL;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_KILLBONUS;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_STACK;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_GALVANIZED_SCOPE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_GALVANIZED_SCOPE_KILLBONUS;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_LEGENDARY_GALVANIZED_SCOPE_STACK;
    public static final ModConfigSpec.ConfigValue<Double> MOD_LEGENDARY_CONDITION_OVERLOAD;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_WARFRAME_COLLABORATIVE_PROFICIENCY;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_WARFRAME_NOURISH_DURATION;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_NOURISH_MAX_COUNT;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_NOURISH_MAX_ENHANCE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_NOURISH_INIT_ENHANCE;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_WARFRAME_RESERVOIRS_DURATION;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_RESERVOIRS_RANGE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_RESERVOIRS_DURATION_FACTOR;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_RESERVOIRS_RANGE_FACTOR;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_RESERVOIRS_STRENGTH_FACTOR;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_WARFRAME_BLADE_STORM_DURATION;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_BLADE_STORM_RANGE;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_THERMAL_SUNDER;
    public static final ModConfigSpec.ConfigValue<Double> MOD_WARFRAME_THERMAL_SUNDER_RANGE;
    public static final ModConfigSpec.ConfigValue<Integer> MOD_WARFRAME_THERMAL_SUNDER_DURATION;

    static {

        BUILDER.push("Optimize for Vanilla");
        BUILDER.comment("The cooldown you use splash potion or lingering potion (default = 20, Integer, tick)");
        SPLASH_POTION_COOLDOWN = BUILDER.defineInRange("Potion_Cooldown", 20, 0, 100);
        BUILDER.pop();

        BUILDER.push("Ban");
        BUILDER.comment("Ban use Meat Shredder(cataclysm) in your offhand? (default = true)");
        BAN_MEAT_SHREDDER_OFFHAND = BUILDER.define("Ban Meat Shredder Offhand", true);
        BUILDER.comment("Ban use Meat Shredder(cataclysm) to get combo count? (default = true)");
        BAN_MEAT_SHREDDER_COMBO = BUILDER.define("Ban_Meat_Shredder_Combo", true);
        BUILDER.pop();

        BUILDER.push("Combo");
        BUILDER.comment("Max Combo Level bonuses your attributes by your Mods, this value is better not to be too large.");
        BUILDER.comment("Max Combo Level (default = 14, Integer)");
        MAX_COMBO_LEVEL = BUILDER.define("Max_Combo_Level", 14);
        BUILDER.comment("The number of combo required for each level.");
        BUILDER.comment("Combo Level Needed Count (default = 10, Integer)");
        COMBO_LEVEL_NEEDED_COUNT = BUILDER.define("Combo_Level_Needed_Count", 10);
        BUILDER.pop();

        BUILDER.push("Entity_Level");
        BUILDER.comment("Whether to active Level bonus for entities (default = true)");
        IF_LEVEL_BONUS = BUILDER.define("If_Level_Bonus", true);
        BUILDER.pop();

        BUILDER.push("Motion");
        BUILDER.comment("Whether to active bullet jump (default = true)");
        IF_BULLET_JUMP = BUILDER.define("If_Bullet_Jump", true);
        BUILDER.comment("Whether to active air brake (default = true)");
        IF_AIR_BRAKE = BUILDER.define("If_Air_Brake", true);
        BUILDER.pop();

        /**
         * Mod config
         */
        BUILDER.push("Mod");

        BUILDER.push("Common_Reach");
        BUILDER.comment("Value of Common Reach Mod per level (default = 0.3)");
        MOD_COMMON_REACH = BUILDER.define("Value", 0.3);
        BUILDER.pop();

        BUILDER.push("Common_The_Eye_of_Pain");
        BUILDER.comment("Value of Common The Eye of Pain Mod per level (default = 18.0)");
        MOD_COMMON_THE_EYE_OF_PAIN = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Common_Torturous_Entanglement");
        BUILDER.comment("Value of Common Torturous Entanglement Mod per level (default = 11.0)");
        MOD_COMMON_TORTUROUS_ENTANGLEMENT = BUILDER.define("Value", 11.0);
        BUILDER.pop();

        BUILDER.push("Common_Sharp_Horn_of_Rhisoil");
        BUILDER.comment("Value of Common Sharp Horn of Rhisoil Mod per level (default = 5.0)");
        MOD_COMMON_SHARP_HORN_OF_RHISOIL = BUILDER.define("Value", 5.0);
        BUILDER.pop();

        BUILDER.push("Common_Nephila_Silk_Belt");
        BUILDER.comment("Value of Common Nephila Silk Belt Mod per level (default = 2.5)");
        MOD_COMMON_NEPHILA_SILK_BELT = BUILDER.define("Value", 2.5);
        BUILDER.pop();

        BUILDER.push("Common_Reserved_Vitality");
        BUILDER.comment("Value of Common Reserved Vitality Mod per level (default = 15.0)");
        MOD_COMMON_RESERVED_VITALITY = BUILDER.define("Value", 15.0);
        BUILDER.pop();

        BUILDER.push("Common_Bear_Wing");
        BUILDER.comment("Value of Common Bear Wing Mod per level (default = 8.0, 0.0--20.0)");
        MOD_COMMON_BEAR_WING = BUILDER.define("Value", 8.0);
        BUILDER.pop();

        BUILDER.push("Uncommon_Element");
        BUILDER.comment("Value of Uncommon Element Mods per level (default = 18.0)");
        MOD_UNCOMMON_ELEMENT = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Uncommon_Blade_Storm");
        BUILDER.comment("Value of Uncommon Blade Storm Mod per level (default = 0.15)");
        MOD_UNCOMMON_BLADE_STORM = BUILDER.define("Value", 0.15);
        BUILDER.pop();

        BUILDER.push("Uncommon_Bone_Finger_Box");
        BUILDER.comment("Value of Uncommon Bone Finger Box Mod per level (default = 18.0)");
        MOD_UNCOMMON_BONE_FINGER_BOX = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Uncommon_Fury");
        BUILDER.comment("Value of Uncommon Fury Mod per level (default = 6.0)");
        MOD_UNCOMMON_FURY = BUILDER.define("Value", 6.0);
        BUILDER.pop();

        BUILDER.push("Uncommon_Antiquated_Glasses");
        BUILDER.comment("Value of Uncommon Antiquated Glasses Mod per level (default = 0.18)");
        MOD_UNCOMMON_ANTIQUATED_GLASSES = BUILDER.define("Value", 0.18);
        BUILDER.pop();

        BUILDER.push("Uncommon_Cloud_Piercing_Boots");
        BUILDER.comment("Value of Uncommon Cloud Piercing Boots Mod per level (default = 10.0)");
        MOD_UNCOMMON_CLOUD_PIERCING_BOOTS = BUILDER.define("Value", 10.0);
        BUILDER.pop();

        BUILDER.push("Uncommon_Stretch");
        BUILDER.comment("Value of Uncommon Stretch Mod per level (default = 9.0)");
        MOD_UNCOMMON_STRETCH = BUILDER.define("Value", 9.0);
        BUILDER.pop();

        BUILDER.push("Rare_Status_Element");
        BUILDER.comment("Value of Rare Status Element Mods per level (default = 12.0)");
        MOD_RARE_STATUS_ELEMENT = BUILDER.define("Value", 12.0);
        BUILDER.pop();

        BUILDER.push("Rare_Physical");
        BUILDER.comment("Value of Rare Physical Mods per level (default = 18.0)");
        MOD_RARE_PHYSICAL = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Rare_Harvest_Time");
        BUILDER.comment("Value of Rare Harvest Time Mod per level (default = 2.0)");
        MOD_RARE_HARVEST_TIME = BUILDER.define("Value", 2.0);
        BUILDER.pop();

        BUILDER.push("Rare_Nameless_Glove");
        BUILDER.comment("Value of Rare Nameless Glove Mod per level (default = 18.0)");
        MOD_RARE_NAMELESS_GLOVE = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Rare_Flame_Feather");
        BUILDER.comment("Value of Rare Flame Feather Mod per level (default = 18.0)");
        MOD_RARE_FLAME_FEATHER = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Rare_Scope");
        BUILDER.comment("Value of Rare Scope Mod per level (default = 27.0)");
        MOD_RARE_SCOPE = BUILDER.define("Value", 27.0);
        BUILDER.comment("Duration of Rare Scope Mod kill bonus (default = 12, seconds)");
        MOD_RARE_SCOPE_DURATION = BUILDER.define("Duration", 12);
        BUILDER.pop();

        BUILDER.push("Rare_Multiply_Shot");
        BUILDER.comment("Value of Rare Multiply Shot Mod per level (default = 18.0)");
        MOD_RARE_MULTIPLY_SHOT = BUILDER.define("Value", 18.0);
        BUILDER.pop();

        BUILDER.push("Rare_Scattered_Shot");
        BUILDER.comment("Multishot of Rare Scattered Shot Mod per level (default = 60.0)");
        MOD_RARE_SCATTER_SHOT_MULTISHOT = BUILDER.define("Multishot", 60.0);
        BUILDER.comment("Spread of Rare Scattered Shot Mod per level (default = 0.8)");
        MOD_RARE_SCATTER_SHOT_SPREAD = BUILDER.define("Spread", 0.8);
        BUILDER.comment("Damage of Rare Scattered Shot Mod each arrow (default = 0.4)");
        MOD_RARE_SCATTER_SHOT_DAMAGE = BUILDER.define("Damage", 0.4);
        BUILDER.pop();

        BUILDER.push("Rare_Gracefully_Serpentine");
        BUILDER.comment("Value of Rare Gracefully Serpentine Mod per level (default = 0.05, 0.1 + value * level)");
        MOD_RARE_GRACEFULLY_SERPENTINE = BUILDER.define("Value", 0.05);
        BUILDER.pop();

        BUILDER.push("Rare_Thorn_Aura");
        BUILDER.comment("Increase Value of Rare Thorn Aura Mod per level (default = 2.0)");
        MOD_RARE_THORN_AURA_INCREASE = BUILDER.define("Increase", 2.0);
        BUILDER.comment("Decrease Value of Rare Thorn Aura Mod per level (default = 1.0)");
        MOD_RARE_THORN_AURA_DECREASE = BUILDER.define("Decrease", 1.0);
        BUILDER.pop();

        BUILDER.push("Rare_Spring_Shoes");
        BUILDER.comment("Value of Rare Spring Shoes Mod per level (default = 10.0)");
        MOD_RARE_SPRING_SHOES = BUILDER.define("Value", 10.0);
        BUILDER.pop();

        BUILDER.push("Rare_Bounty_Hunter");
        BUILDER.comment("Value of Rare Bounty Hunter Mod per level (default = 40.0)");
        MOD_RARE_BOUNTY_HUNTER = BUILDER.define("Value", 40.0);
        BUILDER.pop();

        BUILDER.push("Rare_Berserker_Fury");
        BUILDER.comment("Value of Berserker Fury Mod per level (default = 7.0)");
        MOD_RARE_BERSERKER_FURY = BUILDER.define("Value", 7.0);
        BUILDER.comment("Duration of Berserker Fury Mod (default = 10)");
        MOD_RARE_BERSERKER_FURY_DURATION = BUILDER.define("Duration", 10);
        BUILDER.comment("Stack of Berserker Fury Mod bonus (default = 2)");
        MOD_RARE_BERSERKER_FURY_STACK = BUILDER.define("Stack", 2);
        BUILDER.pop();

        BUILDER.push("Rare_Flow");
        BUILDER.comment("Value of Rare Flow Mod per level (default = 20.0)");
        MOD_RARE_FLOW = BUILDER.define("Value", 20.0);
        BUILDER.pop();

        BUILDER.push("Rare_Ancient_Stabilizer");
        BUILDER.comment("Value of Rare Ancient Stabilizer Mod per level (default = 6.0)");
        MOD_RARE_ANCIENT_STABILIZER = BUILDER.define("Value", 6.0);
        BUILDER.pop();

        BUILDER.push("Rare_Overextended");
        BUILDER.comment("Range Value of Rare Overextended Mod per level (default = 18.0)");
        MOD_RARE_OVEREXTENDED_RANGE = BUILDER.define("Range", 18.0);
        BUILDER.comment("Strength Value of Rare Overextended Mod per level (default = 12.0, negative)");
        MOD_RARE_OVEREXTENDED_STRENGTH = BUILDER.define("Strength", 12.0);
        BUILDER.pop();

        BUILDER.push("Rare_Continuity");
        BUILDER.comment("Value of Rare Continuity Mod per level (default = 6.0)");
        MOD_RARE_CONTINUITY = BUILDER.define("Value", 6.0);
        BUILDER.pop();

        BUILDER.push("Rare_Meditation_Rune");
        BUILDER.comment("Value of Rare Meditation Rune Mod per level (default = 6.0)");
        MOD_RARE_MEDITATION_RUNE = BUILDER.define("Value", 6.0);
        BUILDER.pop();

        BUILDER.push("Legendary_Edge_Disc");
        BUILDER.comment("Value of Legendary Edge Disc Mod per level (default = 15.0)");
        MOD_LEGENDARY_EDGE_DISC = BUILDER.define("Value", 15.0);
        BUILDER.pop();

        BUILDER.push("Legendary_Status_Heal");
        BUILDER.comment("Value of Legendary Status Heal Mod per level (default = 1.0)");
        MOD_LEGENDARY_STATUS_HEAL = BUILDER.define("Value", 1.0);
        BUILDER.pop();

        BUILDER.push("Legendary_Galvanized_Multiply_Shot");
        BUILDER.comment("Value of Legendary Galvanized Multiply Shot Mod per level (default = 8.0)");
        MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT = BUILDER.define("Base", 8.0);
        BUILDER.comment("Value of Legendary Galvanized Multiply Shot Mod for kill bonus per stack (default = 0.3)");
        MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_KILLBONUS = BUILDER.define("Killbonus", 0.3);
        BUILDER.comment("Duration of Legendary Galvanized Multiply Shot Mod kill bonus (default = 20, seconds)");
        MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION = BUILDER.define("Duration", 20);
        BUILDER.comment("Size of Legendary Galvanized Multiply Shot Mod Stack (default = 5, seconds)");
        MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_STACK = BUILDER.define("Stacks", 5);
        BUILDER.pop();

        BUILDER.push("Legendary_Galvanized_Scope");
        BUILDER.comment("Modification Value (decline from Scope Mod, 27/2=13.5 per level) of Legendary Galvanized Scope Mod per level (default = 1.5)");
        MOD_LEGENDARY_GALVANIZED_SCOPE = BUILDER.define("Base", 1.5);
        BUILDER.comment("Value of Legendary Galvanized Scope Mod for kill bonus per stack, duration is as the same as Scope Mod (default = 0.4)");
        MOD_LEGENDARY_GALVANIZED_SCOPE_KILLBONUS = BUILDER.define("Killbonus", 0.4);
        BUILDER.comment("Size of Legendary Galvanized Scope Mod Stack (default = 5, seconds)");
        MOD_LEGENDARY_GALVANIZED_SCOPE_STACK = BUILDER.define("Stacks", 5);
        BUILDER.pop();

        BUILDER.push("Legendary_Condition_Overload");
        BUILDER.comment("Value of Legendary Condition Overload Mod per level (default = 12.0)");
        MOD_LEGENDARY_CONDITION_OVERLOAD = BUILDER.define("Value", 12.0);
        BUILDER.pop();

        BUILDER.push("Warframe_Collaborative_Proficiency");
        BUILDER.comment("Value of Warframe Collaborative Proficiency Mod per level (default = 50, 0--100)");
        MOD_WARFRAME_COLLABORATIVE_PROFICIENCY = BUILDER.define("Value", 50);
        BUILDER.pop();

        BUILDER.push("Warframe_Nourish");
        BUILDER.comment("Duration of Warframe Nourish Mod (default = 30, seconds)");
        MOD_WARFRAME_NOURISH_DURATION = BUILDER.define("Value", 30);
        BUILDER.comment("Max Count of Warframe Nourish Mod, determined the speed at which the max enhance is achieved (default = 1024.0)");
        MOD_WARFRAME_NOURISH_MAX_COUNT = BUILDER.define("Max_Count", 1024.0);
        BUILDER.comment("Init Enhance of Warframe Nourish Mod (default = 1.05)");
        MOD_WARFRAME_NOURISH_INIT_ENHANCE = BUILDER.define("Init_Enhance", 1.05);
        BUILDER.comment("Max Enhance of Warframe Nourish Mod (default = 1.5)");
        MOD_WARFRAME_NOURISH_MAX_ENHANCE = BUILDER.define("Max_Enhance", 1.5);
        BUILDER.pop();

        BUILDER.push("Warframe_Reservoirs");
        BUILDER.comment("Duration of Warframe Reservoirs Mod (default = 30, seconds)");
        MOD_WARFRAME_RESERVOIRS_DURATION = BUILDER.define("Duration", 30);
        BUILDER.comment("Range of Warframe Reservoirs Mod (default = 4.0)");
        MOD_WARFRAME_RESERVOIRS_RANGE = BUILDER.define("Range", 4.0);
        BUILDER.comment("Duration Factor of Warframe Reservoirs Mod (default = 0.06)");
        MOD_WARFRAME_RESERVOIRS_DURATION_FACTOR = BUILDER.define("Duration_Factor", 0.06);
        BUILDER.comment("Strength Factor of Warframe Reservoirs Mod (default = 0.06)");
        MOD_WARFRAME_RESERVOIRS_STRENGTH_FACTOR = BUILDER.define("Strength_Factor", 0.06);
        BUILDER.comment("Range Factor of Warframe Reservoirs Mod (default = 0.2)");
        MOD_WARFRAME_RESERVOIRS_RANGE_FACTOR = BUILDER.define("Range_Factor", 0.2);
        BUILDER.pop();

        BUILDER.push("Warframe_Blade_Storm");
        BUILDER.comment("Duration of Warframe Blade Storm Mod (default = 5, seconds)");
        MOD_WARFRAME_BLADE_STORM_DURATION = BUILDER.define("Duration", 5);
        BUILDER.comment("Range of Warframe Blade Storm Mod (default = 20.0)");
        MOD_WARFRAME_BLADE_STORM_RANGE = BUILDER.define("Range", 20.0);
        BUILDER.pop();

        BUILDER.push("Warframe_Thermal_Sunder");
        BUILDER.comment("Base Damage of Warframe Thermal Sunder Mod (default = 2.0)");
        MOD_WARFRAME_THERMAL_SUNDER = BUILDER.define("Base_Damage", 2.0);
        BUILDER.comment("Duration of Warframe Thermal Sunder Mod (default = 30, seconds)");
        MOD_WARFRAME_THERMAL_SUNDER_DURATION = BUILDER.define("Duration", 30);
        BUILDER.comment("Range of Warframe Thermal Sunder Mod (default = 10.0)");
        MOD_WARFRAME_THERMAL_SUNDER_RANGE = BUILDER.define("Range", 10.0);
        BUILDER.pop();

        BUILDER.pop();
        /**
         * Mod config
         */

        SPEC = BUILDER.build();
    }
}
