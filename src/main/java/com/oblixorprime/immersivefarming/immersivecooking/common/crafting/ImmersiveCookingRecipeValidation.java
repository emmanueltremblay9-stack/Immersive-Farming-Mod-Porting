package com.oblixorprime.immersivefarming.immersivecooking.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

final class ImmersiveCookingRecipeValidation {
    private ImmersiveCookingRecipeValidation() {
    }

    static NonNullList<IngredientWithSize> inputs(String recipeName, NonNullList<IngredientWithSize> inputs,
            int maxInputs) {
        if (inputs == null)
            throw new IllegalArgumentException(recipeName + " recipe inputs must not be null");
        if (inputs.isEmpty())
            throw new IllegalArgumentException(recipeName + " recipe must have at least one input");
        if (inputs.size() > maxInputs)
            throw new IllegalArgumentException(recipeName + " recipe has " + inputs.size() + " inputs but only "
                    + maxInputs + " are supported");
        for (IngredientWithSize input : inputs) {
            if (input == null)
                throw new IllegalArgumentException(recipeName + " recipe inputs must not contain null entries");
        }
        return inputs;
    }

    static ItemStack result(String recipeName, ItemStack result) {
        if (result == null || result.isEmpty())
            throw new IllegalArgumentException(recipeName + " recipe result must not be empty");
        return result;
    }

    static int positive(String recipeName, String fieldName, int value) {
        if (value <= 0)
            throw new IllegalArgumentException(recipeName + " recipe " + fieldName + " must be positive");
        return value;
    }
}
