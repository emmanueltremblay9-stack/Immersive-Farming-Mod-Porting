package com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.RecipeInput;

final class RecipeInputContainers {
    private RecipeInputContainers() {
    }

    static SimpleContainer copyOf(RecipeInput input) {
        SimpleContainer container = new SimpleContainer(input.size());
        for (int slot = 0; slot < input.size(); slot++) {
            container.setItem(slot, input.getItem(slot).copy());
        }
        return container;
    }
}
