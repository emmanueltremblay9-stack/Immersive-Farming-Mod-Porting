package com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.IFluidContainingMultiblockRecipeProvider;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.IMultiblockRecipeProvider;

import java.util.*;

public abstract class ICMultiblockLogic<S extends IMultiblockState, R extends Recipe<?>>
        implements IMultiblockLogic<S> {
    protected final List<IMultiblockRecipeProvider<R>> recipeProviders = new ArrayList<>();

    public Optional<RecipeHolder<R>> findRecipeHolder(RecipeInput input, FluidStack fluid, Level level) {
        for (var normalProvider : recipeProviders) {
            if (normalProvider.canProvide()
                    && normalProvider instanceof IFluidContainingMultiblockRecipeProvider<R> provider) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(input, fluid, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(RecipeInput input, FluidStack fluid, Level level) {
        return findRecipeHolder(input, fluid, level).map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<R>> findRecipeHolder(ItemStack stack, FluidStack fluid, Level level) {
        for (var normalProvider : recipeProviders) {
            if (normalProvider.canProvide()
                    && normalProvider instanceof IFluidContainingMultiblockRecipeProvider<R> provider) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(stack, fluid, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(ItemStack stack, FluidStack fluid, Level level) {
        return findRecipeHolder(stack, fluid, level).map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<R>> findRecipeHolder(Container container, FluidStack fluid, Level level) {
        for (var normalProvider : recipeProviders) {
            if (normalProvider.canProvide()
                    && normalProvider instanceof IFluidContainingMultiblockRecipeProvider<R> provider) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(container, fluid, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(Container container, FluidStack fluid, Level level) {
        return findRecipeHolder(container, fluid, level).map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<R>> findRecipeHolder(RecipeInput input, Level level) {
        for (var provider : recipeProviders) {
            if (provider.canProvide()) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(input, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(RecipeInput input, Level level) {
        return findRecipeHolder(input, level).map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<R>> findRecipeHolder(ItemStack stack, Level level) {
        for (var provider : recipeProviders) {
            if (provider.canProvide()) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(stack, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(ItemStack stack, Level level) {
        return findRecipeHolder(stack, level).map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<R>> findRecipeHolder(Container container, Level level) {
        for (var provider : recipeProviders) {
            if (provider.canProvide() && provider.hasMultiInput()) {
                Optional<RecipeHolder<R>> recipe = provider.findRecipe(container, level);
                if (recipe.isPresent()) {
                    return recipe;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<R> findRecipe(Container container, Level level) {
        return findRecipeHolder(container, level).map(RecipeHolder::value);
    }

    public R byKey(ResourceLocation id, Level level) {
        for (var provider : recipeProviders) {
            if (provider.canProvide()) {
                RecipeHolder<R> recipe = provider.byKey(id, level);
                if (recipe != null) {
                    return recipe.value();
                }
            }
        }
        return null;
    }

    public List<R> getAllProvidedRecipes(Level level) {
        List<R> recipes = new ArrayList<>();
        for (var provider : recipeProviders) {
            if (provider.canProvide()) {
                recipes.addAll(provider.getAllRecipes(level).stream().map(RecipeHolder::value).toList());
            }
        }
        return recipes;
    }

    public ItemStack getRecipeResult(R recipe, Level level) {
        return recipe.getResultItem(level.registryAccess());
    }
}
