package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Seeded geometry in a chord-relative coordinate system, independent of camera and frame rate. */
public final class ArcShape {
    public static final int MAX_SEGMENTS = 96;
    public record Segment(Vec3 from, Vec3 to, int depth) {
        public double widthScale() { return Math.scalb(1D, -depth); }
    }
    public record Anchors(double fromHeight, double toHeight) {}
    private ArcShape() {}

    public static List<Segment> build(int seed, double length, int maxDepth, double probability, double branchLength) {
        if (!Double.isFinite(length) || length <= 0 || maxDepth < 0 || maxDepth > 4
                || !Double.isFinite(probability) || probability < 0 || probability > 1
                || !Double.isFinite(branchLength) || branchLength <= 0 || branchLength > .4)
            throw new IllegalArgumentException("Invalid arc geometry parameters");
        var result = new ArrayList<Segment>();
        path(result, new Random(seed), Vec3.ZERO, new Vec3(1, 0, 0), 0, maxDepth, probability, branchLength, Math.min(.11, .22 / length));
        return List.copyOf(result);
    }

    private static void path(List<Segment> result, Random random, Vec3 from, Vec3 to, int depth,
                             int maxDepth, double probability, double branchLength, double jitter) {
        int pieces = depth == 0 ? 8 : 4;
        if (result.size() + pieces > MAX_SEGMENTS) return;
        double chordLength = from.distanceTo(to);
        var direction = to.subtract(from).scale(1 / chordLength);
        var side = direction.cross(Math.abs(direction.y) < .9 ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1)).normalize();
        var up = side.cross(direction).normalize();
        var points = new ArrayList<Vec3>(); points.add(from);
        for (int i = 1; i <= pieces; i++) {
            double t = i / (double) pieces;
            var next = i == pieces ? to : from.lerp(to, t)
                    .add(side.scale((random.nextDouble() - .5) * jitter))
                    .add(up.scale((random.nextDouble() - .5) * jitter));
            result.add(new Segment(points.get(i - 1), next, depth)); points.add(next);
        }
        if (depth >= maxDepth || probability == 0) return;
        int firstFork = depth == 0 ? pieces / 2 + 1 : 1;
        for (int i = firstFork; i < pieces && result.size() + 4 <= MAX_SEGMENTS; i++) {
            if (random.nextDouble() >= probability) continue;
            double sign = random.nextBoolean() ? 1 : -1;
            var branch = direction.scale(.25 + random.nextDouble() * .25)
                    .add(side.scale(sign * (.6 + random.nextDouble() * .3)))
                    .add(up.scale((random.nextDouble() - .5) * .8)).normalize();
            double span = chordLength * branchLength * (.65 + random.nextDouble() * .35);
            var start = points.get(i);
            path(result, random, start, start.add(branch.scale(span)), depth + 1, maxDepth, probability,
                    branchLength, Math.min(jitter * .5, span * .25));
        }
    }

    public static Anchors anchors(int seed) {
        var random = new Random(Integer.toUnsignedLong(seed) ^ 0x9E3779B97F4A7C15L);
        return new Anchors(.32 + random.nextDouble() * .36, .32 + random.nextDouble() * .36);
    }
}
