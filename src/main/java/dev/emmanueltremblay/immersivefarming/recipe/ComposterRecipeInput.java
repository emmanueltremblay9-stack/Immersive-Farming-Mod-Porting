package dev.emmanueltremblay.immersivefarming.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record ComposterRecipeInput(ItemStack item, FluidStack water, FluidStack wetMatter, FluidStack dryMatter) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty() && water.isEmpty() && wetMatter.isEmpty() && dryMatter.isEmpty();
    }

    public FluidStack fluid(int tank) {
        return switch (tank) {
            case 0 -> water;
            case 1 -> wetMatter;
            case 2 -> dryMatter;
            default -> FluidStack.EMPTY;
        };
    }
}
