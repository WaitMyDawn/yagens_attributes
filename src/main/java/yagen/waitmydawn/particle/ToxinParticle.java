package yagen.waitmydawn.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;


public class ToxinParticle extends TextureSheetParticle {
    private final int LIFE_TIME = ClientConfigs.TOXIN_LIFE_TIME.get();
    private final float SIZE = (float) ClientConfigs.TOXIN_SIZE.get().doubleValue();
    private final boolean IS_VALID = ClientConfigs.TOXIN_VALID.get();

    private final SpriteSet sprites;
    private final float initialSize = SIZE;

    public ToxinParticle(ClientLevel level, double x, double y, double z,
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
        this.quadSize = this.initialSize;
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

        this.setSpriteFromAge(this.sprites);
        this.move(this.xd, this.yd, this.zd);
        this.yd *= 0.95D;
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
            return new ToxinParticle(lvl, x, y, z, radius, height, za, sprites);
        }
    }
}