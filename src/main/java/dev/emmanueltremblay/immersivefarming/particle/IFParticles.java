package dev.emmanueltremblay.immersivefarming.particle;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPRINKLER_PARTICLES =
            PARTICLE_TYPES.register("sprinkler_particles", () -> new SimpleParticleType(true));

    private IFParticles() {
    }
}
