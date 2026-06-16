package dev.emmanueltremblay.immersivefarming.util;

import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.SprinklerBlock;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class FarmingLogic {
    public static final int NO_IRRIGATION = 0;
    public static final int WATER_IRRIGATION = 1;
    public static final int TREATED_IRRIGATION = 2;

    private FarmingLogic() {
    }

    public static int irrigationAt(LevelReader level, BlockPos pos) {
        int fromSprinklers = sprinklerIrrigationAt(level, pos);
        int fromFluids = fluidIrrigationAt(level, pos);
        return Math.max(fromSprinklers, fromFluids);
    }

    private static int fluidIrrigationAt(LevelReader level, BlockPos pos) {
        boolean waterFound = false;
        for (BlockPos waterPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
            FluidState fluid = level.getFluidState(waterPos);
            if (fluid.is(IFFluids.TREATED_WATER.get()) || fluid.is(IFFluids.TREATED_WATER_FLOWING.get())) {
                return TREATED_IRRIGATION;
            }
            if (fluid.is(FluidTags.WATER)) {
                waterFound = true;
            }
        }
        return waterFound ? WATER_IRRIGATION : NO_IRRIGATION;
    }

    private static int sprinklerIrrigationAt(LevelReader level, BlockPos pos) {
        int maxRadius = Math.max(IFConfig.sprinklerRadius, IFConfig.highPressureSprinklerRadius);
        boolean waterFound = false;
        for (BlockPos candidate : BlockPos.betweenClosed(
                pos.offset(-maxRadius, -3, -maxRadius),
                pos.offset(maxRadius, 3, maxRadius))) {
            BlockState state = level.getBlockState(candidate);
            if (!state.hasProperty(SprinklerBlock.ACTIVE) || !state.getValue(SprinklerBlock.ACTIVE)) {
                continue;
            }
            int radius;
            if (state.is(IFBlocks.SPRINKLER.get())) {
                radius = IFConfig.sprinklerRadius;
            } else if (state.is(IFBlocks.HIGH_PRESSURE_SPRINKLER.get())) {
                radius = IFConfig.highPressureSprinklerRadius;
            } else {
                continue;
            }
            if (chebyshevDistance2d(pos, candidate) <= radius && Math.abs(pos.getY() - candidate.getY()) <= 3) {
                if (state.getValue(SprinklerBlock.USING_TREATED_WATER)) {
                    return TREATED_IRRIGATION;
                }
                waterFound = true;
            }
        }
        return waterFound ? WATER_IRRIGATION : NO_IRRIGATION;
    }

    public static int chebyshevDistance2d(BlockPos first, BlockPos second) {
        return Math.max(Math.abs(first.getX() - second.getX()), Math.abs(first.getZ() - second.getZ()));
    }
}
