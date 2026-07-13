package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultCookpotRecipeProvider extends AbstractMultiblockRecipeProvider<CookpotRecipe> {
    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public boolean hasMultiInput() {
        return true;
    }

    @Override
    protected RecipeType<CookpotRecipe> getRecipeType() {
        return ICRecipes.Types.COOKPOT.get();
    }

    @Override
    protected Class<CookpotRecipe> getRecipeClass() {
        return CookpotRecipe.class;
    }

    @Override
    public Optional<RecipeHolder<CookpotRecipe>> findRecipe(Container container, Level level) {
        if (container.isEmpty()) {
            return Optional.empty();
        }

        return getAllRecipes(level).stream()
                .filter(recipe -> recipe.value().matches(container, level))
                .findFirst();
    }

    @Override
    public Optional<RecipeHolder<CookpotRecipe>> findRecipe(RecipeInput input, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), level);
    }
}
