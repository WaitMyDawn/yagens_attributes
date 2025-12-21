package yagen.waitmydawn.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;


public class BlastParticle extends Particle {
    private final boolean IS_VALID = ClientConfigs.BLAST_VALID.get();
    private final double EACH_DISTANCE = ClientConfigs.BLAST_EACH_DISTANCE.get();
    private final int EACH_LEVEL = ClientConfigs.BLAST_EACH_LEVEL.get();
    private final int MAX_COUNT = ClientConfigs.BLAST_MAX_COUNT.get();

    public BlastParticle(ClientLevel clientLevel, double x, double y, double z,
                         double level, double ey, double ez) {
        super(clientLevel, x, y, z);
        if (!IS_VALID) {
            this.remove();
            return;
        }
        Vec3 center = new Vec3(x, y, z);
        int count = 0;
        for (int i = 0; i < level; i++) {
            for (int j = 0; j < EACH_LEVEL; j++) {
                Vec3 newCenter = center
                        .add(randomDirection(this.random).scale((i + 1) * EACH_DISTANCE));
                clientLevel.addParticle(ParticleTypes.EXPLOSION,
                        newCenter.x, newCenter.y, newCenter.z,
                        0, 0, 0);
                count++;
            }
            if (count >= MAX_COUNT) {
                this.remove();
                return;
            }
        }
        this.remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {

    }

    public static Vec3 randomDirection(RandomSource random) {
        float theta = random.nextFloat() * 2 * (float) Math.PI;
        float phi = (float) Math.acos(2 * random.nextFloat() - 1);

        float sinPhi = Mth.sin(phi);
        float x = sinPhi * Mth.cos(theta);
        float y = sinPhi * Mth.sin(theta);
        float z = Mth.cos(phi);

        return new Vec3(x, y, z);
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
                                       double level, double ya, double za) {
            return new BlastParticle(clientLevel, x, y, z, level, ya, za);
        }
    }
}