package com.oblixorprime.immersivefarming.immersivecooking;

import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRecipes;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICRegisters;

public class ImmersiveCooking {
    public static final String MODID = "immersivecooking";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private ImmersiveCooking() {
    }

    public static void init(IEventBus bus) {
        ICRecipes.init();
        ICContent.init();
        ICRegisters.init(bus);
    }
}
