package dev.emmanueltremblay.immersivefarming.fluid;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

public final class IFFluids {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ImmersiveFarming.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> TREATED_WATER_TYPE = FLUID_TYPES.register(
            "treated_water",
            () -> makeWaterTexturedType(FluidType.Properties.create()
                    .descriptionId("block.immersive_farming_mod_porting.treated_water")
                    .fallDistanceModifier(0F)
                    .canExtinguish(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
    );
    public static final DeferredHolder<FluidType, FluidType> WET_MATTER_TYPE = FLUID_TYPES.register(
            "wet_matter",
            () -> makeWaterTexturedType(FluidType.Properties.create()
                    .descriptionId("fluid.immersive_farming_mod_porting.wet_matter_fluid")
                    .density(1200)
                    .viscosity(1600)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
    );
    public static final DeferredHolder<FluidType, FluidType> DRY_MATTER_TYPE = FLUID_TYPES.register(
            "dry_matter",
            () -> makeWaterTexturedType(FluidType.Properties.create()
                    .descriptionId("fluid.immersive_farming_mod_porting.dry_matter_fluid")
                    .density(1400)
                    .viscosity(2200)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
    );

    public static final DeferredHolder<Fluid, FlowingFluid> TREATED_WATER = FLUIDS.register(
            "treated_water_fluid",
            () -> new BaseFlowingFluid.Source(treatedWaterProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> TREATED_WATER_FLOWING = FLUIDS.register(
            "treated_water_flowing",
            () -> new BaseFlowingFluid.Flowing(treatedWaterProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> WET_MATTER = FLUIDS.register(
            "wet_matter_fluid",
            () -> new BaseFlowingFluid.Source(wetMatterProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> WET_MATTER_FLOWING = FLUIDS.register(
            "wet_matter_flowing",
            () -> new BaseFlowingFluid.Flowing(wetMatterProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> DRY_MATTER = FLUIDS.register(
            "dry_matter_fluid",
            () -> new BaseFlowingFluid.Source(dryMatterProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> DRY_MATTER_FLOWING = FLUIDS.register(
            "dry_matter_flowing",
            () -> new BaseFlowingFluid.Flowing(dryMatterProperties())
    );

    private IFFluids() {
    }

    private static FluidType makeWaterTexturedType(FluidType.Properties properties) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return WATER_STILL;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return WATER_FLOW;
                    }
                });
            }
        };
    }

    private static BaseFlowingFluid.Properties treatedWaterProperties() {
        return new BaseFlowingFluid.Properties(TREATED_WATER_TYPE, TREATED_WATER, TREATED_WATER_FLOWING)
                .block(IFBlocks.TREATED_WATER)
                .bucket(IFItems.TREATED_WATER_BUCKET);
    }

    private static BaseFlowingFluid.Properties wetMatterProperties() {
        return new BaseFlowingFluid.Properties(WET_MATTER_TYPE, WET_MATTER, WET_MATTER_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }

    private static BaseFlowingFluid.Properties dryMatterProperties() {
        return new BaseFlowingFluid.Properties(DRY_MATTER_TYPE, DRY_MATTER, DRY_MATTER_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }
}
