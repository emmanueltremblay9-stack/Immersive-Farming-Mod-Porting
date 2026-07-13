package com.oblixorprime.immersivefarming.immersivecooking.common.utils.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import com.oblixorprime.immersivefarming.immersivecooking.ImmersiveCooking;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.CookpotScreen;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.FoodFermenterScreen;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.FoodProcessorScreen;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.CookpotLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodFermenterLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodProcessorLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodFermenterRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodProcessorRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Resource;

import java.util.List;

@JeiPlugin
public class ICJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = Resource.mod("main");

    public ICJEIPlugin() {
        ImmersiveCooking.LOGGER.info("ICJEIPlugin Constructed!");
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        ImmersiveCooking.LOGGER.info("Registering categories to JEI...");
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new CookpotRecipeCategory(guiHelper));
        registry.addRecipeCategories(new FoodFermenterRecipeCategory(guiHelper));
        registry.addRecipeCategories(new FoodProcessorRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ImmersiveCooking.LOGGER.info("Registering recipes to JEI...");
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            ImmersiveCooking.LOGGER.warn("Skipping Immersive Cooking JEI recipe registration because no client level is available yet.");
            return;
        }

        List<CookpotRecipe> cookpotRecipes = ((CookpotLogic) ICContent.Multiblock.COOKPOT.logic()).getAllProvidedRecipes(level);
        ImmersiveCooking.LOGGER.info("Found {} Cookpot recipes.", cookpotRecipes.size());
        registration.addRecipes(ICJEIRecipeTypes.COOKPOT, cookpotRecipes);
        List<FoodFermenterRecipe> foodFermenterRecipes = ((FoodFermenterLogic) ICContent.Multiblock.FOOD_FERMENTER.logic()).getAllProvidedRecipes(level);
        ImmersiveCooking.LOGGER.info("Found {} Food Fermenter recipes.", foodFermenterRecipes.size());
        registration.addRecipes(ICJEIRecipeTypes.FOOD_FERMENTER, foodFermenterRecipes);
        List<FoodProcessorRecipe> foodProcessorRecipes = ((FoodProcessorLogic) ICContent.Multiblock.FOOD_PROCESSOR.logic()).getAllProvidedRecipes(level);
        ImmersiveCooking.LOGGER.info("Found {} Food Processor recipes.", foodProcessorRecipes.size());
        registration.addRecipes(ICJEIRecipeTypes.FOOD_PROCESSOR, foodProcessorRecipes);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        ImmersiveCooking.LOGGER.info("Registering recipe transfer handlers to JEI...");
        registration.addRecipeTransferHandler(new GrillOvenRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.SMOKING);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ImmersiveCooking.LOGGER.info("Registering recipe catalysts to JEI...");
        registration.addRecipeCatalyst(ICContent.Multiblock.COOKPOT.iconStack(), ICJEIRecipeTypes.COOKPOT);
        registration.addRecipeCatalyst(ICContent.Multiblock.FOOD_FERMENTER.iconStack(), ICJEIRecipeTypes.FOOD_FERMENTER);
        registration.addRecipeCatalyst(ICContent.Multiblock.FOOD_PROCESSOR.iconStack(), ICJEIRecipeTypes.FOOD_PROCESSOR);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        ImmersiveCooking.LOGGER.info("Registering gui handlers to JEI...");
        registration.addRecipeClickArea(CookpotScreen.class, 91, 19, 16, 12, ICJEIRecipeTypes.COOKPOT);
        registration.addRecipeClickArea(FoodFermenterScreen.class, 91, 19, 16, 12, ICJEIRecipeTypes.FOOD_FERMENTER);
        registration.addRecipeClickArea(FoodProcessorScreen.class, 91, 19, 16, 12, ICJEIRecipeTypes.FOOD_PROCESSOR);
    }
}
