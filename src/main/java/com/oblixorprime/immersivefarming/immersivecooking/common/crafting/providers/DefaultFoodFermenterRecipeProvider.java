package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodFermenterRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultFoodFermenterRecipeProvider implements IFluidContainingMultiblockRecipeProvider<FoodFermenterRecipe> {
    private RecipeHolder<FoodFermenterRecipe> lastRecipe;

    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public boolean hasMultiInput() {
        return true;
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(Container container, Level level) {
        return findMatchingRecipe(container, FluidStack.EMPTY, false, level);
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(ItemStack stack, Level level) {
        if (lastRecipe != null && lastRecipe.value().container.getItem() == stack.getItem()
                && lastRecipe.value().fluidInput == null) {
            return Optional.of(lastRecipe);
        }
        for (RecipeHolder<FoodFermenterRecipe> holder : getAllRecipes(level)) {
            FoodFermenterRecipe recipe = holder.value();
            // Check if the stack matches the container item (simple heuristic)
            if (recipe.container.getItem() == stack.getItem() && recipe.fluidInput == null) {
                lastRecipe = holder;
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(Container container, FluidStack fluid, Level level) {
        return findMatchingRecipe(container, fluid, true, level);
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(ItemStack stack, FluidStack fluid, Level level) {
        if (lastRecipe != null && lastRecipe.value().container.getItem() == stack.getItem()
                && lastRecipe.value().fluidInput != null) {
            if (lastRecipe.value().fluidInput.test(fluid)
                    && fluid.getAmount() >= lastRecipe.value().fluidInput.amount()) {
                return Optional.of(lastRecipe);
            }
        }
        for (RecipeHolder<FoodFermenterRecipe> holder : getAllRecipes(level)) {
            FoodFermenterRecipe recipe = holder.value();
            if (recipe.container.getItem() == stack.getItem() && recipe.fluidInput != null) {
                if (recipe.fluidInput.test(fluid)
                        && fluid.getAmount() >= recipe.fluidInput.amount()) {
                    lastRecipe = holder;
                    return Optional.of(holder);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(RecipeInput input, FluidStack fluid, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), fluid, level);
    }

    @Override
    public Optional<RecipeHolder<FoodFermenterRecipe>> findRecipe(RecipeInput input, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), level);
    }

    @Override
    public RecipeHolder<FoodFermenterRecipe> byKey(ResourceLocation id, Level level) {
        for (RecipeHolder<FoodFermenterRecipe> holder : getAllRecipes(level)) {
            if (holder.id().equals(id)) {
                lastRecipe = holder;
                return holder;
            }
        }
        return null;
    }

    @Override
    public List<RecipeHolder<FoodFermenterRecipe>> getAllRecipes(Level level) {
        return new ArrayList<>(FoodFermenterRecipe.RECIPES.getRecipes(level));
    }

    private Optional<RecipeHolder<FoodFermenterRecipe>> findMatchingRecipe(
            Container container, FluidStack fluid, boolean allowFluidRecipes, Level level) {
        if (lastRecipe != null && matches(lastRecipe.value(), container, fluid, allowFluidRecipes, level)) {
            return Optional.of(lastRecipe);
        }

        for (RecipeHolder<FoodFermenterRecipe> holder : getAllRecipes(level)) {
            FoodFermenterRecipe recipe = holder.value();
            if (matches(recipe, container, fluid, allowFluidRecipes, level)) {
                lastRecipe = holder;
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    private boolean matches(FoodFermenterRecipe recipe, Container container, FluidStack fluid,
            boolean allowFluidRecipes, Level level) {
        if (!recipe.matches(container, level)) {
            return false;
        }
        if (recipe.fluidInput == null) {
            return true;
        }
        return allowFluidRecipes
                && recipe.fluidInput.test(fluid)
                && fluid.getAmount() >= recipe.fluidInput.amount();
    }
}
