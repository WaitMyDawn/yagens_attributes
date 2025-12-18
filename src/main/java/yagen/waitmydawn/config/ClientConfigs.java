package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_Y;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_Y;

    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_MISSION_POSITION;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_MISSION_SUMMON;

    public static final ModConfigSpec.ConfigValue<Boolean> LV_CONTENT;
    public static final ModConfigSpec.ConfigValue<Boolean> LV_CONTENT_ENLARGE;
    public static final ModConfigSpec.ConfigValue<Boolean> BOSS_LV_SEE_THROUGH;
    public static final ModConfigSpec.ConfigValue<Integer> LV_BOSS_COLOR;

    public static final ModConfigSpec.ConfigValue<Double> DAMAGE_NUMBER_ENLARGE;

    /**
     * Damage Type Particle
     */
    public static final ModConfigSpec.ConfigValue<Boolean> ELECTRICITY_VALID;
    public static final ModConfigSpec.ConfigValue<Double> BRANCH_CHANCE;
    public static final ModConfigSpec.ConfigValue<Double> JITTER_STRENGTH;
    public static final ModConfigSpec.ConfigValue<Integer> MAX_GENERATIONS;
    public static final ModConfigSpec.ConfigValue<Integer> BRANCH_GENERATION_LIMIT;
    public static final ModConfigSpec.ConfigValue<Integer> REFRESH_RATE;
    public static final ModConfigSpec.ConfigValue<Integer> LIFE_TIME;

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
        REFRESH_RATE= BUILDER.define("Refresh_Rate", 1);
        BUILDER.comment("(default = 10, Integer, tick)");
        LIFE_TIME= BUILDER.define("Life_Time", 10);
        BUILDER.comment("(default = 5, Integer)");
        MAX_GENERATIONS= BUILDER.define("Max_Generations", 5);
        BUILDER.comment("(default = 3, Integer)");
        BRANCH_GENERATION_LIMIT= BUILDER.define("Branch_Generation_Limit", 3);
        BUILDER.comment("(default = 1.2, Double)");
        JITTER_STRENGTH = BUILDER.define("Jitter_Strength", 1.2);
        BUILDER.comment("(default = 0.3, Double)");
        BRANCH_CHANCE = BUILDER.define("Branch_Chance", 0.3);
        BUILDER.pop();

        BUILDER.pop();


        SPEC = BUILDER.build();
    }
}
