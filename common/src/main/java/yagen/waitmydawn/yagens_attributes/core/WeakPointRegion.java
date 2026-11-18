package yagen.waitmydawn.yagens_attributes.core;

import java.util.Objects;
import java.util.regex.Pattern;

/** Horizontal coordinates are relative to collider width and vertical coordinates to height. */
public record WeakPointRegion(String id, Anchor anchor, Rotation rotation, Point min, Point max, State state, Part part) {
    public static final int MAX_PER_ENTITY = 64;
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]{1,64}");

    public enum Anchor { FEET, EYES }
    public enum Rotation { NONE, BODY, HEAD }
    public enum Part { BODY, DRAGON_HEAD }
    public enum State {
        ALWAYS, GOAT_HEAD_UP, GOAT_HEAD_DOWN, PUFFERFISH_SMALL, PUFFERFISH_MEDIUM, PUFFERFISH_FULL,
        SHULKER_EXPOSED, ARMADILLO_EXPOSED, BAT_FLYING, BAT_HANGING, SQUID_MODEL
    }

    public WeakPointRegion(String id, Anchor anchor, Rotation rotation, Point min, Point max) {
        this(id, anchor, rotation, min, max, State.ALWAYS, Part.BODY);
    }

    public record Point(double x, double y, double z) {
        public Point {
            if (!valid(x) || !valid(y) || !valid(z)) throw new IllegalArgumentException("Weak point coordinate outside [-4, 4]");
        }

        private static boolean valid(double value) {
            return Double.isFinite(value) && Math.abs(value) <= 4;
        }
    }

    public WeakPointRegion {
        validateId(id);
        Objects.requireNonNull(anchor);
        Objects.requireNonNull(rotation);
        Objects.requireNonNull(state);
        Objects.requireNonNull(part);
        if (min.x() >= max.x() || min.y() >= max.y() || min.z() >= max.z())
            throw new IllegalArgumentException("Weak point requires positive volume");
    }

    public static void validateId(String id) {
        if (!ID.matcher(id).matches()) throw new IllegalArgumentException("Invalid weak point ID " + id);
    }
}
