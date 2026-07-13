package com.oblixorprime.immersivefarming.immersivecooking.common.utils.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.jetbrains.annotations.Nullable;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.GrillOvenMenu;

import java.util.Optional;

public class GrillOvenRecipeTransferHandler implements IRecipeTransferHandler<GrillOvenMenu, RecipeHolder<SmokingRecipe>> {
    private final IRecipeTransferHandlerHelper helper;

    public GrillOvenRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<GrillOvenMenu> getContainerClass() {
        return GrillOvenMenu.class;
    }

    @Override
    public Optional<MenuType<GrillOvenMenu>> getMenuType() {
        return Optional.of((MenuType<GrillOvenMenu>) ICContent.MenuTypes.GRILL_OVEN.getType());
    }

    @Override
    public RecipeType<RecipeHolder<SmokingRecipe>> getRecipeType() {
        return RecipeTypes.SMOKING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(GrillOvenMenu container, RecipeHolder<SmokingRecipe> recipe,
            IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        java.util.List<Integer> inputSlots = java.util.List.of(0, 1, 2);
        IRecipeTransferError lasterror = null;
        boolean success = false;

        for (int slotIndex : inputSlots) {
            // Create a temporary handler for this specific slot.
            var handler = helper.createUnregisteredRecipeTransferHandler(
                    new mezz.jei.api.recipe.transfer.IRecipeTransferInfo<GrillOvenMenu, RecipeHolder<SmokingRecipe>>() {
                        @Override
                        public Class<GrillOvenMenu> getContainerClass() {
                            return GrillOvenMenu.class;
                        }

                        @Override
                        public Optional<MenuType<GrillOvenMenu>> getMenuType() {
                            return Optional.of((MenuType<GrillOvenMenu>) ICContent.MenuTypes.GRILL_OVEN.getType());
                        }

                        @Override
                        public RecipeType<RecipeHolder<SmokingRecipe>> getRecipeType() {
                            return RecipeTypes.SMOKING;
                        }

                        @Override
                        public boolean canHandle(GrillOvenMenu container, RecipeHolder<SmokingRecipe> recipe) {
                            return true;
                        }

                        @Override
                        public java.util.List<net.minecraft.world.inventory.Slot> getRecipeSlots(
                                GrillOvenMenu container,
                                RecipeHolder<SmokingRecipe> recipe) {
                            return java.util.Collections.singletonList(container.getSlot(slotIndex));
                        }

                        @Override
                        public java.util.List<net.minecraft.world.inventory.Slot> getInventorySlots(
                                GrillOvenMenu container,
                                RecipeHolder<SmokingRecipe> recipe) {
                            java.util.List<net.minecraft.world.inventory.Slot> slots = new java.util.ArrayList<>();
                            for (int i = 4; i < container.slots.size(); i++) {
                                slots.add(container.getSlot(i));
                            }
                            return slots;
                        }
                    });

            IRecipeTransferError error = handler.transferRecipe(container, recipe, recipeSlots, player, maxTransfer,
                    doTransfer);
            if (error == null) {
                success = true;
                if (!maxTransfer) {
                    return null;
                }
            } else {
                lasterror = error;
            }
        }

        if (success)
            return null;
        return lasterror;
    }
}
