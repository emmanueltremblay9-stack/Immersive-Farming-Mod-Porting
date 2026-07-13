package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;

import java.util.List;

public class CookpotRecipeSerializer extends IERecipeSerializer<CookpotRecipe> {
    public static final DualMapCodec<RegistryFriendlyByteBuf, CookpotRecipe> CODECS = DualCompositeMapCodecs.composite(
            IngredientWithSize.CODECS.listOf().fieldOf("inputs"), recipe -> List.copyOf(recipe.inputs),
            DualCodecs.ITEM_STACK.fieldOf("result"), recipe -> recipe.result,
            DualCodecs.ITEM_STACK.optionalFieldOf("container", ItemStack.EMPTY), recipe -> recipe.container,
            DualCodecs.INT.optionalFieldOf("time", 200), CookpotRecipe::getBaseTime,
            DualCodecs.INT.optionalFieldOf("energy", 2000), CookpotRecipe::getBaseEnergy,
            (inputs, result, container, time, energy) -> new CookpotRecipe(toNonNullList(inputs), result, container, time, energy)
    );

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, CookpotRecipe> codecs() {
        return CODECS;
    }

    @Override
    public ItemStack getIcon() {
        return ICContent.Multiblock.COOKPOT.iconStack();
    }

    private static NonNullList<IngredientWithSize> toNonNullList(List<IngredientWithSize> inputs) {
        NonNullList<IngredientWithSize> result = NonNullList.create();
        result.addAll(inputs);
        return result;
    }
}
