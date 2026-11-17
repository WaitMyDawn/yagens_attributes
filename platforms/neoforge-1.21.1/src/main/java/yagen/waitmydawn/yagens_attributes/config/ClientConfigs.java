package yagen.waitmydawn.yagens_attributes.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import yagen.waitmydawn.yagens_attributes.core.ui.HudLayout;

public final class ClientConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DAMAGE_NUMBERS = BUILDER.define("damageNumbers", true);
    public static final ModConfigSpec.BooleanValue COMBO_HUD = BUILDER.define("comboHud", true);
    public static final ModConfigSpec.BooleanValue BUFF_HUD = BUILDER.define("weaponBuffHud", true);
    public static final ModConfigSpec.BooleanValue BUFF_HUD_SECONDS = BUILDER.define("weaponBuffHudCountdown", true);
    public static final ModConfigSpec.IntValue BUFF_HUD_X = BUILDER.defineInRange("weaponBuffHudOffsetX", 16, 0, HudLayout.MAX_OFFSET);
    public static final ModConfigSpec.IntValue BUFF_HUD_Y = BUILDER.defineInRange("weaponBuffHudOffsetY", 96, 0, HudLayout.MAX_OFFSET);
    public static final ModConfigSpec.DoubleValue BUFF_HUD_SCALE = BUILDER.defineInRange("weaponBuffHudScale", 1D, HudLayout.MIN_SCALE, HudLayout.MAX_SCALE);
    public static final ModConfigSpec.IntValue BUFF_HUD_COLUMNS = BUILDER.defineInRange("weaponBuffHudColumns", 4, 1, 8);
    public static final ModConfigSpec.IntValue COMBO_HUD_X = BUILDER.comment("Combo HUD distance from the right edge in GUI pixels.").defineInRange("comboHudOffsetX", 16, 0, HudLayout.MAX_OFFSET);
    public static final ModConfigSpec.IntValue COMBO_HUD_Y = BUILDER.comment("Combo HUD distance from the bottom edge in GUI pixels.").defineInRange("comboHudOffsetY", 48, 0, HudLayout.MAX_OFFSET);
    public static final ModConfigSpec.DoubleValue COMBO_HUD_SCALE = BUILDER.defineInRange("comboHudScale", 1D, HudLayout.MIN_SCALE, HudLayout.MAX_SCALE);
    public static final ModConfigSpec.BooleanValue SHIELD_HEARTS = BUILDER.define("shieldHearts", true);
    public static final ModConfigSpec.BooleanValue RESTORE_NORMAL_SHIELD_COLOR = BUILDER
            .comment("Use vanilla absorption hearts for normal shields instead of blue hearts.")
            .define("restoreNormalShieldColor", false);
    public static final ModConfigSpec.BooleanValue RESTORE_OVERSHIELD_COLOR = BUILDER
            .comment("Use vanilla absorption hearts for overshields instead of purple hearts.")
            .define("restoreOvershieldColor", false);
    public static final ModConfigSpec.BooleanValue SHOW_SHIELD_GATE_LOCK = BUILDER
            .comment("Display the health-heart lock while shield gate protection is active, independently of shield colors.")
            .define("showShieldGateLock", true);
    public static final ModConfigSpec.BooleanValue STATUS_VISUALS = BUILDER.define("statusVisuals", true);
    public static final ModConfigSpec.DoubleValue ELECTRIC_ARC_WIDTH = BUILDER.comment("Full width of the primary electric arc core, in blocks. Each branch depth halves this width.")
            .defineInRange("electricArcWidth", .04D, .005D, .2D);
    public static final ModConfigSpec.IntValue ELECTRIC_ARC_BRANCH_DEPTH = BUILDER.comment("Maximum recursive electric branch depth; zero disables branches.")
            .defineInRange("electricArcMaxBranchDepth", 2, 0, 4);
    public static final ModConfigSpec.DoubleValue ELECTRIC_ARC_BRANCH_CHANCE = BUILDER.comment("Branch probability at each internal arc node, from zero to one.")
            .defineInRange("electricArcBranchProbability", .35D, 0D, 1D);
    public static final ModConfigSpec.BooleanValue STATUS_MARKERS = BUILDER.define("statusMarkers", true);
    public static final ModConfigSpec.IntValue STATUS_MARKERS_PER_ROW = BUILDER.defineInRange("statusMarkersPerRow", 3, 1, 13);
    public static final ModConfigSpec.DoubleValue STATUS_MARKER_HEIGHT_GAP = BUILDER.comment("Gap between entity top and marker bottom, as a fraction of entity height.")
            .defineInRange("statusMarkerHeightGap", .2, 0, 4);
    public static final ModConfigSpec.IntValue STATUS_MARKER_RANGE = BUILDER.defineInRange("statusMarkerRange", 32, 4, 128);
    public static final ModConfigSpec.BooleanValue SEE_OTHERS = BUILDER.define("seeOthers", true);
    public static final ModConfigSpec.IntValue MAX_NUMBERS = BUILDER.defineInRange("maxNumbers", 128, 1, 512);
    public static final ModConfigSpec.IntValue LIFETIME = BUILDER.defineInRange("lifetimeTicks", 30, 5, 100);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfigs() {
    }
}
