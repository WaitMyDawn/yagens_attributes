package yagen.waitmydawn.yagens_attributes.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import yagen.waitmydawn.yagens_attributes.core.ArmorRules;

public final class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_COMBAT = BUILDER.define("enableCombat", true);
    public static final ModConfigSpec.IntValue DAMAGE_VISIBILITY_RANGE = BUILDER.defineInRange("damageVisibilityRange", 64, 8, 128);
    public static final ModConfigSpec.IntValue SHIELD_RECHARGE_DELAY = BUILDER.defineInRange("shieldRechargeDelayTicks", 60, 1, 72000);
    public static final ModConfigSpec.BooleanValue COMBO_ENABLED = BUILDER.define("comboEnabled", true);
    public static final ModConfigSpec.DoubleValue COMBO_DURATION = BUILDER.defineInRange("comboDurationSeconds", 5D, 0, 7200);
    public static final ModConfigSpec.IntValue COMBO_HITS_PER_TIER = BUILDER.defineInRange("comboHitsPerTier", 20, 1, 10000);
    public static final ModConfigSpec.IntValue COMBO_MAX_TIER = BUILDER.defineInRange("comboMaxTier", 12, 2, 100);
    public static final ModConfigSpec.ConfigValue<String> ARMOR_MODE = BUILDER.comment("none: delegate unchanged; improved_vanilla: legacy damage-dependent curve; linear: independent armor/toughness factors.")
            .define("armorMode", "linear", value -> value instanceof String key && java.util.Set.of("none", "improved_vanilla", "linear").contains(key));
    public static final ModConfigSpec.DoubleValue ARMOR_SCALE = BUILDER.defineInRange("linearArmorScale", ArmorRules.DEFAULT_SCALE, ArmorRules.MIN_SCALE, ArmorRules.MAX_SCALE);
    public static final ModConfigSpec.DoubleValue TOUGHNESS_SCALE = BUILDER.defineInRange("linearToughnessScale", ArmorRules.DEFAULT_TOUGHNESS_SCALE, ArmorRules.MIN_SCALE, ArmorRules.MAX_SCALE);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfigs() {
    }
}
