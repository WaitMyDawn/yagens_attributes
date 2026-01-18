package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_OFFHAND;
    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_COMBO;
    public static final ModConfigSpec.ConfigValue<Boolean> IF_LEVEL_BONUS;
    public static final ModConfigSpec.ConfigValue<Integer> SPLASH_POTION_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> MAX_COMBO_LEVEL;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_LEVEL_NEEDED_COUNT;

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

        SPEC = BUILDER.build();
    }
}
