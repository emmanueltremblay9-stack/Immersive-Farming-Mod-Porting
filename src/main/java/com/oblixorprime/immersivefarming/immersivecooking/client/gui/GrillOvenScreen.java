package com.oblixorprime.immersivefarming.immersivecooking.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.GrillOvenMenu;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Resource;

public class GrillOvenScreen extends ICContainerScreen<GrillOvenMenu> {
    private static final ResourceLocation TEXTURE = Resource.texture("gui/grill_oven.png");

    public GrillOvenScreen(GrillOvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
    }
}
