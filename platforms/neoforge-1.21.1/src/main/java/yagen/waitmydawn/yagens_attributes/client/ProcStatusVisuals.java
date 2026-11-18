package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.StatusEffectPayload;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static yagen.waitmydawn.yagens_attributes.client.ElementStatusVisuals.billboard;
import static yagen.waitmydawn.yagens_attributes.client.ElementStatusVisuals.density;
import static yagen.waitmydawn.yagens_attributes.client.ElementStatusVisuals.scale;

/** Short physical hits and low-density overlays for the remaining status effects. */
final class ProcStatusVisuals {
    private record Key(UUID entity, DamageType type) {}
    private record Burst(Vec3 position, float width, double phase, long born, int lifetime) {
        float progress(double time) { return (float)Math.clamp((time - born) / lifetime, 0, 1); }
    }
    private static final LinkedHashMap<Key, Burst> BURSTS = new LinkedHashMap<>();
    private static final int BURST_LIMIT = 128;
    private static final double TURN = 2 * Math.PI;
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final RenderType PUNCTURE = ElementStatusVisuals.sheet("puncture_wedge");
    private static final RenderType RADIATION = ElementStatusVisuals.sheet("radiation_shard");
    private static final RenderType CORROSION = ElementStatusVisuals.sheet("corrosion_etch");
    private static final RenderType VIRAL = ElementStatusVisuals.sheet("viral_pair");

    private ProcStatusVisuals() {}

    static void accept(StatusEffectPayload payload, float width, long tick) {
        var key = new Key(payload.entityUuid(), payload.effect());
        var previous = BURSTS.get(key);
        if (previous != null && tick - previous.born() < previous.lifetime() / 2) return;
        if (previous == null && BURSTS.size() >= BURST_LIMIT) BURSTS.pollFirstEntry();
        double phase = Integer.toUnsignedLong(payload.seed()) * (TURN / 0x1.0p32);
        BURSTS.put(key, new Burst(payload.position(), Math.clamp(width, .35F, 2.5F), phase, tick, payload.lifetime()));
    }

    static void clear() { BURSTS.clear(); }

    static void tick(long tick) { BURSTS.values().removeIf(burst -> tick - burst.born() >= burst.lifetime()); }

    static void render(MultiBufferSource.BufferSource buffers, Matrix4f matrix, Vec3 camera, Vec3 right,
                       double time, float partial, List<ElementStatusVisuals.Active> hosts) {
        if (!BURSTS.isEmpty()) {
            drawBursts(buffers, ElementStatusVisuals.COLOR_QUADS, DamageType.IMPACT, matrix, camera, right, time);
            drawBursts(buffers, PUNCTURE, DamageType.PUNCTURE, matrix, camera, right, time);
        }
        if (hosts.isEmpty()) return;
        drawAmbient(buffers, RADIATION, DamageType.RADIATION, matrix, camera, right, time, partial, hosts);
        drawAmbient(buffers, CORROSION, DamageType.CORROSIVE, matrix, camera, right, time, partial, hosts);
        drawAmbient(buffers, VIRAL, DamageType.VIRAL, matrix, camera, right, time, partial, hosts);
    }

    private static void drawBursts(MultiBufferSource.BufferSource buffers, RenderType layer, DamageType type,
                                   Matrix4f matrix, Vec3 camera, Vec3 right, double time) {
        if (!ClientConfigs.STATUS.shows(type)) return;
        int count = density(type, type == DamageType.IMPACT ? 4 : 3);
        if (count == 0) return;
        float size = scale(type);
        var out = buffers.getBuffer(layer);
        BURSTS.forEach((key, burst) -> {
            if (key.type() != type || !ElementStatusVisuals.near(burst.position())) return;
            float progress = burst.progress(time);
            if (progress >= 1) return;
            if (type == DamageType.IMPACT) impact(out, matrix, burst, camera, count, size, progress);
            else puncture(out, matrix, burst, camera, right, count, size, progress);
        });
        buffers.endBatch(layer);
    }

    private static void impact(VertexConsumer out, Matrix4f matrix, Burst burst, Vec3 camera, int count, float size, float progress) {
        var center = burst.position().subtract(camera);
        double radius = burst.width() * size * (.63 + progress * 1.25);
        double width = burst.width() * size * (.13 - progress * .074);
        float alpha = .85F * (1 - progress);
        for (int i = 0; i < count; i++) {
            double start = burst.phase() + TURN * i / count;
            double span = TURN * .76 / count;
            for (int j = 0; j < 5; j++) {
                double from = start + span * j / 5;
                double to = start + span * (j + 1) / 5;
                impactVertex(out, matrix, ring(center, radius - width, from, burst.phase()), alpha * .36F);
                impactVertex(out, matrix, ring(center, radius, from, burst.phase()), alpha);
                impactVertex(out, matrix, ring(center, radius, to, burst.phase()), alpha);
                impactVertex(out, matrix, ring(center, radius - width, to, burst.phase()), alpha * .36F);
            }
            double a = start + span * .45;
            Vec3 grit = ring(center, radius * 1.16, a, burst.phase()).add(0, .05 + progress * .11, 0);
            double gritSize = width * .42;
            Vec3 offset = new Vec3(-Math.sin(a) * gritSize, 0, Math.cos(a) * gritSize);
            impactVertex(out, matrix, grit.subtract(offset), alpha);
            impactVertex(out, matrix, grit.add(0, gritSize, 0), alpha);
            impactVertex(out, matrix, grit.add(offset), alpha);
            impactVertex(out, matrix, grit.add(0, -gritSize, 0), alpha * .45F);
        }
    }

    private static Vec3 ring(Vec3 center, double radius, double angle, double phase) {
        return center.add(Math.cos(angle) * radius, Math.sin(angle - phase) * radius * .28, Math.sin(angle) * radius);
    }

    private static void impactVertex(VertexConsumer out, Matrix4f matrix, Vec3 point, float alpha) {
        out.addVertex(matrix, (float)point.x, (float)point.y, (float)point.z).setColor(.82F, .90F, .94F, alpha);
    }

    private static void puncture(VertexConsumer out, Matrix4f matrix, Burst burst, Vec3 camera, Vec3 right, int count,
                                 float size, float progress) {
        var center = burst.position().subtract(camera);
        center = center.add(towardsCamera(center).scale(burst.width() * .54));
        if (progress >= .68F) {
            float fade = 1 - (progress - .68F) / .32F;
            billboard(out, matrix, center, right, .18F * size, .18F * size, fade, 3);
            return;
        }
        double travel = 1 - progress / .68;
        double radius = burst.width() * size * (.16 + .87 * travel * travel);
        float wedge = size * Math.clamp(burst.width() * .34F, .13F, .32F);
        for (int i = 0; i < count; i++) {
            double a = burst.phase() + i * TURN / count;
            var outward = right.scale(Math.cos(a)).add(UP.scale(Math.sin(a)));
            var across = right.scale(-Math.sin(a)).add(UP.scale(Math.cos(a)));
            billboard(out, matrix, center.add(outward.scale(radius)), across.scale(-wedge * .57), outward.scale(-wedge), .93F, i % 3);
        }
    }

    private static void drawAmbient(MultiBufferSource.BufferSource buffers, RenderType layer, DamageType type, Matrix4f matrix,
                                    Vec3 camera, Vec3 right, double time, float partial, List<ElementStatusVisuals.Active> hosts) {
        if (!ClientConfigs.STATUS.shows(type)) return;
        float intensity = ClientConfigs.STATUS.effect(type).density().get() / 100F;
        if (intensity == 0) return;
        float size = scale(type);
        var out = buffers.getBuffer(layer);
        for (var entry : hosts) {
            int stacks = entry.host().layers(type);
            var entity = entry.entity();
            if (stacks == 0 || !ElementStatusVisuals.visible(entity) || firstPerson(entity)) continue;
            var position = entity.getPosition(partial).subtract(camera);
            double phase = time + entity.getId() * 3.17;
            switch (type) {
                case RADIATION -> radiation(out, matrix, position, right, entity, stacks, intensity, size, phase);
                case CORROSIVE -> corrosion(out, matrix, position, right, entity, stacks, intensity, size, phase);
                case VIRAL -> viral(out, matrix, position, right, entity, stacks, intensity, size, phase);
                default -> throw new IllegalArgumentException("Unexpected ambient effect " + type);
            }
        }
        buffers.endBatch(layer);
    }

    private static void radiation(VertexConsumer out, Matrix4f matrix, Vec3 position, Vec3 right, Entity entity,
                                  int stacks, float intensity, float size, double time) {
        int count = Math.max(1, Math.round(intensity * 3));
        float radius = Math.clamp(entity.getBbWidth() * .72F, .29F, 1.1F);
        float width = Math.clamp(entity.getBbWidth() * .36F, .15F, .34F) * size;
        var head = position.add(0, entity.getBbHeight() * .87, 0).add(towardsCamera(position).scale(radius * .73));
        for (int i = 0; i < count; i++) {
            double phase = time * .26 + TURN * i / count;
            double slip = Math.sin(phase) * radius * .23;
            double spread = (i % 2 == 0 ? -1 : 1) * radius * (.97 + .13 * Math.sin(phase * .6));
            var at = head.add(right.scale(spread + slip)).add(0, (.11 + radius * .26) * Math.cos(phase + i), 0);
            float alpha = Math.min(.88F, (.49F + .035F * stacks) * (.77F + .22F * (float)Math.sin(phase)));
            billboard(out, matrix, at, right, width, width * .69F, alpha, i % 4);
        }
    }

    private static void corrosion(VertexConsumer out, Matrix4f matrix, Vec3 position, Vec3 right, Entity entity,
                                  int stacks, float intensity, float size, double time) {
        int count = Math.clamp(Math.round((2 + Math.min(stacks, 10) * .3F) * intensity), 1, 10);
        float radius = Math.clamp(entity.getBbWidth() * .54F, .18F, .9F);
        float width = Math.clamp(entity.getBbWidth() * .28F, .12F, .28F) * size;
        var front = position.add(towardsCamera(position).scale(radius));
        for (int i = 0; i < count; i++) {
            double side = (i % 2 == 0 ? -1 : 1) * radius * (.61 + (i % 3) * .16);
            double height = entity.getBbHeight() * (.47 + (i % 5) * .077);
            var at = front.add(right.scale(side)).add(0, height, 0);
            float alpha = Math.min(.84F, .39F + stacks * .045F);
            billboard(out, matrix, at, right, width, width * .91F, alpha, i % 3);
            double fall = ((time + i * 4.31) % 15) / 15;
            var chip = at.add(right.scale(Math.signum(side) * fall * .11))
                    .add(0, -fall * (.20 + fall * .42) * entity.getBbHeight(), 0);
            billboard(out, matrix, chip, right, width * .48F, width * .48F, (float)(alpha * (1 - fall)), 3);
        }
    }

    private static void viral(VertexConsumer out, Matrix4f matrix, Vec3 position, Vec3 right, Entity entity,
                              int stacks, float intensity, float size, double time) {
        float radius = Math.clamp(entity.getBbWidth() * .50F, .18F, .8F);
        var chest = position.add(0, entity.getBbHeight() * .61, 0).add(towardsCamera(position).scale(radius * 1.33));
        float width = Math.clamp(entity.getBbWidth() * .43F, .18F, .44F) * size;
        double side = entity.getId() % 2 == 0 ? 1 : -1;
        for (int i = 0; i < 2; i++) {
            double phase = time * .218 + i * .86;
            float pulse = (float)Math.pow(.5 + .5 * Math.sin(phase), 2);
            float alpha = Math.min(.87F, (.21F + .65F * pulse) * intensity * (.77F + .045F * stacks));
            float magnitude = width * (i == 0 ? 1 : .70F) * (.80F + .24F * pulse);
            var at = chest.add(right.scale(side * radius * (i == 0 ? .95 : -.54)))
                    .add(0, (i == 0 ? 0 : -.22) * entity.getBbHeight() * .4 + pulse * .04, 0);
            billboard(out, matrix, at, right, magnitude, magnitude, alpha, i);
        }
    }

    private static Vec3 towardsCamera(Vec3 cameraRelative) {
        return new Vec3(-cameraRelative.x, 0, -cameraRelative.z).normalize();
    }

    private static boolean firstPerson(Entity entity) {
        var client = Minecraft.getInstance();
        return entity == client.player && client.options.getCameraType().isFirstPerson();
    }
}
