package yagen.waitmydawn.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;


public class ColdParticle extends Particle {
    private final boolean IS_VALID = ClientConfigs.COLD_VALID.get();
    private final double SUMMON_CHANCE = ClientConfigs.COLD_SUMMON_CHANCE.get();

    public ColdParticle(ClientLevel clientLevel, double x, double y, double z,
                        double width, double height, double ez) {
        super(clientLevel, x, y, z);
        if (!IS_VALID) {
            this.remove();
            return;
        }
        if (this.random.nextDouble() < SUMMON_CHANCE) {
            double xOffset = (this.random.nextDouble() - 0.5) * width;
            double yOffset = (this.random.nextDouble() / 2 + 0.5) * height;
            double zOffset = (this.random.nextDouble() - 0.5) * width;
            clientLevel.addParticle(ParticleTypes.SNOWFLAKE,
                    x+xOffset, y+yOffset, z+zOffset,
                    0, 0, 0);
        }
        this.remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {

    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel clientLevel,
                                       double x, double y, double z,
                                       double xa, double ya, double za) {
            return new ColdParticle(clientLevel, x, y, z, xa, ya, za);
        }
    }
}