package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Compat;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Resource;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FDCookpotRecipeProvider implements IMultiblockRecipeProvider<CookpotRecipe> {
    private static final Lazy<RecipeManager.CachedCheck<RecipeWrapper, CookingPotRecipe>> cookingPotCheckerLazySupplier = Lazy
            .of(() -> RecipeManager.createCheck(ModRecipeTypes.COOKING.get()));

    @Override
    public boolean canProvide() {
        return Compat.isFarmersDelightInstalled();
    }

    @Override
    public boolean hasMultiInput() {
        return true;
    }

    @Override
    public Optional<RecipeHolder<CookpotRecipe>> findRecipe(Container container, Level level) {
        if (!canProvide() || container.isEmpty())
            return Optional.empty();

        return getAllRecipes(level).stream()
                .filter(recipe -> recipe.value().matches(container, level))
                .findFirst();
    }

    @Override
    public Optional<RecipeHolder<CookpotRecipe>> findRecipe(ItemStack stack, Level level) {
        if (!canProvide() || stack.isEmpty())
            return Optional.empty();

        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COOKING.get()).stream()
                .filter(fdRecipe -> ItemStack.isSameItem(fdRecipe.value().getResultItem(level.registryAccess()), stack))
                .findFirst()
                .map(fdRecipe -> toHolder(fdRecipe.id(), toCookpotRecipe(fdRecipe.value(), level, fdRecipe.value().getCookTime() * 8)));
    }

    @Override
    public Optional<RecipeHolder<CookpotRecipe>> findRecipe(net.minecraft.world.item.crafting.RecipeInput input, Level level) {
        return findRecipe(RecipeInputContainers.copyOf(input), level);
    }

    @Override
    public RecipeHolder<CookpotRecipe> byKey(ResourceLocation id, Level level) {
        RecipeHolder<CookpotRecipe> cached = CookpotRecipe.RECIPES.getRecipes(level).stream()
                .filter(holder -> holder.id().equals(id))
                .findFirst()
                .orElse(null);
        if (cached != null)
            return cached;

        ResourceLocation fdId = id;
        if (id.getNamespace().equals("immersivecooking")) {
            fdId = ResourceLocation.fromNamespaceAndPath("farmersdelight", id.getPath());
        }

        return level.getRecipeManager().byKey(fdId)
                .filter(r -> r.value() instanceof CookingPotRecipe)
                .map(r -> toHolder(id, toCookpotRecipe((CookingPotRecipe) r.value(), level)))
                .orElse(null);
    }

    @Override
    public List<RecipeHolder<CookpotRecipe>> getAllRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COOKING.get()).stream()
                .map(fdRecipe -> toHolder(fdRecipe.id(), toCookpotRecipe(fdRecipe.value(), level)))
                .collect(Collectors.toList());
    }

    private RecipeHolder<CookpotRecipe> toHolder(ResourceLocation sourceId, CookpotRecipe recipe) {
        return new RecipeHolder<>(Resource.mod(sourceId.getPath()), recipe);
    }

    private CookpotRecipe toCookpotRecipe(CookingPotRecipe fdRecipe, Level level) {
        return toCookpotRecipe(fdRecipe, level, fdRecipe.getCookTime() * 4);
    }

    private CookpotRecipe toCookpotRecipe(CookingPotRecipe fdRecipe, Level level, int energy) {
        NonNullList<IngredientWithSize> inputs = fdRecipe.getIngredients().stream()
                .map(IngredientWithSize::new)
                .collect(Collectors.toCollection(NonNullList::create));

        return new CookpotRecipe(
                inputs,
                fdRecipe.getResultItem(level.registryAccess()),
                fdRecipe.getOutputContainer(),
                (int) (fdRecipe.getCookTime() * 0.75),
                energy);
    }
}
