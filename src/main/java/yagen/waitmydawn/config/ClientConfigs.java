package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_HUD_Y;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_X;
    public static final ModConfigSpec.ConfigValue<Integer> MISSION_HUD_Y;

    public static final ModConfigSpec.ConfigValue<Float> DAMAGE_NUMBER_ENLARGE;

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
        BUILDER.comment("X position of the mission counter (-1 = center)");
        MISSION_HUD_X = BUILDER.define("X", -1);
        BUILDER.comment("Y position of the mission counter (-1 = center)");
        MISSION_HUD_Y = BUILDER.define("Y", -1);
        BUILDER.pop();

        BUILDER.push("Particle");
        BUILDER.comment("""
                Damage_Number_Enlarge decides amplification factor of Damage Number Particle.
                The distance between the player and the target divided by this value equals the Damage Number Particle enlargement rate.
                The larger this value is, the smaller the Damage Number Particle enlargement rate will be. (default = 4.0)""");
        DAMAGE_NUMBER_ENLARGE = BUILDER.define("Damage Number Enlarge", 4.0f);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
