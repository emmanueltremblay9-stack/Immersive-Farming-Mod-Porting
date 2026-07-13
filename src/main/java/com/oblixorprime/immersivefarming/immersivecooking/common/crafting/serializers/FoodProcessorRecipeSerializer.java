package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodProcessorRecipe;

import java.util.List;
import java.util.Optional;

public class FoodProcessorRecipeSerializer extends IERecipeSerializer<FoodProcessorRecipe> {
    public static final DualMapCodec<RegistryFriendlyByteBuf, FoodProcessorRecipe> CODECS = DualCompositeMapCodecs.composite(
            IngredientWithSize.CODECS.listOf().fieldOf("inputs"), recipe -> List.copyOf(recipe.inputs),
            IEDualCodecs.SIZED_FLUID_INGREDIENT.optionalFieldOf("fluid"), recipe -> Optional.ofNullable(recipe.fluidInput),
            DualCodecs.ITEM_STACK.fieldOf("result"), recipe -> recipe.result,
            DualCodecs.INT.optionalFieldOf("time", 200), FoodProcessorRecipe::getBaseTime,
            DualCodecs.INT.optionalFieldOf("energy", 2000), FoodProcessorRecipe::getBaseEnergy,
            (inputs, fluidInput, result, time, energy) -> new FoodProcessorRecipe(
                    toNonNullList(inputs), fluidInput.orElse(null), result, time, energy)
    );

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, FoodProcessorRecipe> codecs() {
        return CODECS;
    }

    @Override
    public ItemStack getIcon() {
        return ICContent.Multiblock.FOOD_PROCESSOR.iconStack();
    }

    private static NonNullList<IngredientWithSize> toNonNullList(List<IngredientWithSize> inputs) {
        NonNullList<IngredientWithSize> result = NonNullList.create();
        result.addAll(inputs);
        return result;
    }
}
