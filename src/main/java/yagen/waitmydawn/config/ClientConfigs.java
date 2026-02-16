package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_Y;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_Y;
    public static final ModConfigSpec.ConfigValue<Integer> ENERGY_BAR_X;
    public static final ModConfigSpec.ConfigValue<Integer> ENERGY_BAR_Y;
    public static final ModConfigSpec.ConfigValue<Integer> BATTERY_X;
    public static final ModConfigSpec.ConfigValue<Integer> BATTERY_Y;
    public static final ModConfigSpec.ConfigValue<Integer> RESERVOIRS_X;
    public static final ModConfigSpec.ConfigValue<Integer> RESERVOIRS_Y;

    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_MISSION_POSITION;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_MISSION_SUMMON;

    public static final ModConfigSpec.ConfigValue<Boolean> LV_CONTENT;
    public static final ModConfigSpec.ConfigValue<Boolean> LV_CONTENT_ENLARGE;
    public static final ModConfigSpec.ConfigValue<Boolean> BOSS_LV_SEE_THROUGH;
    public static final ModConfigSpec.ConfigValue<Integer> LV_BOSS_COLOR;

    public static final ModConfigSpec.ConfigValue<Double> DAMAGE_NUMBER_ENLARGE;


    public static final ModConfigSpec.ConfigValue<Boolean> IF_BULLET_JUMP;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_AIR_BRAKE;

    public static final ModConfigSpec.ConfigValue<Double> HEADSHOT_VOLUME;
    public static final ModConfigSpec.ConfigValue<Double> HEADSHOT_PITCH;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_HEADSHOT_SOUND;

    public static final ModConfigSpec.ConfigValue<Boolean> IF_THERMAL_SUNDER_SOUND;
    public static final ModConfigSpec.ConfigValue<Double> THERMAL_SUNDER_VOLUME;
    public static final ModConfigSpec.ConfigValue<Double> THERMAL_SUNDER_PITCH;
    /**
     * Damage Type Particle
     */
    public static final ModConfigSpec.ConfigValue<Boolean> ELECTRICITY_VALID;
    public static final ModConfigSpec.ConfigValue<Double> ELECTRICITY_BRANCH_CHANCE;
    public static final ModConfigSpec.ConfigValue<Double> ELECTRICITY_JITTER_STRENGTH;
    public static final ModConfigSpec.ConfigValue<Double> ELECTRICITY_SIZE;
    public static final ModConfigSpec.ConfigValue<Integer> ELECTRICITY_MAX_GENERATIONS;
    public static final ModConfigSpec.ConfigValue<Integer> ELECTRICITY_BRANCH_GENERATION_LIMIT;
    public static final ModConfigSpec.ConfigValue<Integer> ELECTRICITY_REFRESH_RATE;
    public static final ModConfigSpec.ConfigValue<Integer> ELECTRICITY_LIFE_TIME;

    public static final ModConfigSpec.ConfigValue<Boolean> HEAT_VALID;
    public static final ModConfigSpec.ConfigValue<Integer> HEAT_LIFE_TIME;
    public static final ModConfigSpec.ConfigValue<Double> HEAT_SIZE;

    public static final ModConfigSpec.ConfigValue<Boolean> BLAST_VALID;
    public static final ModConfigSpec.ConfigValue<Integer> BLAST_EACH_LEVEL;
    public static final ModConfigSpec.ConfigValue<Integer> BLAST_MAX_COUNT;
    public static final ModConfigSpec.ConfigValue<Double> BLAST_EACH_DISTANCE;

    public static final ModConfigSpec.ConfigValue<Boolean> COLD_VALID;
    public static final ModConfigSpec.ConfigValue<Double> COLD_SUMMON_CHANCE;

    public static final ModConfigSpec.ConfigValue<Boolean> TOXIN_VALID;
    public static final ModConfigSpec.ConfigValue<Integer> TOXIN_LIFE_TIME;
    public static final ModConfigSpec.ConfigValue<Double> TOXIN_SIZE;

    public static final ModConfigSpec.ConfigValue<Boolean> GAS_VALID;
    public static final ModConfigSpec.ConfigValue<Integer> GAS_LIFE_TIME;
    public static final ModConfigSpec.ConfigValue<Double> GAS_MAX_SIZE;

    public static final ModConfigSpec.ConfigValue<Boolean> IF_REVERSAL_THERMAL_SUNDER;

    static {
        BUILDER.push("ComboHUD");
        BUILDER.comment("You can change this by an operation screen in game");
        BUILDER.comment("X position of the combo counter (-1 = center)");
        COMBO_HUD_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of the combo counter (-1 = center)");
        COMBO_HUD_Y = BUILDER.define("Y", -1);
        BUILDER.pop();

        BUILDER.push("MissionHUD");
        BUILDER.comment("You can change this by an operation screen in game");
        BUILDER.comment("X position of the mission counter (-1 = left)");
        MISSION_HUD_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of the mission counter (-1 = screenHeight / 4)");
        MISSION_HUD_Y = BUILDER.define("Y", -1);
        BUILDER.comment("Whether to show Mission Position in MissionOverlay (default = false)");
        SHOW_MISSION_POSITION = BUILDER.define("Show_Mission_Position", false);
        BUILDER.comment("Whether to show Summon Count in MissionOverlay (default = true)");
        SHOW_MISSION_SUMMON = BUILDER.define("Show_Summon_Count", true);
        BUILDER.pop();

        BUILDER.push("EnergyBarHUD");
        BUILDER.comment("You can change this by an operation screen in game");
        BUILDER.comment("X position of energy bar (-1 = default)");
        ENERGY_BAR_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of energy bar (-1 = default)");
        ENERGY_BAR_Y = BUILDER.define("Y", -1);
        BUILDER.pop();

        BUILDER.push("BatteryHUD");
        BUILDER.comment("You can change this by an operation screen in game");
        BUILDER.comment("X position of battery (-1 = default)");
        BATTERY_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of battery (-1 = default)");
        BATTERY_Y = BUILDER.define("Y", -1);
        BUILDER.pop();

        BUILDER.push("ReservoirsHUD");
        BUILDER.comment("You can change this by an operation screen in game");
        BUILDER.comment("X position of reservoirs (-1 = default)");
        RESERVOIRS_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of reservoirs (-1 = default)");
        RESERVOIRS_Y = BUILDER.define("Y", -1);
        BUILDER.pop();

        BUILDER.push("Particle");

        BUILDER.push("Damage_Number");
        BUILDER.comment("""
                Damage_Number_Enlarge decides amplification factor of Damage Number Particle.
                The distance between the player and the target divided by this value equals the Damage Number Particle enlargement rate.
                The larger this value is, the smaller the Damage Number Particle enlargement rate will be. (default = 4.0)""");
        DAMAGE_NUMBER_ENLARGE = BUILDER.define("Damage_Number_Enlarge", 4.0);
        BUILDER.comment("Whether to show Level Content (default = true)");
        LV_CONTENT = BUILDER.define("Lv_Content", true);
        BUILDER.comment("Whether to enlarge Level Content by collision box (default = true)");
        LV_CONTENT_ENLARGE = BUILDER.define("Lv_Content_Enlarge", true);
        BUILDER.comment("Is the Level Content of Boss see through (default = true)");
        BOSS_LV_SEE_THROUGH = BUILDER.define("Boss_Lv_See_Through", true);
        BUILDER.comment("The color of Level Content for Boss (default = 0xFFAA00, Integer)");
        LV_BOSS_COLOR = BUILDER.define("Lv_Boss_Color", 0xFFAA00);
        BUILDER.pop();

        BUILDER.push("Electricity_Particle");
        BUILDER.comment("Whether to summon Electricity Particle (default = true)");
        ELECTRICITY_VALID = BUILDER.define("Electricity_Valid", true);
        BUILDER.comment("(default = 1, Integer, tick)");
        ELECTRICITY_REFRESH_RATE = BUILDER.define("Refresh_Rate", 1);
        BUILDER.comment("(default = 10, Integer, tick)");
        ELECTRICITY_LIFE_TIME = BUILDER.define("Life_Time", 10);
        BUILDER.comment("(default = 5, Integer)");
        ELECTRICITY_MAX_GENERATIONS = BUILDER.define("Max_Generations", 5);
        BUILDER.comment("(default = 3, Integer)");
        ELECTRICITY_BRANCH_GENERATION_LIMIT = BUILDER.define("Branch_Generation_Limit", 3);
        BUILDER.comment("(default = 0.1, Double)");
        ELECTRICITY_SIZE = BUILDER.define("Electricity_Size", 0.1);
        BUILDER.comment("(default = 1.2, Double)");
        ELECTRICITY_JITTER_STRENGTH = BUILDER.define("Jitter_Strength", 1.2);
        BUILDER.comment("(default = 0.3, Double)");
        ELECTRICITY_BRANCH_CHANCE = BUILDER.define("Branch_Chance", 0.3);
        BUILDER.pop();

        BUILDER.push("Heat_Particle");
        BUILDER.comment("Whether to summon Heat Particle (default = true)");
        HEAT_VALID = BUILDER.define("Heat_Valid", true);
        BUILDER.comment("(default = 0.25, Double)");
        HEAT_SIZE = BUILDER.define("Heat_Size", 0.25);
        BUILDER.comment("(default = 10, Integer, tick)");
        HEAT_LIFE_TIME = BUILDER.define("Life_Time", 50);
        BUILDER.pop();

        BUILDER.push("Blast_Particle");
        BUILDER.comment("Whether to summon Blast Particle (default = true)");
        BLAST_VALID = BUILDER.define("Blast_Valid", true);
        BUILDER.comment("The added distance of each level (default = 0.25, Double)");
        BLAST_EACH_DISTANCE = BUILDER.define("Blast_Each_Distance", 0.25);
        BUILDER.comment("The count of Blast for each level (default = 1, Integer)");
        BLAST_EACH_LEVEL = BUILDER.define("Blast_Each_Level", 1);
        BUILDER.comment("The max number of Blast (default = 8, Integer)");
        BLAST_MAX_COUNT = BUILDER.define("Blast_Max_Count", 8);
        BUILDER.pop();

        BUILDER.push("Cold_Particle");
        BUILDER.comment("Whether to summon Cold Particle (default = true)");
        COLD_VALID = BUILDER.define("Cold_Valid", true);
        BUILDER.comment("Summon chance of Cold Particle for each tick (default = 0.5, Double)");
        COLD_SUMMON_CHANCE = BUILDER.define("Summon_Chance", 0.5);
        BUILDER.pop();

        BUILDER.push("Toxin_Particle");
        BUILDER.comment("Whether to summon Toxin Particle (default = true)");
        TOXIN_VALID = BUILDER.define("Toxin_Valid", true);
        BUILDER.comment("(default = 0.15, Double)");
        TOXIN_SIZE = BUILDER.define("Toxin_Size", 0.15);
        BUILDER.comment("(default = 10, Integer, tick)");
        TOXIN_LIFE_TIME = BUILDER.define("Toxin_Time", 50);
        BUILDER.pop();

        BUILDER.push("Gas_Particle");
        BUILDER.comment("Whether to summon Gas Particle (default = true)");
        GAS_VALID = BUILDER.define("Gas_Valid", true);
        BUILDER.comment("(default = 0.25, Double)");
        GAS_MAX_SIZE = BUILDER.define("Gas_Max_Size", 0.25);
        BUILDER.comment("(default = 10, Integer, tick)");
        GAS_LIFE_TIME = BUILDER.define("Gas_Time", 50);
        BUILDER.pop();

        BUILDER.pop();

        BUILDER.push("Motion");
        BUILDER.comment("Whether to active bullet jump (default = true)");
        IF_BULLET_JUMP = BUILDER.define("If_Bullet_Jump", true);
        BUILDER.comment("Whether to active air brake (default = true)");
        IF_AIR_BRAKE = BUILDER.define("If_Air_Brake", true);
        BUILDER.pop();

        BUILDER.push("Sound");
        BUILDER.comment("Whether to play Thermal Sunder sound (default = true)");
        IF_THERMAL_SUNDER_SOUND = BUILDER.define("If_Thermal_Sunder_Sound", true);
        BUILDER.comment("The volume of Thermal Sunder sound (default = 1.0)");
        THERMAL_SUNDER_VOLUME = BUILDER.define("Volume", 3.0);
        BUILDER.comment("The pitch of Thermal Sunder sound (default = 1.0)");
        THERMAL_SUNDER_PITCH = BUILDER.define("Pitch", 1.0);
        BUILDER.comment("Whether to play headshot sound (default = true)");
        IF_HEADSHOT_SOUND = BUILDER.define("If_Headshot_Sound", true);
        BUILDER.comment("The volume of headshot sound (default = 0.6)");
        HEADSHOT_VOLUME = BUILDER.define("Volume", 0.6);
        BUILDER.comment("The pitch of headshot sound (default = 1.2)");
        HEADSHOT_PITCH = BUILDER.define("Pitch", 1.2);
        BUILDER.pop();

        BUILDER.push("Reversal");
        BUILDER.comment("Whether to reversal Thermal Sunder (default = false)");
        IF_REVERSAL_THERMAL_SUNDER = BUILDER.define("Thermal_Sunder", false);
        BUILDER.pop();


        SPEC = BUILDER.build();
    }
}
