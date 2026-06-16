package dev.emmanueltremblay.immersivefarming;

import com.mojang.logging.LogUtils;
import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.entity.IFEntityTypes;
import dev.emmanueltremblay.immersivefarming.event.FarmingEvents;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import dev.emmanueltremblay.immersivefarming.integration.IFIntegrations;
import dev.emmanueltremblay.immersivefarming.item.IFCreativeTabs;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import dev.emmanueltremblay.immersivefarming.menu.IFMenus;
import dev.emmanueltremblay.immersivefarming.recipe.IFRecipeSerializers;
import dev.emmanueltremblay.immersivefarming.recipe.IFRecipeTypes;
import dev.emmanueltremblay.immersivefarming.test.IFFarmingGameTests;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;

@Mod(ImmersiveFarming.MOD_ID)
public final class ImmersiveFarming {
    public static final String MOD_ID = "immersivefarming";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveFarming(IEventBus modEventBus, ModContainer modContainer) {
        IFFluids.FLUID_TYPES.register(modEventBus);
        IFFluids.FLUIDS.register(modEventBus);
        IFItems.ITEMS.register(modEventBus);
        IFBlocks.BLOCKS.register(modEventBus);
        IFBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        IFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        IFEntityTypes.ENTITY_TYPES.register(modEventBus);
        IFMenus.MENUS.register(modEventBus);
        IFRecipeTypes.RECIPE_TYPES.register(modEventBus);
        IFRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerGameTests);
        modContainer.registerConfig(ModConfig.Type.COMMON, IFConfig.SPEC);

        NeoForge.EVENT_BUS.register(new FarmingEvents());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(IFIntegrations::bootstrap);
        LOGGER.info("Loaded Immersive Farming unofficial modernization fork. IE present={}, Astikor Redux present={}",
                isModLoaded("immersiveengineering"), isAstikorReduxLoaded());
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                IFBlockEntities.SPRINKLER.get(),
                SprinklerBlockEntity::getFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get(),
                SprinklerBlockEntity::getFluidHandler
        );
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(IFFarmingGameTests.class);
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isAstikorReduxLoaded() {
        return isModLoaded("astikorcarts") || isModLoaded("astikorcartsredux") || isModLoaded("astikorredux");
    }
}
