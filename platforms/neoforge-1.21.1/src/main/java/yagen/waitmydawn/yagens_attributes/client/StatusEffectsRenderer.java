package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.*;
import java.util.*;

/** Client-only bounded scene data. Layer counts drive auras; actual damage pulses drive links and bursts. */
@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class StatusEffectsRenderer {
    private record Magnet(int id, UUID uuid, long received) {}
    private record SoundCell(int x, int y, int z) {}
    private record Played(long at, boolean burst) {}
    private record Splat(Vec3 position, float size, float angle, long born, int duration, int color) {}
    private record Arc(StatusEffectPayload source, StatusEffectPayload.Target target, Vec3 from, Vec3 to, long born, int duration, ArcShape.Anchors anchors,
                       List<ArcShape.Segment> segments) {}
    private static final Map<UUID, Magnet> MAGNETS = new LinkedHashMap<>();
    private static final Deque<Splat> SPLATS = new ArrayDeque<>();
    private static final Deque<Arc> ARCS = new ArrayDeque<>();
    private static final Map<SoundCell, Played> SOUNDS = new HashMap<>();
    private static final ResourceLocation BLOOD = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "textures/effect/status/blood_splat.png");
    private static final RenderType BLOOD_TYPE = RenderType.entityTranslucent(BLOOD);
    // Alpha blending preserves the arc's hue against bright scenery instead of adding it toward white.
    private static final RenderType ARC_TYPE = RenderType.create("yagens_attributes_status_arc", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE).setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(RenderStateShard.WEATHER_TARGET).createCompositeState(false));
    private static ClientLevel world;
    private static long time;
    private StatusEffectsRenderer() {}
    public static void clear() { MAGNETS.clear(); clearTransient(); world = null; }
    private static void clearTransient() { SPLATS.clear(); ARCS.clear(); SOUNDS.clear(); BloodParticles.clear(); }
    private static boolean world() {
        var level = Minecraft.getInstance().level;
        if (level != world) { MAGNETS.clear(); clearTransient(); world = level; time = 0; }
        return level != null;
    }
    public static void markers(StatusMarkersPayload payload) {
        if (!world()) return;
        if (payload.counts().getOrDefault(DamageType.MAGNETIC, 0) == 0) MAGNETS.remove(payload.entityUuid());
        else if (MAGNETS.size() < 2048 || MAGNETS.containsKey(payload.entityUuid())) MAGNETS.put(payload.entityUuid(), new Magnet(payload.entityId(), payload.entityUuid(), time));
    }
    public static boolean magnetic(Entity entity) {
        var client = Minecraft.getInstance();
        return ClientConfigs.STATUS_VISUALS.get() && MAGNETS.containsKey(entity.getUUID()) && entity.isAlive()
                && client.player != null && !entity.isInvisibleTo(client.player);
    }
    public static void tick() {
        if (!world() || Minecraft.getInstance().isPaused()) return;
        time++;
        if (!ClientConfigs.STATUS_VISUALS.get()) { clearTransient(); return; }
        BloodParticles.tick();
        if (time % 20 == 0) SOUNDS.values().removeIf(value -> time - value.at > 20);
        if (time % 40 == 0) MAGNETS.values().removeIf(value -> time - value.received > 40 && entity(value.id, value.uuid) == null);
        SPLATS.removeIf(splat -> time - splat.born >= splat.duration || time % 10 == 0 && world.getBlockState(BlockPos.containing(splat.position.add(0, -.02, 0))).isAir());
        ARCS.removeIf(arc -> time - arc.born >= arc.duration);
        if (time % 12 == 0) for (var magnet : MAGNETS.values()) {
            var entity = entity(magnet.id, magnet.uuid);
            if (entity == null || !magnetic(entity) || !near(entity.position())) continue;
            var payload = new StatusEffectPayload(entity.getId(), entity.getUUID(), entity.position().add(0, entity.getBbHeight() * .5, 0), DamageType.MAGNETIC, 7,
                    entity.tickCount * 37 + entity.getId(), false, List.of());
            selfArc(payload, entity, new Random(payload.seed()));
        }
    }
    public static void accept(StatusEffectPayload payload) {
        if (!world() || !ClientConfigs.STATUS_VISUALS.get() || !near(payload.position())) return;
        var random = new Random(payload.seed()); var entity = entity(payload.entityId(), payload.entityUuid());
        if (entity != null && Minecraft.getInstance().player != null && entity.isInvisibleTo(Minecraft.getInstance().player)) return;
        switch (payload.effect()) {
            case SLASH -> {
                double width = entity == null ? .5 : entity.getBbWidth();
                for (int i = 0; i < (payload.burst() ? 10 : 5); i++) {
                    var point = payload.position().add((random.nextDouble() - .5) * width * .7, (random.nextDouble() - .5) * .35, (random.nextDouble() - .5) * width * .7);
                    BloodParticles.spawn(point, new Vec3((random.nextDouble() - .5) * .16, .08 + random.nextDouble() * .10, (random.nextDouble() - .5) * .16), payload.lifetime());
                }
            }
            case ELECTRICITY -> {
                if (entity != null) selfArc(payload, entity, random);
                for (var target : payload.targets()) if (!target.uuid().equals(payload.entityUuid())) addArc(payload, target, Vec3.ZERO, Vec3.ZERO, random.nextInt());
            }
            case MAGNETIC -> {
                if (entity != null) for (int i = 0; i < 7; i++) selfArc(payload, entity, random);
            }
            case BLAST -> {
                world.addParticle(payload.burst() ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, true,
                        payload.position().x, payload.position().y, payload.position().z, 0, 0, 0);
                var cell = new SoundCell((int)Math.floor(payload.position().x / 2), (int)Math.floor(payload.position().y / 2), (int)Math.floor(payload.position().z / 2));
                var previous = SOUNDS.get(cell);
                if (previous == null || time - previous.at >= 4 || payload.burst() && !previous.burst) {
                    if (SOUNDS.size() < 256 || previous != null) {
                        SOUNDS.put(cell, new Played(time, payload.burst()));
                        world.playLocalSound(payload.position().x, payload.position().y, payload.position().z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
                                payload.burst() ? .7F : .22F, (payload.burst() ? .9F : 1.2F) + random.nextFloat() * .15F, false);
                    }
                }
            }
            default -> { }
        }
    }
    private static boolean near(Vec3 position) { return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(position) <= 48 * 48; }
    private static Entity entity(int id, UUID uuid) { var entity = world.getEntity(id); return entity != null && entity.getUUID().equals(uuid) ? entity : null; }
    private static void selfArc(StatusEffectPayload source, Entity entity, Random random) {
        double width = Math.max(.3, entity.getBbWidth() * .65), height = entity.getBbHeight();
        Vec3 a = new Vec3((random.nextBoolean() ? 1 : -1) * width, height * (.2 + random.nextDouble() * .6), (random.nextDouble() - .5) * width);
        Vec3 b = new Vec3(-a.x, height * (.2 + random.nextDouble() * .6), -a.z);
        addArc(source, null, a, b, random.nextInt());
    }
    private static void addArc(StatusEffectPayload source, StatusEffectPayload.Target target, Vec3 from, Vec3 to, int seed) {
        var victim = target == null ? null : entity(target.id(), target.uuid());
        double length = victim == null ? from.distanceTo(to) : victim.position().distanceTo(source.position());
        boolean electric = source.effect() == DamageType.ELECTRICITY;
        var segments = ArcShape.build(seed, Math.max(.01, length), electric ? ClientConfigs.ELECTRIC_ARC_BRANCH_DEPTH.get() : 0,
                electric ? ClientConfigs.ELECTRIC_ARC_BRANCH_CHANCE.get() : 0);
        while (ARCS.size() >= 192) ARCS.removeFirst();
        ARCS.addLast(new Arc(source, target, from, to, time, source.lifetime(), ArcShape.anchors(seed), segments));
    }
    public static void splat(Vec3 position, int duration, long seed, int color) {
        if (!world() || duration <= 0 || !ClientConfigs.STATUS_VISUALS.get()) return;
        var hit = world.clip(new ClipContext(position.add(0, .15, 0), position.add(0, -.25, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty()));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getDirection() != net.minecraft.core.Direction.UP) return;
        var random = new Random(seed);
        while (SPLATS.size() >= 512) SPLATS.removeFirst();
        SPLATS.addLast(new Splat(hit.getLocation().add(0, .003 + random.nextDouble() * .002, 0), .025F + random.nextFloat() * .025F,
                random.nextInt(4) * (float)(Math.PI / 2), time, duration, color));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !world() || !ClientConfigs.STATUS_VISUALS.get()) return;
        var client = Minecraft.getInstance(); if (client.player == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false); Vec3 camera = event.getCamera().getPosition();
        var buffers = client.renderBuffers().bufferSource(); var lightning = buffers.getBuffer(ARC_TYPE);
        var matrix = new Matrix4f();
        int remaining = 4096;
        for (var arc : ARCS) {
            if (arc.segments.size() > remaining) break;
            drawArc(lightning, matrix, arc, camera, partial); remaining -= arc.segments.size();
        }
        for (var magnet : MAGNETS.values()) {
            var entity = entity(magnet.id, magnet.uuid);
            if (entity != null && magnetic(entity) && near(entity.position()) && !(client.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer))
                fallback(lightning, matrix, entity, camera, partial);
        }
        buffers.endBatch(ARC_TYPE);
        var blood = buffers.getBuffer(BLOOD_TYPE);
        for (var splat : SPLATS) {
            if (!near(splat.position)) continue;
            float alpha = (float) Math.min(.9, (splat.duration - (time - splat.born) - partial) / 10D);
            int light = LevelRenderer.getLightColor(world, BlockPos.containing(splat.position.add(0, .05, 0)));
            Vec3 center = splat.position.subtract(camera); double c = Math.cos(splat.angle) * splat.size, s = Math.sin(splat.angle) * splat.size;
            textured(blood, matrix, center.add(-c-s, 0, -s+c), 0, 0, alpha, light, splat.color);
            textured(blood, matrix, center.add(c-s, 0, s+c), 1, 0, alpha, light, splat.color);
            textured(blood, matrix, center.add(c+s, 0, s-c), 1, 1, alpha, light, splat.color);
            textured(blood, matrix, center.add(-c+s, 0, -s-c), 0, 1, alpha, light, splat.color);
        }
        buffers.endBatch(BLOOD_TYPE);
    }
    private static void textured(VertexConsumer out, Matrix4f matrix, Vec3 p, float u, float v, float alpha, int light, int color) {
        out.addVertex(matrix, (float)p.x, (float)p.y, (float)p.z).setColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F,
                        (color & 255) / 255F, Math.max(0, alpha)).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }
    private static void drawArc(VertexConsumer out, Matrix4f matrix, Arc arc, Vec3 camera, float partial) {
        var source = entity(arc.source.entityId(), arc.source.entityUuid());
        if (source != null && source.isInvisibleTo(Minecraft.getInstance().player)) return;
        Vec3 from = source == null ? arc.source.position() : source.getPosition(partial)
                .add(0, source.getBbHeight() * arc.anchors.fromHeight(), 0);
        Vec3 to;
        if (arc.target == null) { Vec3 foot = source == null ? arc.source.position() : source.getPosition(partial); from = foot.add(arc.from); to = foot.add(arc.to); }
        else {
            var target = entity(arc.target.id(), arc.target.uuid());
            if (target == null || target.isInvisibleTo(Minecraft.getInstance().player)) return;
            to = target.getPosition(partial).add(0, target.getBbHeight() * arc.anchors.toHeight(), 0);
        }
        float alpha = (float) Math.min(1, (arc.duration - (time - arc.born) - partial) / 3D);
        var delta = to.subtract(from); double length = delta.length(); if (length < .001) return;
        var direction = delta.scale(1 / length);
        var side = direction.cross(Math.abs(direction.y) < .9 ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1)).normalize().scale(length);
        var up = side.cross(direction);
        boolean electric = arc.source.effect() == DamageType.ELECTRICITY;
        double width = electric ? ClientConfigs.ELECTRIC_ARC_WIDTH.get() : .024;
        for (var segment : arc.segments) {
            var a = local(from, delta, side, up, segment.from()); var b = local(from, delta, side, up, segment.to());
            double halfWidth = width * .5 * segment.widthScale();
            if (electric) {
                ribbon(out, matrix, a, b, camera, halfWidth * 3.2, .38F, .015F, .82F, alpha * .32F);
                ribbon(out, matrix, a, b, camera, halfWidth, .67F, .10F, 1F, alpha);
            } else {
                ribbon(out, matrix, a, b, camera, halfWidth * 3.2, .15F, .55F, .95F, alpha * .28F);
                ribbon(out, matrix, a, b, camera, halfWidth, .55F, .85F, 1F, alpha);
            }
        }
    }
    private static Vec3 local(Vec3 origin, Vec3 chord, Vec3 side, Vec3 up, Vec3 point) {
        return origin.add(chord.scale(point.x)).add(side.scale(point.y)).add(up.scale(point.z));
    }
    private static void ribbon(VertexConsumer out, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 camera, double width, float r, float g, float blue, float alpha) {
        Vec3 side = b.subtract(a).cross(camera.subtract(a.add(b).scale(.5))).normalize().scale(width);
        for (var p : List.of(a.subtract(side), a.add(side), b.add(side), b.subtract(side))) {
            var relative = p.subtract(camera); out.addVertex(matrix, (float)relative.x, (float)relative.y, (float)relative.z).setColor(r, g, blue, Math.max(0, alpha));
        }
    }
    private static void fallback(VertexConsumer out, Matrix4f matrix, Entity entity, Vec3 camera, float partial) {
        Vec3 center = entity.getPosition(partial).add(0, entity.getBbHeight() * .5, 0); double radius = entity.getBbWidth() * .65;
        for (int band = 0; band < 3; band++) for (int i = 0; i < 24; i++) {
            double a = i * Math.PI / 12 + time * .015, b = (i+1) * Math.PI / 12 + time * .015;
            Vec3 from = center.add(Math.cos(a)*radius, (band-1)*entity.getBbHeight()*.3, Math.sin(a)*radius);
            Vec3 to = center.add(Math.cos(b)*radius, (band-1)*entity.getBbHeight()*.3, Math.sin(b)*radius);
            ribbon(out, matrix, from, to, camera, .025, .45F, .78F, 1F, .4F);
        }
    }
}
