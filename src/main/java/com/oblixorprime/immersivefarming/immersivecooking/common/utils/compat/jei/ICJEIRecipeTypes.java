package com.oblixorprime.immersivefarming.immersivecooking.common.utils.compat.jei;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodFermenterRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodProcessorRecipe;

public class ICJEIRecipeTypes {
    public static final RecipeType<CookpotRecipe> COOKPOT = create(ICRecipes.Types.COOKPOT);
    public static final RecipeType<FoodFermenterRecipe> FOOD_FERMENTER = create(ICRecipes.Types.FOOD_FERMENTER);
    public static final RecipeType<FoodProcessorRecipe> FOOD_PROCESSOR = create(ICRecipes.Types.FOOD_PROCESSOR);

    private static <T extends Recipe<?>> RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type) {
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
