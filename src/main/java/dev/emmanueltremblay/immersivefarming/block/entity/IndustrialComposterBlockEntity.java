package dev.emmanueltremblay.immersivefarming.block.entity;

import dev.emmanueltremblay.immersivefarming.block.IndustrialComposterBlock;
import dev.emmanueltremblay.immersivefarming.block.ComposterMultiblock;
import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import dev.emmanueltremblay.immersivefarming.recipe.ComposterRecipe;
import dev.emmanueltremblay.immersivefarming.recipe.ComposterRecipeInput;
import dev.emmanueltremblay.immersivefarming.recipe.IFRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IndustrialComposterBlockEntity extends BlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int TANK_CAPACITY = 8_000;
    public static final int ENERGY_CAPACITY = 16_000;
    public static final int ENERGY_TRANSFER = 256;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final DirtyFluidTank waterTank = new DirtyFluidTank(TANK_CAPACITY, stack -> stack.is(Fluids.WATER));
    private final DirtyFluidTank wetMatterTank = new DirtyFluidTank(TANK_CAPACITY, stack -> stack.is(IFFluids.WET_MATTER.get()));
    private final DirtyFluidTank dryMatterTank = new DirtyFluidTank(TANK_CAPACITY, stack -> stack.is(IFFluids.DRY_MATTER.get()));
    private final FluidTank[] tanks = new FluidTank[]{waterTank, wetMatterTank, dryMatterTank};
    private final IFluidHandler fluidHandler = new CombinedTankHandler();
    private final DirtyEnergyStorage energyStorage = new DirtyEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER);

    @Nullable
    private ResourceLocation activeRecipe;
    private int processTime;
    private int processTimeTotal;

    public IndustrialComposterBlockEntity(BlockPos pos, BlockState blockState) {
        super(IFBlockEntities.COMPOSTER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IndustrialComposterBlockEntity composter) {
        if (!composter.isFormedMaster()) {
            return;
        }
        composter.tickServer();
    }

    @Nullable
    public IItemHandler getItemHandler(Direction side) {
        IndustrialComposterBlockEntity master = resolveFormedMaster();
        return master == null ? null : master.itemHandler;
    }

    @Nullable
    public IFluidHandler getFluidHandler(Direction side) {
        IndustrialComposterBlockEntity master = resolveFormedMaster();
        return master == null ? null : master.fluidHandler;
    }

    @Nullable
    public IEnergyStorage getEnergyStorage(Direction side) {
        IndustrialComposterBlockEntity master = resolveFormedMaster();
        return master == null ? null : master.energyStorage;
    }

    public ItemStackHandler getItemHandlerForMenu() {
        return itemHandler;
    }

    public int getTankCapacityForMenu(int tank) {
        return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
    }

    public int getEnergyCapacityForMenu() {
        return energyStorage.getMaxEnergyStored();
    }

    public ContainerData createMenuData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> waterTank.getFluidAmount();
                    case 1 -> wetMatterTank.getFluidAmount();
                    case 2 -> dryMatterTank.getFluidAmount();
                    case 3 -> energyStorage.getEnergyStored();
                    case 4 -> energyStorage.getMaxEnergyStored();
                    case 5 -> processTime;
                    case 6 -> processTimeTotal;
                    case 7 -> waterTank.getCapacity();
                    case 8 -> wetMatterTank.getCapacity();
                    case 9 -> dryMatterTank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Server-side menu data is read-only. Client menus use SimpleContainerData.
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
    }

    public int getTankAmountForTest(int tank) {
        return tank >= 0 && tank < tanks.length ? tanks[tank].getFluidAmount() : 0;
    }

    public int getEnergyForTest() {
        return energyStorage.getEnergyStored();
    }

    public int getProcessTimeForTest() {
        return processTime;
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Containers.dropItemStack(
                    level,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    stack.copy());
            itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public boolean isFormedMaster() {
        BlockState state = getBlockState();
        return state.hasProperty(IndustrialComposterBlock.FORMED)
                && state.getValue(IndustrialComposterBlock.FORMED)
                && !state.getValue(IndustrialComposterBlock.SLAVE);
    }

    @Nullable
    public IndustrialComposterBlockEntity resolveFormedMaster() {
        if (isFormedMaster()) {
            return this;
        }
        if (level == null) {
            return null;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(IndustrialComposterBlock.FORMED)
                || !state.getValue(IndustrialComposterBlock.FORMED)) {
            return null;
        }
        BlockPos masterPos = ComposterMultiblock.masterPosFromPart(worldPosition, state);
        if (masterPos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(masterPos);
        if (blockEntity instanceof IndustrialComposterBlockEntity composter && composter.isFormedMaster()) {
            return composter;
        }
        return null;
    }

    private void tickServer() {
        if (level == null || level.isClientSide()) {
            return;
        }

        RecipeHolder<ComposterRecipe> recipeHolder = activeRecipe == null ? null : recipeById(activeRecipe).orElse(null);
        ComposterRecipe recipe = recipeHolder == null ? null : recipeHolder.value();
        if (recipeHolder == null) {
            recipeHolder = findRecipe().orElse(null);
            recipe = recipeHolder == null ? null : recipeHolder.value();
            if (recipeHolder == null || recipe == null || !canProcess(recipe)
                    || energyStorage.consumeEnergy(recipe.energyPerTick(), true) < recipe.energyPerTick()) {
                resetProcess();
                return;
            }
            activeRecipe = recipeHolder.id();
            processTime = 0;
            processTimeTotal = recipe.processTime();
        }

        if (!recipe.matches(input(), level) || !canProcess(recipe)) {
            resetProcess();
            return;
        }

        int energyPerTick = recipe.energyPerTick();
        if (energyStorage.consumeEnergy(energyPerTick, true) < energyPerTick) {
            return;
        }

        energyStorage.consumeEnergy(energyPerTick, false);
        processTime++;
        setChanged();

        if (processTime >= processTimeTotal) {
            finishRecipe(recipe);
            resetProcess();
        }
    }

    private Optional<RecipeHolder<ComposterRecipe>> findRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        ComposterRecipeInput input = input();
        Optional<RecipeHolder<ComposterRecipe>> fluidProduct = level.getRecipeManager().getAllRecipesFor(IFRecipeTypes.COMPOSTER.get())
                .stream()
                .filter(holder -> holder.value().isFluidProduct())
                .filter(holder -> holder.value().matches(input, level))
                .findFirst();
        return fluidProduct.or(() -> level.getRecipeManager().getAllRecipesFor(IFRecipeTypes.COMPOSTER.get())
                .stream()
                .filter(holder -> !holder.value().isFluidProduct())
                .filter(holder -> holder.value().matches(input, level))
                .findFirst());
    }

    private Optional<RecipeHolder<ComposterRecipe>> recipeById(ResourceLocation id) {
        if (level == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().getAllRecipesFor(IFRecipeTypes.COMPOSTER.get())
                .stream()
                .filter(holder -> holder.id().equals(id))
                .findFirst();
    }

    private ComposterRecipeInput input() {
        return new ComposterRecipeInput(
                itemHandler.getStackInSlot(INPUT_SLOT),
                waterTank.getFluid(),
                wetMatterTank.getFluid(),
                dryMatterTank.getFluid()
        );
    }

    private boolean canProcess(ComposterRecipe recipe) {
        if (recipe.isFluidProduct()) {
            boolean canStoreWet = recipe.wetMatterOutput().isEmpty()
                    || wetMatterTank.fill(recipe.wetMatterOutput(), IFluidHandler.FluidAction.SIMULATE) == recipe.wetMatterOutput().getAmount();
            boolean canStoreDry = recipe.dryMatterOutput().isEmpty()
                    || dryMatterTank.fill(recipe.dryMatterOutput(), IFluidHandler.FluidAction.SIMULATE) == recipe.dryMatterOutput().getAmount();
            return canStoreWet && canStoreDry;
        }
        return recipe.waterInput().isPresent()
                && recipe.wetMatterInput().isPresent()
                && recipe.dryMatterInput().isPresent()
                && recipe.itemOutput() != null
                && !recipe.itemOutput().isEmpty();
    }

    private void finishRecipe(ComposterRecipe recipe) {
        if (level == null) {
            return;
        }
        if (recipe.isFluidProduct()) {
            itemHandler.extractItem(INPUT_SLOT, 1, false);
            if (!recipe.wetMatterOutput().isEmpty()) {
                wetMatterTank.fill(recipe.wetMatterOutput(), IFluidHandler.FluidAction.EXECUTE);
            }
            if (!recipe.dryMatterOutput().isEmpty()) {
                dryMatterTank.fill(recipe.dryMatterOutput(), IFluidHandler.FluidAction.EXECUTE);
            }
        } else {
            recipe.waterInput().ifPresent(input -> waterTank.drain(input.amount(), IFluidHandler.FluidAction.EXECUTE));
            recipe.wetMatterInput().ifPresent(input -> wetMatterTank.drain(input.amount(), IFluidHandler.FluidAction.EXECUTE));
            recipe.dryMatterInput().ifPresent(input -> dryMatterTank.drain(input.amount(), IFluidHandler.FluidAction.EXECUTE));
            BlockPos origin = worldPosition.subtract(ComposterMultiblock.MASTER_OFFSET);
            Containers.dropItemStack(level,
                    origin.getX() + ComposterMultiblock.WIDTH / 2.0D,
                    origin.getY() + ComposterMultiblock.HEIGHT + 0.1D,
                    origin.getZ() + ComposterMultiblock.DEPTH / 2.0D,
                    recipe.itemOutput().copy());
        }
        setChanged();
    }

    private void resetProcess() {
        if (activeRecipe != null || processTime != 0 || processTimeTotal != 0) {
            activeRecipe = null;
            processTime = 0;
            processTimeTotal = 0;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", itemHandler.serializeNBT(registries));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("wetMatterTank", wetMatterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("dryMatterTank", dryMatterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("energy", energyStorage.serializeNBT(registries));
        if (activeRecipe != null) {
            tag.putString("activeRecipe", activeRecipe.toString());
        }
        tag.putInt("processTime", processTime);
        tag.putInt("processTimeTotal", processTimeTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("items"));
        waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        wetMatterTank.readFromNBT(registries, tag.getCompound("wetMatterTank"));
        dryMatterTank.readFromNBT(registries, tag.getCompound("dryMatterTank"));
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
        activeRecipe = tag.contains("activeRecipe") ? ResourceLocation.tryParse(tag.getString("activeRecipe")) : null;
        processTime = tag.getInt("processTime");
        processTimeTotal = tag.getInt("processTimeTotal");
    }

    private final class DirtyFluidTank extends FluidTank {
        private DirtyFluidTank(int capacity, java.util.function.Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    }

    private final class DirtyEnergyStorage extends EnergyStorage {
        private DirtyEnergyStorage(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        private int consumeEnergy(int toExtract, boolean simulate) {
            if (toExtract <= 0 || energy < toExtract) {
                return 0;
            }
            if (!simulate) {
                energy -= toExtract;
                setChanged();
            }
            return toExtract;
        }
    }

    private final class CombinedTankHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank >= 0 && tank < tanks.length && tanks[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            for (FluidTank tank : tanks) {
                int filled = tank.fill(resource, action);
                if (filled > 0) {
                    return filled;
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            for (FluidTank tank : tanks) {
                FluidStack drained = tank.drain(resource, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank tank : tanks) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
