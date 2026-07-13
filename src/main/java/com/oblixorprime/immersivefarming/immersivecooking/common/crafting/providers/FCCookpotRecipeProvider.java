package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
import net.satisfy.farm_and_charm.core.registry.RecipeTypeRegistry;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Compat;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Resource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FCCookpotRecipeProvider implements IMultiblockRecipeProvider<CookpotRecipe> {
    private static final int DEFAULT_COOK_TIME = 200;
    private static final int DEFAULT_ENERGY = 800;

    @Override
    public boolean canProvide() {
        return Compat.isFarmCharmInstalled();
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

        return level.getRecipeManager()
                .getAllRecipesFor(RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.get())
                .stream()
                .filter(r -> ItemStack.isSameItem(r.value().getResultItem(level.registryAccess()), stack))
                .findFirst()
                .map(facRecipe -> toHolder(facRecipe.id(), toCookpotRecipe(facRecipe.value(), level)));
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

        ResourceLocation facId = id;
        if (id.getNamespace().equals("immersivecooking")) {
            facId = ResourceLocation.fromNamespaceAndPath("farm_and_charm", id.getPath());
        }

        return level.getRecipeManager().byKey(facId)
                .filter(r -> r.value() instanceof CookingPotRecipe)
                .map(r -> toHolder(id, toCookpotRecipe((CookingPotRecipe) r.value(), level)))
                .orElse(null);
    }

    @Override
    public List<RecipeHolder<CookpotRecipe>> getAllRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.get()).stream()
                .map(r -> toHolder(r.id(), toCookpotRecipe(r.value(), level)))
                .collect(Collectors.toList());
    }

    private RecipeHolder<CookpotRecipe> toHolder(ResourceLocation sourceId, CookpotRecipe recipe) {
        return new RecipeHolder<>(Resource.mod(sourceId.getPath()), recipe);
    }

    private CookpotRecipe toCookpotRecipe(CookingPotRecipe facRecipe, Level level) {
        NonNullList<IngredientWithSize> inputs = facRecipe.getIngredients().stream()
                .map(IngredientWithSize::new)
                .collect(Collectors.toCollection(NonNullList::create));

        ItemStack output = facRecipe.getResultItem(level.registryAccess());

        ItemStack container = facRecipe.isContainerRequired()
                ? facRecipe.getContainerItem()
                : ItemStack.EMPTY;

        return new CookpotRecipe(
                inputs,
                output,
                container,
                DEFAULT_COOK_TIME,
                DEFAULT_ENERGY);
    }
}
