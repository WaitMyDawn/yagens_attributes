package yagen.waitmydawn.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;

import java.util.ArrayList;
import java.util.List;

public class ElectricityParticle extends TextureSheetParticle {
    private final int MAX_GENERATIONS = ClientConfigs.ELECTRICITY_MAX_GENERATIONS.get();
    private final int BRANCH_GENERATION_LIMIT = ClientConfigs.ELECTRICITY_BRANCH_GENERATION_LIMIT.get();
    private final int REFRESH_RATE = ClientConfigs.ELECTRICITY_REFRESH_RATE.get();
    private final double JITTER_STRENGTH = ClientConfigs.ELECTRICITY_JITTER_STRENGTH.get();
    private final float BRANCH_CHANCE = (float) ClientConfigs.ELECTRICITY_BRANCH_CHANCE.get().doubleValue();
    private final float SIZE = (float) ClientConfigs.ELECTRICITY_SIZE.get().doubleValue();
    private final int LIFE_TIME = ClientConfigs.ELECTRICITY_LIFE_TIME.get();
    private final boolean IS_VALID = ClientConfigs.ELECTRICITY_VALID.get();

    private final Vec3 target;
    private final List<Segment> segments = new ArrayList<>();
    private final SpriteSet sprites;
    private final double startX, startY, startZ;

    public ElectricityParticle(ClientLevel level, double x, double y, double z,
                               double ex, double ey, double ez, SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.target = new Vec3(ex, ey, ez);
        this.lifetime = LIFE_TIME;
        this.quadSize = SIZE; // width
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;

        // init
        if(IS_VALID) generateLightning();
    }

    @Override
    public void tick() {
        super.tick();
        // regenerate by tick(s)
        if (!IS_VALID) {
            this.remove();
            return;
        }
        if (this.age % REFRESH_RATE == 0) {
            generateLightning();
        }

        // gradually transparent by time
        this.alpha = 1.0f - (float) age / lifetime;
    }

    private void generateLightning() {
        segments.clear();
        Vec3 start = new Vec3(startX, startY, startZ);
        generateBolt(start, target, JITTER_STRENGTH, 0);
    }

    /**
     * generateBolt
     *
     * @param p1           start
     * @param p2           end
     * @param displacement current offset intensity
     * @param generation   control
     */
    private void generateBolt(Vec3 p1, Vec3 p2, double displacement, int generation) {
        if (displacement < 0.05 || generation >= MAX_GENERATIONS) {
            segments.add(new Segment(p1, p2, 1.0f / (generation + 1)));
            return;
        }

        // random offset
        Vec3 mid = p1.add(p2).scale(0.5);
        Vec3 dir = p2.subtract(p1).normalize();
        Vec3 randomVec = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5);
        Vec3 offsetDir = dir.cross(randomVec).normalize();
        if (offsetDir.lengthSqr() == 0) offsetDir = new Vec3(0, 1, 0);

        // apply offset
        double scale = (random.nextDouble() - 0.5) * displacement * 2.0;
        mid = mid.add(offsetDir.scale(scale));

        // two part
        generateBolt(p1, mid, displacement * 0.5, generation);
        generateBolt(mid, p2, displacement * 0.5, generation);

        // random branch
        if (generation < BRANCH_GENERATION_LIMIT && random.nextFloat() < BRANCH_CHANCE) {
            Vec3 branchDir = mid.subtract(p1).normalize();
            // far from main branch
            Vec3 branchOffset = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
            branchDir = branchDir.add(branchOffset.scale(0.8)).normalize();

            double length = p1.distanceTo(p2) * 0.6;
            Vec3 branchEnd = mid.add(branchDir.scale(length));

            // smaller by iteration
            generateBolt(mid, branchEnd, displacement * 0.5, generation + 1);
        }
    }

    @Override
    public void render(@NotNull VertexConsumer vc, @NotNull Camera camera, float pt) {
        Vec3 cam = camera.getPosition();

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        for (Segment seg : segments) {
            drawSegment(vc, seg, cam, u0, u1, v0, v1);
        }
    }

    private void drawSegment(VertexConsumer buf, Segment seg, Vec3 cam,
                             float u0, float u1, float v0, float v1) {
        Vec3 start = seg.start;
        Vec3 end = seg.end;

        float x1 = (float) (start.x - cam.x);
        float y1 = (float) (start.y - cam.y);
        float z1 = (float) (start.z - cam.z);
        float x2 = (float) (end.x - cam.x);
        float y2 = (float) (end.y - cam.y);
        float z2 = (float) (end.z - cam.z);

        Vec3 dir = end.subtract(start);
        if (dir.lengthSqr() == 0) return;
        dir = dir.normalize();

        Vec3 toCamera = new Vec3(-x1, -y1, -z1);
        Vec3 side = dir.cross(toCamera);
        if (side.lengthSqr() == 0) side = new Vec3(1, 0, 0);

        float currentWidth = this.quadSize * seg.widthScale;
        side = side.normalize().scale(currentWidth);

        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = this.alpha;
        int light = 15728880; // Full bright

        float overlapAmount = currentWidth * 0.55f;
        Vec3 overlapVec = dir.scale(overlapAmount);

        // extend for overlap
        float sx = x1 - (float) overlapVec.x;
        float sy = y1 - (float) overlapVec.y;
        float sz = z1 - (float) overlapVec.z;

        float ex = x2 + (float) overlapVec.x;
        float ey = y2 + (float) overlapVec.y;
        float ez = z2 + (float) overlapVec.z;

        // P1: Start - Side
        addVertex(buf, sx - (float) side.x, sy - (float) side.y, sz - (float) side.z, u1, v1, r, g, b, a, light);
        // P2: Start + Side
        addVertex(buf, sx + (float) side.x, sy + (float) side.y, sz + (float) side.z, u1, v0, r, g, b, a, light);
        // P3: End + Side
        addVertex(buf, ex + (float) side.x, ey + (float) side.y, ez + (float) side.z, u0, v0, r, g, b, a, light);
        // P4: End - Side
        addVertex(buf, ex - (float) side.x, ey - (float) side.y, ez - (float) side.z, u0, v1, r, g, b, a, light);
    }

    private void addVertex(VertexConsumer buf, float x, float y, float z,
                           float u, float v, float r, float g, float b, float a, int light) {
        buf.addVertex(x, y, z)
                .setUv(u, v)
                .setColor(r, g, b, a)
                .setLight(light)
                .setNormal(0, 1, 0);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static class Segment {
        final Vec3 start;
        final Vec3 end;
        final float widthScale;

        public Segment(Vec3 start, Vec3 end, float widthScale) {
            this.start = start;
            this.end = end;
            this.widthScale = widthScale;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel lvl,
                                       double x, double y, double z,
                                       double xa, double ya, double za) {
            return new ElectricityParticle(lvl, x, y, z, xa, ya, za, sprites);
        }
    }
}