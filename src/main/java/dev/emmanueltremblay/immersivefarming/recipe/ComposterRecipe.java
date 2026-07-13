package dev.emmanueltremblay.immersivefarming.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.Optional;

public class ComposterRecipe implements Recipe<ComposterRecipeInput> {
    private final boolean fluidProduct;
    private final Optional<Ingredient> itemInput;
    private final FluidStack wetMatterOutput;
    private final FluidStack dryMatterOutput;
    private final Optional<FluidIngredient> waterInput;
    private final Optional<FluidIngredient> wetMatterInput;
    private final Optional<FluidIngredient> dryMatterInput;
    private final ItemStack itemOutput;
    private final int energy;

    public ComposterRecipe(
            boolean fluidProduct,
            Optional<Ingredient> itemInput,
            FluidStack wetMatterOutput,
            FluidStack dryMatterOutput,
            Optional<FluidIngredient> waterInput,
            Optional<FluidIngredient> wetMatterInput,
            Optional<FluidIngredient> dryMatterInput,
            ItemStack itemOutput,
            int energy
    ) {
        if (energy <= 0) {
            throw new IllegalArgumentException("Composter recipe energy must be positive");
        }
        this.fluidProduct = fluidProduct;
        this.itemInput = itemInput;
        this.wetMatterOutput = wetMatterOutput;
        this.dryMatterOutput = dryMatterOutput;
        this.waterInput = waterInput;
        this.wetMatterInput = wetMatterInput;
        this.dryMatterInput = dryMatterInput;
        this.itemOutput = itemOutput;
        this.energy = energy;
    }

    public boolean isFluidProduct() {
        return fluidProduct;
    }

    public Optional<Ingredient> itemInput() {
        return itemInput;
    }

    public FluidStack wetMatterOutput() {
        return wetMatterOutput;
    }

    public FluidStack dryMatterOutput() {
        return dryMatterOutput;
    }

    public Optional<FluidIngredient> waterInput() {
        return waterInput;
    }

    public Optional<FluidIngredient> wetMatterInput() {
        return wetMatterInput;
    }

    public Optional<FluidIngredient> dryMatterInput() {
        return dryMatterInput;
    }

    public ItemStack itemOutput() {
        return itemOutput;
    }

    public int energy() {
        return energy;
    }

    public int processTime() {
        return fluidProduct ? Math.max(1, energy) : 100;
    }

    public int energyPerTick() {
        return Math.max(1, energy / processTime());
    }

    @Override
    public boolean matches(ComposterRecipeInput input, Level level) {
        if (fluidProduct) {
            return itemInput.isPresent() && itemInput.get().test(input.item());
        }
        return waterInput.isPresent()
                && wetMatterInput.isPresent()
                && dryMatterInput.isPresent()
                && waterInput.get().matches(input.fluid(0))
                && wetMatterInput.get().matches(input.fluid(1))
                && dryMatterInput.get().matches(input.fluid(2));
    }

    @Override
    public ItemStack assemble(ComposterRecipeInput input, HolderLookup.Provider registries) {
        return itemOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return itemOutput.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        itemInput.ifPresent(ingredients::add);
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(IFItems.COMPOST.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IFRecipeSerializers.COMPOSTER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return IFRecipeTypes.COMPOSTER.get();
    }

    public record FluidIngredient(TagKey<Fluid> tag, int amount) {
        public FluidIngredient {
            Objects.requireNonNull(tag, "Composter fluid ingredient tag must not be null");
            if (amount <= 0) {
                throw new IllegalArgumentException("Composter fluid ingredient amount must be positive");
            }
        }

        public static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(FluidIngredient::tag),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(FluidIngredient::amount)
        ).apply(instance, FluidIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public FluidIngredient decode(RegistryFriendlyByteBuf buffer) {
                ResourceLocation tagId = ResourceLocation.STREAM_CODEC.decode(buffer);
                return new FluidIngredient(TagKey.create(Registries.FLUID, tagId), buffer.readVarInt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, FluidIngredient ingredient) {
                ResourceLocation.STREAM_CODEC.encode(buffer, ingredient.tag().location());
                buffer.writeVarInt(ingredient.amount());
            }
        };

        public boolean matches(FluidStack stack) {
            return !stack.isEmpty() && stack.getAmount() >= amount && stack.is(tag);
        }
    }
}
