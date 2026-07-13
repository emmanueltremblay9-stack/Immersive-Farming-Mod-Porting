package dev.emmanueltremblay.immersivefarming.client;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import dev.emmanueltremblay.immersivefarming.client.gui.ComposterScreen;
import dev.emmanueltremblay.immersivefarming.client.particle.SprinklerParticles;
import dev.emmanueltremblay.immersivefarming.client.render.SprinklerRenderer;
import dev.emmanueltremblay.immersivefarming.menu.IFMenus;
import dev.emmanueltremblay.immersivefarming.particle.IFParticles;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class IFClientEvents {
    private IFClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(IFClientEvents::registerParticleProviders);
        modEventBus.addListener(IFClientEvents::registerMenuScreens);
        modEventBus.addListener(IFClientEvents::registerRenderers);
        modEventBus.addListener(IFClientEvents::registerAdditionalModels);
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(IFParticles.SPRINKLER_PARTICLES.get(), SprinklerParticles.Provider::new);
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(IFMenus.COMPOSTER.get(), ComposterScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(IFBlockEntities.SPRINKLER.get(), SprinklerRenderer::new);
        event.registerBlockEntityRenderer(IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get(), SprinklerRenderer::new);
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(ImmersiveFarming.id("dynamic/sprinkler_top"), "standalone"));
        event.register(new ModelResourceLocation(ImmersiveFarming.id("dynamic/sprinkler_extended_top"), "standalone"));
    }
}
