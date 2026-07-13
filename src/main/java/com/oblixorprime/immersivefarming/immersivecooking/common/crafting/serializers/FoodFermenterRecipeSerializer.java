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
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodFermenterRecipe;

import java.util.List;
import java.util.Optional;

public class FoodFermenterRecipeSerializer extends IERecipeSerializer<FoodFermenterRecipe> {
    public static final DualMapCodec<RegistryFriendlyByteBuf, FoodFermenterRecipe> CODECS = DualCompositeMapCodecs.composite(
            IngredientWithSize.CODECS.listOf().fieldOf("inputs"), recipe -> List.copyOf(recipe.inputs),
            IEDualCodecs.SIZED_FLUID_INGREDIENT.optionalFieldOf("fluid"), recipe -> Optional.ofNullable(recipe.fluidInput),
            DualCodecs.ITEM_STACK.fieldOf("result"), recipe -> recipe.result,
            DualCodecs.ITEM_STACK.optionalFieldOf("container", ItemStack.EMPTY), recipe -> recipe.container,
            DualCodecs.INT.optionalFieldOf("time", 200), FoodFermenterRecipe::getBaseTime,
            DualCodecs.INT.optionalFieldOf("energy", 2000), FoodFermenterRecipe::getBaseEnergy,
            (inputs, fluidInput, result, container, time, energy) -> new FoodFermenterRecipe(
                    toNonNullList(inputs), fluidInput.orElse(null), result, container, time, energy)
    );

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, FoodFermenterRecipe> codecs() {
        return CODECS;
    }

    @Override
    public ItemStack getIcon() {
        return ICContent.Multiblock.FOOD_FERMENTER.iconStack();
    }

    private static NonNullList<IngredientWithSize> toNonNullList(List<IngredientWithSize> inputs) {
        NonNullList<IngredientWithSize> result = NonNullList.create();
        result.addAll(inputs);
        return result;
    }
}
