package dev.emmanueltremblay.immersivefarming.fluid;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class IFFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ImmersiveFarming.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> TREATED_WATER_TYPE = FLUID_TYPES.register(
            "treated_water",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.immersivefarming.treated_water")
                    .fallDistanceModifier(0F)
                    .canExtinguish(true)
                    .supportsBoating(true)
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

    private IFFluids() {
    }

    private static BaseFlowingFluid.Properties treatedWaterProperties() {
        return new BaseFlowingFluid.Properties(TREATED_WATER_TYPE, TREATED_WATER, TREATED_WATER_FLOWING)
                .block(IFBlocks.TREATED_WATER)
                .bucket(IFItems.TREATED_WATER_BUCKET);
    }
}
