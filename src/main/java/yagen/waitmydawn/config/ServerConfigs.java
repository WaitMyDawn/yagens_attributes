package yagen.waitmydawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_OFFHAND;
    public static final ModConfigSpec.ConfigValue<Boolean> BAN_MEAT_SHREDDER_COMBO;
    public static final ModConfigSpec.ConfigValue<Integer> SPLASH_POTION_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> MAX_COMBO_LEVEL;
    public static final ModConfigSpec.ConfigValue<Integer> COMBO_LEVEL_NEEDED_COUNT;

//    public static final ModConfigSpec.ConfigValue<Integer> COMMON_WEIGHT;
//    public static final ModConfigSpec.ConfigValue<Integer> UNCOMMON_WEIGHT;
//    public static final ModConfigSpec.ConfigValue<Integer> RARE_WEIGHT;
//    public static final ModConfigSpec.ConfigValue<Integer> LEGENDARY_WEIGHT;
//
//    public static final ModConfigSpec.ConfigValue<Integer> MOD_LEVEL_PERCENT_INF;
//    public static final ModConfigSpec.ConfigValue<Integer> MOD_LEVEL_PERCENT_SUP;
//
//    public static final ModConfigSpec.ConfigValue<Boolean> GRASS_LOOT;
//    public static final ModConfigSpec.ConfigValue<Float> GRASS_LOOT_CHANCE;
//    public static final ModConfigSpec.ConfigValue<Boolean> SNIFFER_EGG;
//
//    public static final ModConfigSpec.ConfigValue<Float> AMPLIFICATION_MOD_ESSENCE_ENTITY;
//    public static final ModConfigSpec.ConfigValue<Float> AMPLIFICATION_FORMA_ENTITY;


    static {
//        BUILDER.push("Weight of Mod Rarity (Integer)");
//        BUILDER.comment("There are basic weights of Mod Rarity. It will be modified due to different Loot Tables");
//        BUILDER.comment("Weights of Common Rarity (default = 50)");
//        COMMON_WEIGHT = BUILDER.define("Common Weight", 50);
//        BUILDER.comment("Weights of Uncommon Rarity (default = 40)");
//        UNCOMMON_WEIGHT = BUILDER.define("Uncommon Weight", 40);
//        BUILDER.comment("Weights of Rare Rarity (default = 10)");
//        RARE_WEIGHT = BUILDER.define("Rare Weight", 10);
//        BUILDER.comment("Weights of Legendary Rarity (default = 0)");
//        BUILDER.comment("""
//                This doesn't show the true weight of Legendary Rarity,
//                because in Loot Tables contain Legendary Rarity, other weights are always enlarged.""");
//        LEGENDARY_WEIGHT = BUILDER.define("Legendary Weight", 0);
//        BUILDER.pop();
//
//        BUILDER.push("Range of Mod Level (Integer)");
//        BUILDER.comment("""
//                There is basic range of Mod Level. It will be modified due to different Loot Tables.
//                The range will be fixed depend on mathematical norms.""");
//        BUILDER.comment("Inf Percent of Mod level (default = 0)");
//        MOD_LEVEL_PERCENT_INF = BUILDER.define("Mod Level Percent Inf", 0);
//        BUILDER.comment("Sup Percent of Mod level (default = 50)");
//        MOD_LEVEL_PERCENT_SUP = BUILDER.define("Mod Level Percent Sup", 50);
//        BUILDER.pop();
//
//        BUILDER.push("Optimize for Vanilla");
//        BUILDER.comment("Can you get carrot and potato from grass? (default = true)");
//        GRASS_LOOT = BUILDER.define("Grass Loot (Boolean)", true);
//        BUILDER.comment("The chance you get carrot and potato from grass (default = 0.1)");
//        GRASS_LOOT_CHANCE = BUILDER.define("Grass Loot Chance (Float)", 0.1f);
//        BUILDER.comment("Can you get sniffer egg when sniffer dead? (default = true)");
//        SNIFFER_EGG = BUILDER.define("Grass Loot (Boolean)", true);
//        BUILDER.pop();
//
//        BUILDER.push("Loot Chance From Entity");
//        BUILDER.comment("""
//                Amplification of Mod Essence Loot Chance From Entity
//                (default = 1.0, default chance range = [0.01 (creeper),0.8 (warden)])""");
//        AMPLIFICATION_MOD_ESSENCE_ENTITY = BUILDER.define("Amplification (Float)", 1.0f);
//        BUILDER.comment("""
//                Amplification of Forma Loot Chance From Entity
//                (default = 1.0, default chance = 0.1)""");
//        AMPLIFICATION_FORMA_ENTITY = BUILDER.define("Amplification (Float)", 1.0f);
//        BUILDER.pop();

        BUILDER.push("Optimize for Vanilla");
        BUILDER.comment("The cooldown you use splash potion or lingering potion (default = 20)");
        SPLASH_POTION_COOLDOWN = BUILDER.defineInRange("Potion Cooldown (Integer, tick)", 20, 0, 100);
        BUILDER.pop();

        BUILDER.push("Ban");
        BUILDER.comment("Ban use Meat Shredder(cataclysm) in your offhand? (default = true)");
        BAN_MEAT_SHREDDER_OFFHAND = BUILDER.define("Ban Meat Shredder Offhand", true);
        BUILDER.comment("Ban use Meat Shredder(cataclysm) to get combo count? (default = true)");
        BAN_MEAT_SHREDDER_COMBO = BUILDER.define("Ban Meat Shredder Combo", true);
        BUILDER.pop();

        BUILDER.push("Combo");
        BUILDER.comment("Max Combo Level bonuses your attributes by your Mods, this value is better not to be too large.");
        BUILDER.comment("Max Combo Level (default = 14)");
        MAX_COMBO_LEVEL = BUILDER.define("Max Combo Level (Integer)", 14);
        BUILDER.comment("The number of combo required for each level.");
        BUILDER.comment("Combo Level Needed count (default = 10)");
        COMBO_LEVEL_NEEDED_COUNT = BUILDER.define("Combo Level Needed count (Integer)", 10);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
