package yagen.waitmydawn.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.config.ClientConfigs;

import static yagen.waitmydawn.api.util.DamageCompat.linearGrowth;
import static yagen.waitmydawn.api.util.DamageCompat.logGrowth;

public class DamageNumberParticle extends Particle {
    private final Component text;
    private final double damage;
    private int color = 0xFFFFFF;

    private static final float RISE_SPEED = 0.01F;
    private static final float BOX_LENGTH = 0.8F;
    private static final float BASIC_SCALE = 0.015f;
    private final float fontScale;

    public DamageNumberParticle(ClientLevel level, double x, double y, double z,
                                double amount, double dColor, double dLevel) {
        super(level, x, y, z);
        this.lifetime = 10 + Math.min(20, (int) (20 * amount));
        this.damage = amount;

        double dx = (level.random.nextDouble() - 0.5) * BOX_LENGTH;
        double dy = (level.random.nextDouble() - 0.5) * BOX_LENGTH;
        double dz = (level.random.nextDouble() - 0.5) * BOX_LENGTH;
        this.setPos(x + dx, y + dy, z + dz);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        float scale;

        if (damage <= 1000)
            scale = (float) linearGrowth(damage, 0, 1000, BASIC_SCALE, BASIC_SCALE * 1.5);
        else if (damage <= 10000)
            scale = (float) linearGrowth(damage, 1000, 10000, BASIC_SCALE * 1.5, BASIC_SCALE * 2);
        else
            scale = (float) logGrowth(damage, 10000, BASIC_SCALE * 2, BASIC_SCALE * 3, 50000);

        this.fontScale = scale;

        int criticalLevel = (int) dLevel;
        if (criticalLevel == 1) dColor = 0xFFFF00;
        else if (criticalLevel == 2) dColor = 0xFF4500;
        else if (criticalLevel >= 3) dColor = 0xFF0000;

        int signCount = criticalLevel >= 4 ? criticalLevel - 3 : 0;
        String signStr = "!".repeat(signCount);

        this.text = Component.literal(String.format("%.1f", amount) + signStr);
        this.color = (int) dColor;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.y += (RISE_SPEED / Math.max(1, damage));
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(@NotNull VertexConsumer vc, Camera cam, float pt) {
        Vec3 camPos = cam.getPosition();
        float px = (float) (Mth.lerp(pt, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(pt, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(pt, zo, z) - camPos.z());

        PoseStack pose = new PoseStack();
        pose.pushPose();
        pose.translate(px, py, pz);

        float distance = (float) cam.getPosition().distanceTo(new Vec3(x, y, z));
        float distanceScale = fontScale * Math.max(1f, distance / ClientConfigs.DAMAGE_NUMBER_ENLARGE.get());

        pose.mulPose(cam.rotation());
        pose.scale(distanceScale, -distanceScale, distanceScale);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        Font font = Minecraft.getInstance().font;
        int light = LightTexture.FULL_BRIGHT;
        float xOff = -font.width(text) / 2f;

        font.drawInBatch(text, xOff, 0,
                FastColor.ARGB32.color(255,
                        FastColor.ARGB32.red(color),
                        FastColor.ARGB32.green(color),
                        FastColor.ARGB32.blue(color)),
                false, pose.last().pose(), buffer,
                Font.DisplayMode.SEE_THROUGH, 0, light);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        pose.popPose();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel lvl,
                                       double x, double y, double z,
                                       double xa, double ya, double za) {
            return new DamageNumberParticle(lvl, x, y, z, xa, ya, za);
        }
    }
}