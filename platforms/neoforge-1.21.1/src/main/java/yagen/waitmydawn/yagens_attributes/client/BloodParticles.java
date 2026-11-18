package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.registries.StatusParticles;
import java.util.*;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class BloodParticles {
    private static final int CELLS_PER_SIDE = 4;
    private static SpriteSet sprites;
    private static final Deque<Particle> OWNED = new ArrayDeque<>();

    // Keep cell coordinates relative to the sprite so marks can resolve the atlas again after a reload.
    record Appearance(int cell, float halfSize, float angle) {
        float u0() { return (cell % CELLS_PER_SIDE) / (float)CELLS_PER_SIDE; }
        float u1() { return u0() + 1F / CELLS_PER_SIDE; }
        float v0() { return (cell / CELLS_PER_SIDE) / (float)CELLS_PER_SIDE; }
        float v1() { return v0() + 1F / CELLS_PER_SIDE; }
    }

    private BloodParticles() {}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(StatusParticles.BLOOD.get(), set -> {
            sprites = set;
            return (type, level, x, y, z, dx, dy, dz) -> ClientConfigs.STATUS.shows(DamageType.SLASH)
                    ? tracked(new Drop(level, x, y, z, dx, dy, dz, 60, set)) : null;
        });
    }
    private static Particle tracked(Particle particle) {
        while (OWNED.size() >= 256) OWNED.removeFirst().remove();
        OWNED.addLast(particle); return particle;
    }
    public static void spawn(Vec3 position, Vec3 velocity, int lifetime) {
        var client = Minecraft.getInstance();
        if (client.level == null || sprites == null) return;
        client.particleEngine.add(tracked(new Drop(client.level, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, lifetime, sprites)));
    }
    public static void clear() { OWNED.forEach(Particle::remove); OWNED.clear(); }
    public static void tick() { OWNED.removeIf(value -> !value.isAlive()); }
    static TextureAtlasSprite sheet() { return sprites.get(0, 1); }

    private static final class Drop extends TextureSheetParticle {
        private final Appearance appearance;

        Drop(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, int ticks, SpriteSet sprites) {
            super(level, x, y, z);
            setSprite(sprites.get(0, 1));
            lifetime = ticks;
            xd = dx; yd = dy; zd = dz; gravity = .8F; friction = .94F;
            int cell = random.nextInt(CELLS_PER_SIDE * CELLS_PER_SIDE);
            float size = (.025F + random.nextFloat() * .025F) * ClientConfigs.STATUS.bloodSize.get().floatValue();
            float angle = random.nextInt(8) * (float)(Math.PI / 4);
            appearance = new Appearance(cell, size, angle);
            quadSize = appearance.halfSize();
            setSize(.018F, .018F);
            roll = oRoll = appearance.angle();
            setColor(.55F + random.nextFloat() * .25F, .025F + random.nextFloat() * .04F, .025F + random.nextFloat() * .02F);
        }
        @Override protected float getU0() { return sprite.getU(appearance.u0()); }
        @Override protected float getU1() { return sprite.getU(appearance.u1()); }
        @Override protected float getV0() { return sprite.getV(appearance.v0()); }
        @Override protected float getV1() { return sprite.getV(appearance.v1()); }
        @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
        @Override public void tick() {
            super.tick();
            alpha = Math.min(1, Math.max(0, (lifetime - age) / 10F));
            if (onGround && isAlive()) {
                int color = Math.round(rCol * 255) << 16 | Math.round(gCol * 255) << 8 | Math.round(bCol * 255);
                StatusEffectsRenderer.splat(new Vec3(x, y, z), lifetime - age, random.nextDouble(), color, appearance);
                remove();
            }
        }
    }
}
