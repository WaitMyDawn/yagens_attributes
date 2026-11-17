package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import yagen.waitmydawn.yagens_attributes.registries.StatusParticles;
import java.util.*;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class BloodParticles {
    private static SpriteSet sprites;
    private static final Deque<Particle> OWNED = new ArrayDeque<>();
    private BloodParticles() {}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(StatusParticles.BLOOD.get(), set -> {
            sprites = set;
            return (type, level, x, y, z, dx, dy, dz) -> tracked(new Drop(level, x, y, z, dx, dy, dz, 60, set));
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
    private static final class Drop extends TextureSheetParticle {
        Drop(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, int ticks, SpriteSet sprites) {
            super(level, x, y, z); pickSprite(sprites); lifetime = ticks;
            xd = dx; yd = dy; zd = dz; gravity = .8F; friction = .94F;
            quadSize = .0125F * (1 + random.nextInt(3)); setSize(.018F, .018F);
            setColor(.55F + random.nextFloat() * .25F, .025F + random.nextFloat() * .04F, .025F + random.nextFloat() * .02F);
        }
        @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
        @Override public void tick() {
            super.tick();
            alpha = Math.min(1, Math.max(0, (lifetime - age) / 10F));
            if (onGround && isAlive()) {
                int color = (int)(rCol * 190) << 16 | (int)(gCol * 190) << 8 | (int)(bCol * 190);
                StatusEffectsRenderer.splat(new Vec3(x, y, z), lifetime - age, random.nextLong(), color); remove();
            }
        }
    }
}
