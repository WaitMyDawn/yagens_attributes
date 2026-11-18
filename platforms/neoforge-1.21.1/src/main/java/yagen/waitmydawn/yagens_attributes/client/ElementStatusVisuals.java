package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.core.StatusRules;
import yagen.waitmydawn.yagens_attributes.network.StatusEffectPayload;
import yagen.waitmydawn.yagens_attributes.network.StatusMarkersPayload;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class ElementStatusVisuals {
    record Host(StatusMarkersPayload snapshot, long received) {
        int id() { return snapshot.entityId(); }
        UUID uuid() { return snapshot.entityUuid(); }
        boolean frozen() { return snapshot.frozen(); }
        int frost() { return layers(DamageType.COLD); }
        int heat() { return layers(DamageType.HEAT); }
        int gas() { return layers(DamageType.GAS); }
        int layers(DamageType type) { return snapshot.counts().getOrDefault(type, 0); }
    }
    record Active(Host host, Entity entity) {}
    private static final List<DamageType> AMBIENT = List.of(DamageType.COLD, DamageType.HEAT, DamageType.GAS,
            DamageType.RADIATION, DamageType.CORROSIVE, DamageType.VIRAL);
    private record Flash(UUID uuid, long end) {}
    private record Mote(DamageType type, Vec3 at, Vec3 velocity, float size, int cell, long born, int lifetime) {}

    private static final int HOST_LIMIT = 2048, ACTIVE_LIMIT = 48, MOTE_LIMIT = 256, GAS_LIMIT = 12;
    private static final double TURN = 2 * Math.PI;
    private static final LinkedHashMap<Integer, Host> HOSTS = new LinkedHashMap<>();
    private static final LinkedHashMap<Integer, Flash> FLASHES = new LinkedHashMap<>();
    private static final ArrayDeque<Mote> MOTES = new ArrayDeque<>();
    private static final RenderType FROST = sheet("frost_shard");
    private static final RenderType HEAT = sheet("heat_flame");
    private static final RenderType TOXIN = sheet("toxin_bubble");
    private static final RenderType GAS = sheet("gas_puff");
    static final RenderType COLOR_QUADS = RenderType.create("yagens_attributes_status_color", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(RenderStateShard.WEATHER_TARGET).createCompositeState(false));
    private static ClientLevel world;
    private static long tick;
    private static List<Active> active = List.of();

    private ElementStatusVisuals() {}

    static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("yagens_attributes", "textures/effect/status/" + name + ".png");
    }

    static RenderType sheet(String name) { return RenderType.entityTranslucent(texture(name)); }

    static void clearTransient() {
        FLASHES.clear();
        MOTES.clear();
        ProcStatusVisuals.clear();
    }

    private static boolean inWorld() {
        var level = Minecraft.getInstance().level;
        if (level != world) {
            HOSTS.clear();
            clearTransient();
            active = List.of();
            world = level;
            tick = 0;
        }
        return world != null;
    }

    static void markers(StatusMarkersPayload packet) {
        if (!inWorld()) return;
        int frost = packet.counts().getOrDefault(DamageType.COLD, 0);
        var old = HOSTS.get(packet.entityId());
        if (old != null && old.uuid().equals(packet.entityUuid()) && old.frozen() && !packet.frozen() && frost > 0
                && ClientConfigs.STATUS.shows(DamageType.COLD)) {
            var entity = entity(old);
            if (entity != null && visible(entity) && near(entity.position())) frost(entity, frost, true);
        }
        if (AMBIENT.stream().noneMatch(packet.counts()::containsKey)) {
            if (old != null && old.uuid().equals(packet.entityUuid())) HOSTS.remove(packet.entityId());
            return;
        }
        if (!HOSTS.containsKey(packet.entityId()) && HOSTS.size() >= HOST_LIMIT) HOSTS.pollFirstEntry();
        HOSTS.put(packet.entityId(), new Host(packet, tick));
    }

    static boolean frozen(LivingEntity entity) {
        if (world != entity.level() || !ClientConfigs.STATUS.shows(DamageType.COLD)) return false;
        var host = HOSTS.get(entity.getId());
        return host != null && host.frozen() && host.uuid().equals(entity.getUUID()) && visible(entity) && near(entity.position());
    }

    static void pulse(StatusEffectPayload packet) {
        var type = packet.effect();
        if (!inWorld() || !ClientConfigs.STATUS.shows(type) || !near(packet.position())) return;
        var entity = world.getEntity(packet.entityId());
        if (entity != null && (!entity.getUUID().equals(packet.entityUuid())
                || Minecraft.getInstance().player != null && entity.isInvisibleTo(Minecraft.getInstance().player))) return;
        boolean fire = type == DamageType.HEAT;
        if (fire) {
            if (!FLASHES.containsKey(packet.entityId()) && FLASHES.size() >= 256) FLASHES.pollFirstEntry();
            FLASHES.put(packet.entityId(), new Flash(packet.entityUuid(), tick + packet.lifetime()));
        }
        var random = new Random(packet.seed());
        double width = entity == null ? .6 : entity.getBbWidth();
        int count = density(type, fire ? 3 : 6);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * TURN;
            var outward = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            double distance = fire ? .34 + .32 * random.nextDouble() : .68 + .38 * random.nextDouble();
            var at = packet.position().add(outward.scale(width * distance))
                    .add(0, (random.nextDouble() - (fire ? .5 : .16)) * width, 0);
            float size = (fire ? .025F : .105F) + random.nextFloat() * (fire ? .03F : .075F);
            var motion = outward.scale(fire ? .01 : .018).add(0, .025 + random.nextDouble() * .022, 0);
            addMote(new Mote(type, at, motion, size, random.nextInt(3), tick, 11 + random.nextInt(8)));
        }
    }

    static void physicalPulse(StatusEffectPayload packet) {
        if (!inWorld() || !ClientConfigs.STATUS.shows(packet.effect()) || !near(packet.position())) return;
        var entity = world.getEntity(packet.entityId());
        if (entity != null && (!entity.getUUID().equals(packet.entityUuid()) || !visible(entity))) return;
        ProcStatusVisuals.accept(packet, entity == null ? .6F : entity.getBbWidth(), tick);
    }

    static void tick() {
        if (!inWorld() || Minecraft.getInstance().isPaused()) return;
        tick++;
        ProcStatusVisuals.tick(tick);
        FLASHES.values().removeIf(flash -> flash.end() <= tick);
        MOTES.removeIf(mote -> tick - mote.born() >= mote.lifetime());
        if (tick % 20 == 0)
            HOSTS.values().removeIf(host -> tick - host.received() > 40 && entity(host) == null);
        if (!ClientConfigs.STATUS.visible.get()) {
            active = List.of();
            return;
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        active = HOSTS.values().stream().map(host -> new Active(host, entity(host)))
                .filter(entry -> entry.entity() != null && visible(entry.entity()) && near(entry.entity().position()))
                .sorted(Comparator.comparingDouble(entry -> entry.entity().distanceToSqr(camera))).limit(ACTIVE_LIMIT).toList();
        if (!ClientConfigs.STATUS.shows(DamageType.COLD)) return;
        for (var entry : active) {
            var host = entry.host();
            if (host.frost() == 0 || (tick + host.id()) % 7 != 0) continue;
            boolean hasShell = host.frozen() && Minecraft.getInstance().getEntityRenderDispatcher()
                    .getRenderer(entry.entity()) instanceof LivingEntityRenderer;
            if (!hasShell) frost(entry.entity(), host.frost(), false);
        }
    }

    private static void frost(Entity entity, int layers, boolean thaw) {
        var random = new Random(tick * 31 + entity.getId());
        int count = density(DamageType.COLD, thaw ? 12 : 2 + Math.min(4, (layers - 1) / 2));
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * TURN;
            var outward = new Vec3(Math.cos(a), 0, Math.sin(a));
            double fraction = thaw ? .1 + .8 * random.nextDouble() : .10 + .22 * random.nextInt(3);
            Vec3 at = entity.position().add(outward.scale(entity.getBbWidth() * .58)).add(0, fraction * entity.getBbHeight(), 0);
            Vec3 motion = outward.scale(thaw ? .045 + random.nextDouble() * .04 : .006)
                    .add(0, thaw ? .03 + random.nextDouble() * .045 : .004, 0);
            addMote(new Mote(DamageType.COLD, at, motion, .065F + random.nextFloat() * .06F, random.nextInt(4), tick,
                    (thaw ? 12 : 9) + random.nextInt(8)));
        }
    }

    private static void addMote(Mote mote) {
        if (MOTES.size() >= MOTE_LIMIT) MOTES.removeFirst();
        MOTES.addLast(mote);
    }

    private static Entity entity(Host host) {
        var entity = world.getEntity(host.id());
        return entity != null && host.uuid().equals(entity.getUUID()) ? entity : null;
    }

    static boolean visible(Entity entity) {
        var player = Minecraft.getInstance().player;
        return player != null && entity.isAlive() && !entity.isInvisibleTo(player);
    }

    static boolean near(Vec3 position) {
        int range = ClientConfigs.STATUS.distance.get();
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(position) <= (double)range * range;
    }

    static int density(DamageType type, int base) {
        return Math.round(base * ClientConfigs.STATUS.effect(type).density().get() / 100F);
    }

    static float scale(DamageType type) { return ClientConfigs.STATUS.effect(type).scale().get().floatValue(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !inWorld() || !ClientConfigs.STATUS.visible.get()) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double time = tick + (Minecraft.getInstance().isPaused() ? 0 : partial);
        Vec3 camera = event.getCamera().getPosition();
        float yaw = event.getCamera().getYRot();
        Vec3 right = new Vec3(1, 0, 0).yRot((float)Math.toRadians(-yaw));
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        var matrix = new Matrix4f();

        if (ClientConfigs.STATUS.shows(DamageType.GAS)) {
            gasGround(buffers.getBuffer(COLOR_QUADS), matrix, camera, partial);
            buffers.endBatch(COLOR_QUADS);
            var fog = buffers.getBuffer(GAS);
            gas(fog, matrix, camera, right, time, partial);
            buffers.endBatch(GAS);
        }
        if (ClientConfigs.STATUS.shows(DamageType.HEAT)) {
            var fire = buffers.getBuffer(HEAT);
            heat(fire, matrix, camera, right, time, partial);
            motes(fire, matrix, DamageType.HEAT, camera, right, time);
            buffers.endBatch(HEAT);
        }
        if (ClientConfigs.STATUS.shows(DamageType.COLD)) {
            motes(buffers.getBuffer(FROST), matrix, DamageType.COLD, camera, right, time);
            buffers.endBatch(FROST);
        }
        if (ClientConfigs.STATUS.shows(DamageType.TOXIN)) {
            motes(buffers.getBuffer(TOXIN), matrix, DamageType.TOXIN, camera, right, time);
            buffers.endBatch(TOXIN);
        }
        ProcStatusVisuals.render(buffers, matrix, camera, right, time, partial, active);
    }

    private static void heat(VertexConsumer out, Matrix4f matrix, Vec3 camera, Vec3 right, double time, float partial) {
        int count = density(DamageType.HEAT, 4);
        float size = scale(DamageType.HEAT);
        for (var entry : active) {
            var host = entry.host();
            var entity = entry.entity();
            if (host.heat() == 0 || !visible(entity) || entity == Minecraft.getInstance().player
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson()) continue;
            Vec3 anchor = entity.getPosition(partial).subtract(camera);
            var pulse = FLASHES.get(host.id());
            float flash = pulse != null && pulse.uuid().equals(host.uuid())
                    ? (float)Math.clamp((pulse.end() - time) / 8D, 0D, 1D) : 0;
            float height = Math.clamp(entity.getBbHeight() * .24F, .20F, .8F) * size * (1 + flash * .15F);
            for (int i = 0; i < count; i++) {
                double angle = TURN * i / count + host.id() * .23;
                double offset = entity.getBbWidth() * .52;
                var at = anchor.add(Math.cos(angle) * offset,
                        height * .8 + (i % 3 == 0 ? entity.getBbHeight() * .28 : 0),
                        Math.sin(angle) * offset);
                int frame = Math.floorMod((int)(time / 3) + host.id() + i * 3, 4);
                billboard(out, matrix, at, right, height * .61F, height, .70F + flash * .3F, frame);
            }
        }
    }

    private static void gas(VertexConsumer out, Matrix4f matrix, Vec3 camera, Vec3 right, double time, float partial) {
        int budget = GAS_LIMIT;
        int density = density(DamageType.GAS, 22);
        float opacity = ClientConfigs.STATUS.gasOpacity.get().floatValue();
        float scale = scale(DamageType.GAS);
        if (density == 0 || opacity == 0) return;
        for (var entry : active) {
            int stacks = entry.host().gas();
            if (stacks == 0 || !visible(entry.entity())) continue;
            if (budget-- == 0) break;
            double radius = StatusRules.gasRadius(stacks);
            var anchor = entry.entity().getPosition(partial).subtract(camera);
            for (int i = 0; i < density; i++) {
                double phase = i * 2.399963 + entry.host().id() * .17;
                double drift = Math.sin(time * .041 + phase) * .09;
                float halfWidth = (float)Math.min(radius * .29, (.69 + (i % 5) * .11) * scale);
                float halfHeight = (.23F + (i % 3) * .07F) * (float)Math.sqrt(scale);
                double radial = (radius - halfWidth) * Math.min(.99, Math.sqrt((i + .5) / density) + drift);
                double angle = phase + Math.sin(time * .021 + phase) * .18;
                var at = anchor.add(Math.cos(angle) * radial, .06 + halfHeight + .07 * Math.sin(time * .055 + phase), Math.sin(angle) * radial);
                float alpha = opacity * (float)(.73 + .20 * Math.sin(time * .067 + phase) - .15 * radial / radius);
                billboard(out, matrix, at, right, halfWidth, halfHeight, alpha, i % 4);
            }
        }
    }

    private static void gasGround(VertexConsumer out, Matrix4f matrix, Vec3 camera, float partial) {
        float haze = ClientConfigs.STATUS.gasOpacity.get().floatValue() * .48F;
        if (ClientConfigs.STATUS.effect(DamageType.GAS).density().get() == 0) haze = 0;
        boolean outline = ClientConfigs.STATUS.gasOutline.get();
        if (haze == 0 && !outline) return;
        int budget = GAS_LIMIT;
        for (var entry : active) {
            double radius = StatusRules.gasRadius(entry.host().gas());
            if (radius == 0 || !visible(entry.entity())) continue;
            if (budget-- == 0) break;
            var origin = entry.entity().getPosition(partial).subtract(camera).add(0, .034, 0);
            for (int i = 0; i < 40; i++) {
                double a = TURN * i / 40, b = TURN * (i + 1) / 40;
                if (haze > 0) {
                    groundBand(out, matrix, origin, 0, radius * .62, a, b, haze, haze, false);
                    groundBand(out, matrix, origin, radius * .62, radius, a, b, haze, 0, false);
                }
                if (outline) groundBand(out, matrix, origin, radius - .022, radius, a, b, .53F, .53F, true);
            }
        }
    }

    private static void groundBand(VertexConsumer out, Matrix4f matrix, Vec3 origin, double inner, double outer,
                                   double a, double b, float inside, float outside, boolean outline) {
        groundVertex(out, matrix, origin, inner, a, inside, outline);
        groundVertex(out, matrix, origin, outer, a, outside, outline);
        groundVertex(out, matrix, origin, outer, b, outside, outline);
        groundVertex(out, matrix, origin, inner, b, inside, outline);
    }

    private static void groundVertex(VertexConsumer out, Matrix4f matrix, Vec3 at, double radius, double angle, float alpha, boolean outline) {
        out.addVertex(matrix, (float)(at.x + Math.cos(angle) * radius), (float)at.y, (float)(at.z + Math.sin(angle) * radius))
                .setColor(outline ? .71F : .43F, outline ? .77F : .48F, outline ? .36F : .21F, alpha);
    }

    private static void motes(VertexConsumer out, Matrix4f matrix, DamageType type, Vec3 camera, Vec3 right, double time) {
        float scale = scale(type);
        for (var mote : MOTES) {
            if (mote.type() != type || !near(mote.at())) continue;
            double age = Math.max(0, time - mote.born());
            float progress = (float)(age / mote.lifetime());
            if (progress >= 1) continue;
            Vec3 at = mote.at().add(mote.velocity().scale(age)).subtract(camera);
            if (type == DamageType.COLD) at = at.add(0, -.003 * age * age, 0);
            boolean pop = type == DamageType.TOXIN && progress >= .76F;
            float size = mote.size() * scale * (pop ? 1.24F : 1);
            float alpha = Math.min(.94F, (1 - progress) * (pop ? 4.2F : 3F));
            billboard(out, matrix, at, right, size, size, alpha, pop ? 3 : mote.cell());
        }
    }

    static void billboard(VertexConsumer out, Matrix4f matrix, Vec3 center, Vec3 right,
                                  float width, float height, float alpha, int cell) {
        billboard(out, matrix, center, right.scale(width), new Vec3(0, height, 0), alpha, cell);
    }

    static void billboard(VertexConsumer out, Matrix4f matrix, Vec3 center, Vec3 along, Vec3 up, float alpha, int cell) {
        Vec3 top = center.add(up), bottom = center.subtract(up);
        float span = .5F;
        float u = (cell % 2) * span, v = (cell / 2) * span;
        vertex(out, matrix, top.subtract(along), u, v, alpha);
        vertex(out, matrix, bottom.subtract(along), u, v + span, alpha);
        vertex(out, matrix, bottom.add(along), u + span, v + span, alpha);
        vertex(out, matrix, top.add(along), u + span, v, alpha);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Vec3 point, float u, float v, float alpha) {
        out.addVertex(matrix, (float)point.x, (float)point.y, (float)point.z).setColor(1F, 1F, 1F, alpha)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }
}
