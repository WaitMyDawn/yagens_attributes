package yagen.waitmydawn.yagens_attributes.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.StatusEffectPayload;
import yagen.waitmydawn.yagens_attributes.client.HudElement;
import yagen.waitmydawn.yagens_attributes.client.ArcShape;
import java.util.*;

public final class StatusEffectContracts {
    private static int checks;
    private StatusEffectContracts() {}
    public static void run() {
        var owner = UUID.randomUUID(); var victim = new StatusEffectPayload.Target(17, UUID.randomUUID());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            for (var type : List.of(DamageType.SLASH, DamageType.ELECTRICITY, DamageType.MAGNETIC, DamageType.BLAST)) {
                var packet = new StatusEffectPayload(1, owner, new Vec3(-2, 71.5, 4), type, type == DamageType.SLASH ? 60 : 10, 42, true,
                        type == DamageType.ELECTRICITY ? List.of(victim) : List.of());
                StatusEffectPayload.CODEC.encode(buffer, packet); equal(packet, StatusEffectPayload.CODEC.decode(buffer));
            }
        } finally { buffer.release(); }
        rejects(() -> new StatusEffectPayload(1, owner, Vec3.ZERO, DamageType.SLASH, 0, 0, false, List.of()));
        rejects(() -> new StatusEffectPayload(1, owner, Vec3.ZERO, DamageType.HEAT, 60, 0, false, List.of()));
        rejects(() -> new StatusEffectPayload(1, owner, new Vec3(Double.NaN, 0, 0), DamageType.BLAST, 10, 0, true, List.of()));
        rejects(() -> new StatusEffectPayload(1, owner, Vec3.ZERO, DamageType.ELECTRICITY, 10, 0, true, List.of(victim, victim)));
        rejects(() -> new StatusEffectPayload(1, owner, Vec3.ZERO, DamageType.SLASH, 60, 0, false, List.of(victim)));
        try (var stream = StatusEffectContracts.class.getResourceAsStream("/net/minecraft/world/level/ClipContext.class")) {
            var node = new org.objectweb.asm.tree.ClassNode(); new org.objectweb.asm.ClassReader(Objects.requireNonNull(stream)).accept(node, 0);
            equal(true, node.methods.stream().anyMatch(method -> method.name.equals("<init>") && method.desc.equals("(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;Lnet/minecraft/world/phys/shapes/CollisionContext;)V")));
        } catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        equal(true, HudElement.BUFFS.defaults().countdown());
        equal(false, new HudElement.Settings(16, 96, 1, true, false).countdown());
        arcGeometry();
        System.out.println("Passed " + checks + " status effect wire, bounded arc geometry, collision-context and HUD countdown checks");
    }
    private static void arcGeometry() {
        var anchorsBySeed = new HashSet<ArcShape.Anchors>();
        for (double length : List.of(.05, .8, 15D, 40D)) {
            for (int seed : List.of(0, -37, 1903)) {
                var plain = ArcShape.build(seed, length, 4, 0);
                equal(8, plain.size()); equal(true, plain.stream().allMatch(segment -> segment.depth() == 0));
                equal(plain, ArcShape.build(seed, length, 0, 1));
                var branches = ArcShape.build(seed, length, 4, 1);
                equal(branches, ArcShape.build(seed, length, 4, 1));
                equal(true, branches.size() <= ArcShape.MAX_SEGMENTS && branches.size() > plain.size());
                equal(true, branches.stream().anyMatch(segment -> segment.depth() == 4));
                for (var segment : branches) {
                    equal(true, segment.depth() <= 4 && segment.depth() >= 0);
                    equal(1D / (1 << segment.depth()), segment.widthScale());
                    equal(true, Double.isFinite(segment.from().lengthSqr()) && Double.isFinite(segment.to().lengthSqr()));
                }
                equal(Vec3.ZERO, branches.get(0).from()); equal(new Vec3(1, 0, 0), branches.get(7).to());
                var anchors = ArcShape.anchors(seed); anchorsBySeed.add(anchors);
                equal(anchors, ArcShape.anchors(seed));
                equal(true, anchors.fromHeight() >= .32 && anchors.fromHeight() < .68);
                equal(true, anchors.toHeight() >= .32 && anchors.toHeight() < .68);
                equal(false, anchors.fromHeight() == anchors.toHeight());
            }
        }
        equal(3, anchorsBySeed.size());
        rejects(() -> ArcShape.build(1, Double.NaN, 2, .35));
        rejects(() -> ArcShape.build(1, 0, 2, .35));
        rejects(() -> ArcShape.build(1, 1, 5, .35));
        rejects(() -> ArcShape.build(1, 1, 2, 1.1));
    }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
    private static void rejects(Runnable action) { try { action.run(); } catch (RuntimeException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
