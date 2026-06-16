package dev.emmanueltremblay.immersivefarming.block.entity;

import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.SprinklerBlock;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class SprinklerBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1000;
    private int fluidAmount;
    private boolean treatedWater;
    private final IFluidHandler fluidHandler = new TankHandler();

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(state.is(IFBlocks.HIGH_PRESSURE_SPRINKLER.get())
                ? IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get()
                : IFBlockEntities.SPRINKLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        int consumption = sprinkler.isHighPressure() ? 3 : 1;
        boolean active = sprinkler.fluidAmount >= consumption;
        if (active) {
            sprinkler.fluidAmount -= consumption;
        }
        BlockState newState = state
                .setValue(SprinklerBlock.ACTIVE, active)
                .setValue(SprinklerBlock.USING_TREATED_WATER, active && sprinkler.treatedWater);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
        }
        sprinkler.setChanged();
    }

    public IFluidHandler getFluidHandler(net.minecraft.core.Direction side) {
        return fluidHandler;
    }

    public boolean isHighPressure() {
        return getBlockState().is(IFBlocks.HIGH_PRESSURE_SPRINKLER.get());
    }

    public void fillForTest(boolean treated, int amount) {
        this.treatedWater = treated;
        this.fluidAmount = Math.min(CAPACITY, Math.max(0, amount));
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fluidAmount", fluidAmount);
        tag.putBoolean("treatedWater", treatedWater);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidAmount = Math.min(CAPACITY, Math.max(0, tag.getInt("fluidAmount")));
        treatedWater = fluidAmount > 0 && tag.getBoolean("treatedWater");
    }

    private boolean accepts(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == IFFluids.TREATED_WATER.get();
    }

    private Fluid storedFluid() {
        return treatedWater ? IFFluids.TREATED_WATER.get() : Fluids.WATER;
    }

    private final class TankHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank != 0 || fluidAmount <= 0) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(storedFluid(), fluidAmount);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && accepts(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !accepts(resource.getFluid())) {
                return 0;
            }
            boolean incomingTreated = resource.getFluid() == IFFluids.TREATED_WATER.get();
            if (fluidAmount > 0 && treatedWater != incomingTreated) {
                return 0;
            }
            int filled = Math.min(CAPACITY - fluidAmount, resource.getAmount());
            if (filled > 0 && action.execute()) {
                treatedWater = incomingTreated;
                fluidAmount += filled;
                setChanged();
            }
            return filled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != storedFluid()) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            int drained = Math.min(maxDrain, fluidAmount);
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack stack = new FluidStack(storedFluid(), drained);
            if (action.execute()) {
                fluidAmount -= drained;
                if (fluidAmount <= 0) {
                    treatedWater = false;
                }
                setChanged();
            }
            return stack;
        }
    }
}
