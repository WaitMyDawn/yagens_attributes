package yagen.waitmydawn.yagens_attributes.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import yagen.waitmydawn.yagens_attributes.core.DamageType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

/** The runtime and settings screen share defaults, bounds and config bindings. */
public final class StatusVisualOptions {
    public record Option<T>(String label, ModConfigSpec.ConfigValue<T> config, Function<String, T> parse, String range) {
        public T get() { return config.get(); }
        public T defaultValue() { return config.getDefault(); }
    }
    public record Effect(Option<Boolean> visible, Option<Boolean> marker, List<Option<?>> options,
                         Option<Integer> density, Option<Double> scale) {}

    public final Option<Boolean> visible, markers, groundMarks, magneticArcs, blastParticles, gasOutline;
    public final Option<Integer> distance, markerColumns, markerDistance, bloodDrops, bloodBurst;
    public final Option<Double> markerGap, bloodSize, arcWidth, branchChance, branchLength, magneticOpacity, blastVolume;
    public final Option<Double> frostOpacity, gasOpacity;
    public final Option<Integer> branchDepth;
    public final List<Option<?>> general, markerGeneral;
    private final EnumMap<DamageType, Effect> effects = new EnumMap<>(DamageType.class);

    StatusVisualOptions(ModConfigSpec.Builder builder) {
        visible = toggle(builder, "statusVisuals", "effects_master", true);
        distance = integer(builder, "statusVisualRange", "range", 48, 4, 128);
        markers = toggle(builder, "statusMarkers", "markers_master", true);
        markerColumns = integer(builder, "statusMarkersPerRow", "columns", 3, 1, 13);
        markerGap = decimal(builder, "statusMarkerHeightGap", "gap", .2, 0, 4);
        markerDistance = integer(builder, "statusMarkerRange", "marker_range", 32, 4, 128);
        general = List.of(visible, distance, markers, markerColumns, markerGap, markerDistance);
        markerGeneral = List.of(markers, markerColumns, markerGap, markerDistance);

        bloodDrops = integer(builder, "slashPulseDrops", "blood_drops", 5, 0, 32);
        bloodBurst = integer(builder, "slashBurstDrops", "blood_burst", 10, 0, 32);
        bloodSize = decimal(builder, "slashDropSize", "blood_size", 1, .25, 3);
        groundMarks = toggle(builder, "slashGroundMarks", "ground_marks", true);
        arcWidth = decimal(builder, "electricArcWidth", "arc_width", .04, .005, .2);
        branchDepth = integer(builder, "electricArcMaxBranchDepth", "branch_depth", 2, 0, 4);
        branchChance = decimal(builder, "electricArcBranchProbability", "branch_chance", .35, 0, 1);
        branchLength = decimal(builder, "electricArcBranchLength", "branch_length", .22, .05, .4);
        magneticOpacity = decimal(builder, "magneticShellOpacity", "magnetic_opacity", .56, 0, 1);
        magneticArcs = toggle(builder, "magneticArcs", "magnetic_arcs", true);
        blastParticles = toggle(builder, "blastParticles", "blast_particles", true);
        blastVolume = decimal(builder, "blastVolume", "blast_volume", 1, 0, 2);
        frostOpacity = decimal(builder, "frostShellOpacity", "frost_opacity", .48, 0, 1);
        gasOpacity = decimal(builder, "gasFogOpacity", "gas_opacity", .5, 0, 1);
        gasOutline = toggle(builder, "gasFogOutline", "gas_outline", false);

        builder.push("statusTypes");
        for (DamageType type : DamageType.values()) {
            builder.push(type.key());
            var showsEffect = toggle(builder, "effect", "effects", true);
            var showsMarker = toggle(builder, "marker", "markers", true);
            var options = new ArrayList<Option<?>>();
            options.add(showsEffect);
            options.add(showsMarker);
            Option<Integer> count = null;
            Option<Double> size = null;
            switch (type) {
                case SLASH -> options.addAll(List.of(bloodDrops, bloodBurst, bloodSize, groundMarks));
                case ELECTRICITY -> options.addAll(List.of(arcWidth, branchDepth, branchChance, branchLength));
                case MAGNETIC -> options.addAll(List.of(magneticOpacity, magneticArcs));
                case BLAST -> options.addAll(List.of(blastParticles, blastVolume));
                default -> {
                    count = integer(builder, "densityPercent", "density", 100, 0, 200);
                    size = decimal(builder, "size", "dust_size", 1, .25, 3);
                    options.addAll(List.of(count, size));
                    if (type == DamageType.COLD) options.add(frostOpacity);
                    if (type == DamageType.GAS) options.addAll(List.of(gasOpacity, gasOutline));
                }
            }
            effects.put(type, new Effect(showsEffect, showsMarker, List.copyOf(options), count, size));
            builder.pop();
        }
        builder.pop();
    }

    public Effect effect(DamageType type) { return effects.get(type); }
    public boolean shows(DamageType type) { return visible.get() && effect(type).visible().get(); }

    public boolean isMarkerOption(Option<?> option) {
        return markerGeneral.contains(option) || effects.values().stream().anyMatch(effect -> effect.marker() == option);
    }

    public int markerMask() {
        int mask = 0;
        for (var entry : effects.entrySet())
            if (entry.getValue().marker().get()) mask |= 1 << entry.getKey().ordinal();
        return mask;
    }

    private static Option<Boolean> toggle(ModConfigSpec.Builder builder, String key, String label, boolean value) {
        return new Option<>(label, builder.define(key, value), Boolean::valueOf, "");
    }

    private static Option<Integer> integer(ModConfigSpec.Builder builder, String key, String label, int value, int min, int max) {
        return new Option<>(label, builder.defineInRange(key, value, min, max), text -> {
            int number = Integer.parseInt(text);
            if (number < min || number > max) throw new IllegalArgumentException();
            return number;
        }, min + "–" + max);
    }

    private static Option<Double> decimal(ModConfigSpec.Builder builder, String key, String label, double value, double min, double max) {
        return new Option<>(label, builder.defineInRange(key, value, min, max), text -> {
            double number = Double.parseDouble(text);
            if (!Double.isFinite(number) || number < min || number > max) throw new IllegalArgumentException();
            return number;
        }, min + "–" + max);
    }
}
