package com.oblixorprime.immersivefarming.immersivecooking.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;

import java.util.ArrayList;

public class CookpotRecipe extends MultiblockRecipe {
    public static final CachedRecipeList<CookpotRecipe> RECIPES = new CachedRecipeList<>(ICRecipes.Types.COOKPOT);
    private static final RecipeMultiplier DEFAULT_MULTIPLIER = new RecipeMultiplier(() -> 1.0, () -> 1.0);
    private static final int MAX_INPUTS = 6;

    public final NonNullList<IngredientWithSize> inputs;
    public final ItemStack result;
    public final ItemStack container;

    public CookpotRecipe(NonNullList<IngredientWithSize> inputs, ItemStack result,
            ItemStack container, int cookTime, int energy) {
        super(new TagOutput(ImmersiveCookingRecipeValidation.result("Cookpot", result)), ICRecipes.Types.COOKPOT,
                ImmersiveCookingRecipeValidation.positive("Cookpot", "time", cookTime),
                ImmersiveCookingRecipeValidation.positive("Cookpot", "energy", energy), () -> DEFAULT_MULTIPLIER);
        this.inputs = ImmersiveCookingRecipeValidation.inputs("Cookpot", inputs, MAX_INPUTS);
        this.result = result;
        this.container = container;

        this.setInputListWithSizes(new ArrayList<>(this.inputs));
        this.outputList = new TagOutputList(new TagOutput(this.result));
    }

    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return ICRecipes.Serializers.COOKPOT.get();
    }

    public boolean matches(net.minecraft.world.Container inv, net.minecraft.world.level.Level level) {
        java.util.List<ItemStack> inventoryCopy = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                inventoryCopy.add(stack.copy());
            }
        }

        for (IngredientWithSize required : this.inputs) {
            int amountNeeded = required.getCount();

            java.util.Iterator<ItemStack> it = inventoryCopy.iterator();
            while (it.hasNext()) {
                ItemStack stack = it.next();

                if (required.testIgnoringSize(stack)) {
                    int amountTaken = Math.min(amountNeeded, stack.getCount());

                    amountNeeded -= amountTaken;
                    stack.shrink(amountTaken);

                    if (stack.isEmpty()) {
                        it.remove();
                    }

                    if (amountNeeded <= 0) {
                        break;
                    }
                }
            }

            if (amountNeeded > 0) {
                return false;
            }
        }

        return true;
    }
}
