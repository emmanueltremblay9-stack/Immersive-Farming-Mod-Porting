package com.oblixorprime.immersivefarming.immersivecooking.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FoodProcessorRecipe extends MultiblockRecipe {
    private static final RecipeMultiplier DEFAULT_MULTIPLIER = new RecipeMultiplier(() -> 1.0, () -> 1.0);
    private static final int MAX_INPUTS = 8;

    public NonNullList<IngredientWithSize> inputs; // 8 slots
    @Nullable
    public final SizedFluidIngredient fluidInput;
    public final ItemStack result;

    public FoodProcessorRecipe(NonNullList<IngredientWithSize> inputs,
                               @Nullable SizedFluidIngredient fluidInput,
                               ItemStack result, int time, int energy) {
        super(new TagOutput(ImmersiveCookingRecipeValidation.result("Food Processor", result)),
                ICRecipes.Types.FOOD_PROCESSOR,
                ImmersiveCookingRecipeValidation.positive("Food Processor", "time", time),
                ImmersiveCookingRecipeValidation.positive("Food Processor", "energy", energy),
                () -> DEFAULT_MULTIPLIER);
        this.fluidInput = fluidInput;
        this.result = result;
        this.inputs = ImmersiveCookingRecipeValidation.inputs("Food Processor", inputs, MAX_INPUTS);

        this.setInputListWithSizes(new ArrayList<>(this.inputs));
        this.outputList = new TagOutputList(new TagOutput(this.result));
        this.fluidInputList = fluidInput == null ? List.of() : List.of(fluidInput);
    }

    public boolean matches(Container inv, Level level) {
        List<ItemStack> inventoryCopy = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                inventoryCopy.add(stack.copy());
            }
        }

        for (IngredientWithSize required : this.inputs) {
            int amountNeeded = required.getCount();

            Iterator<ItemStack> it = inventoryCopy.iterator();
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

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return ICRecipes.Serializers.FOOD_PROCESSOR.get();
    }

    @Override
    public NonNullList<ItemStack> getItemOutputs() {
        return NonNullList.of(ItemStack.EMPTY, result);
    }

    @Override
    public List<IngredientWithSize> getItemInputs() {
        return inputs;
    }

    @Override
    public List<SizedFluidIngredient> getFluidInputs() {
        return fluidInput == null ? List.of() : List.of(fluidInput);
    }

    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    public boolean shouldCheckItemAvailability() {
        return false;
    }
}
