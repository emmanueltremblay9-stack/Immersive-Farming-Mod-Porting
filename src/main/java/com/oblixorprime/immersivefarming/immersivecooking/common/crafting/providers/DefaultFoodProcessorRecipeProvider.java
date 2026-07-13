package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodProcessorRecipe;

import java.util.List;
import java.util.Optional;

public class DefaultFoodProcessorRecipeProvider implements IFluidContainingMultiblockRecipeProvider<FoodProcessorRecipe> {
    private RecipeHolder<FoodProcessorRecipe> lastRecipe;

    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public boolean hasMultiInput() {
        return true;
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(Container container, Level level) {
        return findMatchingRecipe(container, FluidStack.EMPTY, false, level);
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(ItemStack stack, Level level) {
        return findRecipe(singleItemContainer(stack), level);
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(Container container, FluidStack fluid, Level level) {
        return findMatchingRecipe(container, fluid, true, level);
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(ItemStack stack, FluidStack fluid, Level level) {
        return findRecipe(singleItemContainer(stack), fluid, level);
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(RecipeInput input, FluidStack fluid, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), fluid, level);
    }

    @Override
    public Optional<RecipeHolder<FoodProcessorRecipe>> findRecipe(RecipeInput input, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), level);
    }

    @Override
    public RecipeHolder<FoodProcessorRecipe> byKey(ResourceLocation id, Level level) {
        for (RecipeHolder<FoodProcessorRecipe> holder : getAllRecipes(level)) {
            if (holder.id().equals(id)) {
                lastRecipe = holder;
                return holder;
            }
        }
        return null;
    }

    @Override
    public List<RecipeHolder<FoodProcessorRecipe>> getAllRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ICRecipes.Types.FOOD_PROCESSOR.get());
    }

    private Optional<RecipeHolder<FoodProcessorRecipe>> findMatchingRecipe(
            Container container, FluidStack fluid, boolean allowFluidRecipes, Level level) {
        if (lastRecipe != null && matches(lastRecipe.value(), container, fluid, allowFluidRecipes, level)) {
            return Optional.of(lastRecipe);
        }

        for (RecipeHolder<FoodProcessorRecipe> holder : getAllRecipes(level)) {
            FoodProcessorRecipe recipe = holder.value();
            if (matches(recipe, container, fluid, allowFluidRecipes, level)) {
                lastRecipe = holder;
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    private boolean matches(FoodProcessorRecipe recipe, Container container, FluidStack fluid, boolean allowFluidRecipes, Level level) {
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

    private SimpleContainer singleItemContainer(ItemStack stack) {
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, stack);
        return container;
    }
}
