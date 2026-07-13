package com.oblixorprime.immersivefarming.immersivecooking.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.CookpotScreen;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.FoodFermenterScreen;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.FoodProcessorScreen;
import com.oblixorprime.immersivefarming.immersivecooking.client.gui.GrillOvenScreen;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerScreens);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ICContent.MenuTypes.GRILL_OVEN.getType(), GrillOvenScreen::new);
        event.register(ICContent.MenuTypes.COOKPOT.getType(), CookpotScreen::new);
        event.register(ICContent.MenuTypes.FOOD_FERMENTER.getType(), FoodFermenterScreen::new);
        event.register(ICContent.MenuTypes.FOOD_PROCESSOR.getType(), FoodProcessorScreen::new);
    }
}
