package dev.emmanueltremblay.immersivefarming.menu;

import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.entity.IndustrialComposterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class ComposterMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 10;
    private static final int INPUT_SLOT_COUNT = 1;

    @Nullable
    private final IndustrialComposterBlockEntity composter;
    @Nullable
    private final BlockPos composterPos;
    private final ContainerData data;

    public ComposterMenu(int containerId, Inventory playerInventory, IndustrialComposterBlockEntity composter) {
        this(containerId, playerInventory, composter, composter.getBlockPos(), composter.getItemHandlerForMenu(), composter.createMenuData());
    }

    public static ComposterMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new ComposterMenu(containerId, playerInventory, null, pos, new ItemStackHandler(INPUT_SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    private ComposterMenu(
            int containerId,
            Inventory playerInventory,
            @Nullable IndustrialComposterBlockEntity composter,
            @Nullable BlockPos composterPos,
            ItemStackHandler itemHandler,
            ContainerData data
    ) {
        super(IFMenus.COMPOSTER.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.composter = composter;
        this.composterPos = composterPos;
        this.data = data;

        addSlot(new SlotItemHandler(itemHandler, IndustrialComposterBlockEntity.INPUT_SLOT, 22, 15));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 86 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 144));
        }

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < INPUT_SLOT_COUNT) {
                if (!moveItemStackTo(stack, INPUT_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, INPUT_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (composter == null) {
            return true;
        }
        return composter.isFormedMaster()
                && composter.getLevel() != null
                && composter.getLevel().getBlockState(composter.getBlockPos()).is(IFBlocks.COMPOSTER.get())
                && player.distanceToSqr(
                composter.getBlockPos().getX() + 0.5D,
                composter.getBlockPos().getY() + 0.5D,
                composter.getBlockPos().getZ() + 0.5D
        ) <= 64.0D;
    }

    public int waterAmount() {
        return data.get(0);
    }

    public int wetMatterAmount() {
        return data.get(1);
    }

    public int dryMatterAmount() {
        return data.get(2);
    }

    public int energy() {
        return data.get(3);
    }

    public int energyCapacity() {
        return data.get(4);
    }

    public int processTime() {
        return data.get(5);
    }

    public int processTimeTotal() {
        return data.get(6);
    }

    public int waterCapacity() {
        return data.get(7);
    }

    public int wetMatterCapacity() {
        return data.get(8);
    }

    public int dryMatterCapacity() {
        return data.get(9);
    }

    @Nullable
    public BlockPos composterPos() {
        return composterPos;
    }
}
