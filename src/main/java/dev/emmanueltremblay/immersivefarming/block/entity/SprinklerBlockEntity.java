package dev.emmanueltremblay.immersivefarming.block.entity;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.SprinklerBlock;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import dev.emmanueltremblay.immersivefarming.particle.IFParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SprinklerBlockEntity extends BlockEntity implements IConfigurableSides {
    public static final int CAPACITY = 1000;
    private static final String SIDE_CONFIG_TAG = "sideConfig";
    private int fluidAmount;
    private boolean treatedWater;
    private float sprinklerRotation;
    private final IFluidHandler fluidHandler = new TankHandler();
    private final IFluidHandler inputFluidHandler = new SidedTankHandler(true, false);
    private final IFluidHandler outputFluidHandler = new SidedTankHandler(false, true);
    private final Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(state.is(IFBlocks.HIGH_PRESSURE_SPRINKLER.get())
                ? IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get()
                : IFBlockEntities.SPRINKLER.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction, IOSideConfig.INPUT);
        }
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
        sprinkler.syncPartState(level, pos, state, active, active && sprinkler.treatedWater);
        sprinkler.setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler) {
        if (!state.hasProperty(SprinklerBlock.ACTIVE) || !state.getValue(SprinklerBlock.ACTIVE)) {
            return;
        }
        sprinkler.sprinklerRotation = (sprinkler.sprinklerRotation + (sprinkler.isHighPressure() ? 1.0F : 18.0F)) % 360.0F;
        if (level.getGameTime() % 5L == 0L) {
            sprinkler.spawnSprinklerParticles(level, pos, state);
        }
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return fluidHandler;
        }
        return switch (getSideConfig(side)) {
            case INPUT -> inputFluidHandler;
            case OUTPUT -> outputFluidHandler;
            case NONE -> null;
        };
    }

    @Override
    public IOSideConfig getSideConfig(Direction side) {
        return sideConfig.getOrDefault(side, IOSideConfig.NONE);
    }

    @Override
    public boolean toggleSide(Direction side, Player player) {
        if (level == null) {
            return false;
        }
        IOSideConfig nextConfig = IOSideConfig.next(getSideConfig(side));
        sideConfig.put(side, nextConfig);
        setChanged();
        level.invalidateCapabilities(worldPosition);
        if (level.isClientSide()) {
            requestModelDataUpdate();
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.immersive_farming_mod_porting.sprinkler_side_config",
                        side.getName(), nextConfig.getTextComponent()), true);
            }
        }
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public boolean isHighPressure() {
        return getBlockState().is(IFBlocks.HIGH_PRESSURE_SPRINKLER.get());
    }

    public void fillForTest(boolean treated, int amount) {
        this.treatedWater = treated;
        this.fluidAmount = Math.min(CAPACITY, Math.max(0, amount));
        setChanged();
    }

    public float getSprinklerRotation(float partialTicks) {
        if (getBlockState().getValue(SprinklerBlock.ACTIVE)) {
            return (sprinklerRotation + (isHighPressure() ? 1.0F : 18.0F) * partialTicks) % 360.0F;
        }
        return sprinklerRotation;
    }

    private void syncPartState(Level level, BlockPos pos, BlockState state, boolean active, boolean usingTreatedWater) {
        BlockState newState = state
                .setValue(SprinklerBlock.ACTIVE, active)
                .setValue(SprinklerBlock.USING_TREATED_WATER, usingTreatedWater)
                .setValue(SprinklerBlock.SLAVE, false);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
        }

        BlockPos topPos = pos.above();
        BlockState topState = level.getBlockState(topPos);
        if (topState.is(state.getBlock()) && topState.getValue(SprinklerBlock.SLAVE)) {
            BlockState newTopState = topState
                    .setValue(SprinklerBlock.ACTIVE, active)
                    .setValue(SprinklerBlock.USING_TREATED_WATER, usingTreatedWater);
            if (!newTopState.equals(topState)) {
                level.setBlock(topPos, newTopState, 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fluidAmount", fluidAmount);
        tag.putBoolean("treatedWater", treatedWater);
        int[] serializedSides = new int[Direction.values().length];
        for (Direction direction : Direction.values()) {
            serializedSides[direction.ordinal()] = getSideConfig(direction).ordinal();
        }
        tag.putIntArray(SIDE_CONFIG_TAG, serializedSides);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidAmount = Math.min(CAPACITY, Math.max(0, tag.getInt("fluidAmount")));
        treatedWater = fluidAmount > 0 && tag.getBoolean("treatedWater");
        if (tag.contains(SIDE_CONFIG_TAG)) {
            int[] serializedSides = tag.getIntArray(SIDE_CONFIG_TAG);
            for (Direction direction : Direction.values()) {
                int index = direction.ordinal();
                if (index < serializedSides.length && serializedSides[index] >= 0
                        && serializedSides[index] < IOSideConfig.VALUES.length) {
                    sideConfig.put(direction, IOSideConfig.VALUES[serializedSides[index]]);
                }
            }
        }
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private boolean accepts(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == IFFluids.TREATED_WATER.get();
    }

    private Fluid storedFluid() {
        return treatedWater ? IFFluids.TREATED_WATER.get() : Fluids.WATER;
    }

    private void spawnSprinklerParticles(Level level, BlockPos pos, BlockState state) {
        double angle = -2.0D * Math.PI * sprinklerRotation / 360.0D;
        boolean highPressure = isHighPressure();
        int count = highPressure ? 24 : 8;
        double baseSpeed = highPressure ? 0.65D : 0.35D;
        double spread = highPressure ? 0.60D : 0.25D;
        double ySpeed = highPressure ? 0.45D : 0.20D;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.4D;
        double z = pos.getZ() + 0.5D;
        for (int i = 0; i < count; i++) {
            double velocity = baseSpeed + spread * level.random.nextDouble();
            double sideJitter = 0.12D * (2.0D * level.random.nextDouble() - 1.0D);
            level.addParticle(IFParticles.SPRINKLER_PARTICLES.get(),
                    x, y, z,
                    (Math.cos(angle) + sideJitter) * velocity,
                    (ySpeed + 0.2D * level.random.nextDouble()) * velocity,
                    (Math.sin(angle) + sideJitter) * velocity);
        }
        if (state.getValue(SprinklerBlock.USING_TREATED_WATER)) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        x + (level.random.nextDouble() - 0.5D),
                        y - 0.25D + level.random.nextDouble() * 0.5D,
                        z + (level.random.nextDouble() - 0.5D),
                        0.0D, 0.02D, 0.0D);
            }
        }
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

    private final class SidedTankHandler implements IFluidHandler {
        private final boolean allowFill;
        private final boolean allowDrain;

        private SidedTankHandler(boolean allowFill, boolean allowDrain) {
            this.allowFill = allowFill;
            this.allowDrain = allowDrain;
        }

        @Override
        public int getTanks() {
            return fluidHandler.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return fluidHandler.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return fluidHandler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return allowFill && fluidHandler.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return allowFill ? fluidHandler.fill(resource, action) : 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return allowDrain ? fluidHandler.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return allowDrain ? fluidHandler.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }
}
