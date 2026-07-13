package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SmokingRecipeProvider implements IMultiblockRecipeProvider<SmokingRecipe> {
    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public boolean hasMultiInput() {
        return false;
    }

    @Override
    public Optional<RecipeHolder<SmokingRecipe>> findRecipe(RecipeInput input, Level level) {
        return input.size() > 0 ? findRecipe(input.getItem(0), level) : Optional.empty();
    }

    @Override
    public Optional<RecipeHolder<SmokingRecipe>> findRecipe(Container container, Level level) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            Optional<RecipeHolder<SmokingRecipe>> recipe = findRecipe(container.getItem(slot), level);
            if (recipe.isPresent()) {
                return recipe;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<RecipeHolder<SmokingRecipe>> findRecipe(ItemStack stack, Level level) {
        return stack.isEmpty()
                ? Optional.empty()
                : level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(stack), level);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeHolder<SmokingRecipe> byKey(ResourceLocation id, Level level) {
        return (RecipeHolder<SmokingRecipe>) level.getRecipeManager().byKey(id)
                .filter(holder -> holder.value() instanceof SmokingRecipe)
                .orElse(null);
    }

    @Override
    public List<RecipeHolder<SmokingRecipe>> getAllRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(RecipeType.SMOKING);
    }
}
