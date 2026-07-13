package dev.emmanueltremblay.immersivefarming.recipe;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ComposterRecipe>> COMPOSTER =
            RECIPE_SERIALIZERS.register("composter", ComposterRecipeSerializer::new);

    private IFRecipeSerializers() {
    }
}
