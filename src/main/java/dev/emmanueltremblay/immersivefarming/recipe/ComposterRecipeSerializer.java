package dev.emmanueltremblay.immersivefarming.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class ComposterRecipeSerializer implements RecipeSerializer<ComposterRecipe> {
    private static final MapCodec<ComposterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("fluidProduct").forGetter(ComposterRecipe::isFluidProduct),
            Ingredient.CODEC.optionalFieldOf("input").forGetter(ComposterRecipe::itemInput),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("output0", FluidStack.EMPTY).forGetter(ComposterRecipe::wetMatterOutput),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("output1", FluidStack.EMPTY).forGetter(ComposterRecipe::dryMatterOutput),
            ComposterRecipe.FluidIngredient.CODEC.optionalFieldOf("input0").forGetter(ComposterRecipe::waterInput),
            ComposterRecipe.FluidIngredient.CODEC.optionalFieldOf("input1").forGetter(ComposterRecipe::wetMatterInput),
            ComposterRecipe.FluidIngredient.CODEC.optionalFieldOf("input2").forGetter(ComposterRecipe::dryMatterInput),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(ComposterRecipe::itemOutput),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("energy").forGetter(ComposterRecipe::energy)
    ).apply(instance, ComposterRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ComposterRecipe> STREAM_CODEC = StreamCodec.of(
            ComposterRecipeSerializer::toNetwork,
            ComposterRecipeSerializer::fromNetwork
    );

    @Override
    public MapCodec<ComposterRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ComposterRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static ComposterRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        boolean fluidProduct = buffer.readBoolean();
        Optional<Ingredient> itemInput = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
        FluidStack wetMatterOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        FluidStack dryMatterOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        Optional<ComposterRecipe.FluidIngredient> waterInput = readFluidIngredient(buffer);
        Optional<ComposterRecipe.FluidIngredient> wetMatterInput = readFluidIngredient(buffer);
        Optional<ComposterRecipe.FluidIngredient> dryMatterInput = readFluidIngredient(buffer);
        ItemStack itemOutput = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        int energy = buffer.readVarInt();
        return new ComposterRecipe(fluidProduct, itemInput, wetMatterOutput, dryMatterOutput,
                waterInput, wetMatterInput, dryMatterInput, itemOutput, energy);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ComposterRecipe recipe) {
        buffer.writeBoolean(recipe.isFluidProduct());
        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, recipe.itemInput());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.wetMatterOutput());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.dryMatterOutput());
        writeFluidIngredient(buffer, recipe.waterInput());
        writeFluidIngredient(buffer, recipe.wetMatterInput());
        writeFluidIngredient(buffer, recipe.dryMatterInput());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.itemOutput());
        buffer.writeVarInt(recipe.energy());
    }

    private static Optional<ComposterRecipe.FluidIngredient> readFluidIngredient(RegistryFriendlyByteBuf buffer) {
        return buffer.readBoolean()
                ? Optional.of(ComposterRecipe.FluidIngredient.STREAM_CODEC.decode(buffer))
                : Optional.empty();
    }

    private static void writeFluidIngredient(RegistryFriendlyByteBuf buffer, Optional<ComposterRecipe.FluidIngredient> ingredient) {
        buffer.writeBoolean(ingredient.isPresent());
        ingredient.ifPresent(value -> ComposterRecipe.FluidIngredient.STREAM_CODEC.encode(buffer, value));
    }
}
