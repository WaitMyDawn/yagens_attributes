package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.network.StatusVisualPayload;

/** Sparse geometric bursts avoid persistent potion spirals and per-stack particle spam. */
public final class StatusVisuals {
    private static long tick = Long.MIN_VALUE;
    private static int particles;
    private StatusVisuals() {}
    public static void accept(StatusVisualPayload payload) {
        var minecraft = Minecraft.getInstance(); var level = minecraft.level;
        if (level == null || !ClientConfigs.STATUS_VISUALS.get()) return;
        if (level.getGameTime() != tick) { tick = level.getGameTime(); particles = 0; }
        if (minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(payload.position()) > 48 * 48) return;
        int color = color(payload.damageType());
        var dust = new DustParticleOptions(new Vector3f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F), payload.burst() ? .75F : .48F);
        int count = payload.burst() ? 24 : payload.damageType() == DamageType.ELECTRICITY ? 9 : 6;
        double radius = Math.max(.16, payload.radius());
        for (int i = 0; i < count && particles < 256; i++, particles++) {
            double angle = Math.PI * 2 * i / count + level.random.nextDouble() * .25;
            Vec3 offset;
            if (payload.damageType() == DamageType.SLASH) offset = new Vec3((i - 2.5) * .08, .2 - i * .07, 0);
            else if (payload.damageType() == DamageType.ELECTRICITY) offset = new Vec3(Math.cos(angle) * radius * .55, (i % 3 - 1) * .16, Math.sin(angle) * radius * .55);
            else if (payload.damageType() == DamageType.GAS) {
                double spread = radius * Math.sqrt(level.random.nextDouble());
                offset = new Vec3(Math.cos(angle) * spread, -.5 + level.random.nextDouble() * .7, Math.sin(angle) * spread);
            }
            else if (payload.damageType() == DamageType.COLD && payload.burst()) offset = new Vec3(Math.cos(angle) * radius * .75, (i / 4D - 2.5) * .24, Math.sin(angle) * radius * .75);
            else if (payload.damageType() == DamageType.VIRAL) offset = new Vec3(Math.cos(angle) * radius * .5, (i - 2.5) * .14, Math.sin(angle) * radius * .5);
            else if (payload.damageType() == DamageType.CORROSIVE) offset = new Vec3(Math.cos(angle) * radius * .55, -.45 + level.random.nextDouble() * .25, Math.sin(angle) * radius * .55);
            else offset = new Vec3(Math.cos(angle) * radius * .65, (i % 2 == 0 ? .2 : -.2), Math.sin(angle) * radius * .65);
            var point = payload.position().add(offset);
            var motion = payload.damageType() == DamageType.COLD ? Vec3.ZERO : payload.burst() ? offset.normalize().scale(.07) : payload.damageType() == DamageType.HEAT ? new Vec3(0, .025, 0) : new Vec3(0, -.006, 0);
            level.addParticle(dust, point.x, point.y, point.z, motion.x, motion.y, motion.z);
        }
    }

    private static int color(DamageType type) {
        return switch (type) {
            case IMPACT -> 0xb7cbd2; case PUNCTURE -> 0xc6a86b; case SLASH -> 0xd35a62;
            case COLD -> 0x8ad9ee; case HEAT -> 0xf2a454; case ELECTRICITY -> 0x83b8ff; case TOXIN -> 0x8cb954;
            case BLAST -> 0xeac98a; case RADIATION -> 0xb4d979; case CORROSIVE -> 0xa4cc65;
            case VIRAL -> 0xd294cf; case GAS -> 0xc2ba79; case MAGNETIC -> 0xad9dee;
        };
    }
}
