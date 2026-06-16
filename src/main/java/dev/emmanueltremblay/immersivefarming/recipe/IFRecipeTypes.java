package dev.emmanueltremblay.immersivefarming.recipe;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ImmersiveFarming.MOD_ID);

    private IFRecipeTypes() {
    }
}
