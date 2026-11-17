package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class StatusParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, "yagens_attributes");
    public static final Supplier<SimpleParticleType> BLOOD = REGISTER.register("status_blood", () -> new SimpleParticleType(false));
    private StatusParticles() {}
}
