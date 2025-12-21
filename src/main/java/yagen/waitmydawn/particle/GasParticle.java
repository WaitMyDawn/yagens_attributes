package yagen.waitmydawn.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;


public class GasParticle extends TextureSheetParticle {
    private final int LIFE_TIME = ClientConfigs.GAS_LIFE_TIME.get();
    private final float MAX_SIZE = (float) ClientConfigs.GAS_MAX_SIZE.get().doubleValue();
    private final boolean IS_VALID = ClientConfigs.GAS_VALID.get();

    private final SpriteSet sprites;

    public GasParticle(ClientLevel level, double x, double y, double z,
                       double radius, double height, double ez, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        if (!IS_VALID) {
            this.remove();
            return;
        }
        double angle = this.random.nextDouble() * Math.PI * 2;
        double distance = (this.random.nextDouble() / 2 + 0.75) * radius;
        double offsetX = Math.cos(angle) * distance;
        double offsetY = this.random.nextDouble() * height;
        double offsetZ = Math.sin(angle) * distance;
        this.setPos(x + offsetX, y + offsetY, z + offsetZ);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lifetime = LIFE_TIME;
        this.quadSize = 0;
        this.hasPhysics = false;
        this.yd = 0.05D + ((this.random.nextDouble() - 0.5) * 0.025D);
        this.xd = 0;
        this.zd = 0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float lifeRatio = (float) this.age / (float) this.lifetime;
        this.quadSize = MAX_SIZE * lifeRatio;

        this.setSpriteFromAge(this.sprites);
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
                                       double radius, double height, double za) {
            return new GasParticle(lvl, x, y, z, radius, height, za, sprites);
        }
    }
}