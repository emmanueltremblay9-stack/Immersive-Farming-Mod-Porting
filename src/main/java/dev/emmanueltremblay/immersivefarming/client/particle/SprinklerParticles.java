package dev.emmanueltremblay.immersivefarming.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SprinklerParticles extends TextureSheetParticle {
    protected SprinklerParticles(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        friction = 0.9F;
        xd = xSpeed;
        yd = ySpeed;
        zd = zSpeed;
        quadSize *= 0.85F;
        lifetime = 100;
        gravity = 2.0F;
        hasPhysics = true;
        setSpriteFromAge(sprites);
        rCol = 1.0F;
        gCol = 1.0F;
        bCol = 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        alpha = 1.0F - age / (float) lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SprinklerParticles(level, x, y, z, sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}
