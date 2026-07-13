package com.oblixorprime.immersivefarming.immersivecooking.common;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oblixorprime.immersivefarming.immersivecooking.ImmersiveCooking;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.CookpotMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.FoodFermenterMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.FoodProcessorMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.GrillOvenMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.CookpotLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodFermenterLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodProcessorLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.GrillOvenLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.fluids.ICFluids;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.CookpotMenu;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.FoodFermenterMenu;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.FoodProcessorMenu;
import com.oblixorprime.immersivefarming.immersivecooking.common.gui.GrillOvenMenu;

import java.util.Collection;

import static com.oblixorprime.immersivefarming.immersivecooking.ImmersiveCooking.MODID;

public final class ICContent {
    public static final Logger LOGGER = LoggerFactory.getLogger(ImmersiveCooking.MODID + "/Content");

    public static class Multiblock {
        public static final MultiblockRegistration<GrillOvenLogic.State> GRILL_OVEN = ICRegisters
                .registerStoneMultiblock(
                        "grill_oven",
                        new GrillOvenLogic(),
                        () -> GrillOvenMultiblock.INSTANCE,
                        builder -> builder.gui(MenuTypes.GRILL_OVEN));

        public static final MultiblockRegistration<CookpotLogic.State> COOKPOT = ICRegisters
                .registerMetalMultiblock(
                        "cookpot",
                        new CookpotLogic(),
                        () -> CookpotMultiblock.INSTANCE,
                        builder -> builder.gui(MenuTypes.COOKPOT).redstone(s -> s.rsState));

        public static final MultiblockRegistration<FoodFermenterLogic.State> FOOD_FERMENTER = ICRegisters
                .registerMetalMultiblock(
                        "food_fermenter",
                        new FoodFermenterLogic(),
                        () -> FoodFermenterMultiblock.INSTANCE,
                        builder -> builder.gui(MenuTypes.FOOD_FERMENTER).redstone(s -> s.rsState));

        public static final MultiblockRegistration<FoodProcessorLogic.State> FOOD_PROCESSOR = ICRegisters
                .registerMetalMultiblock(
                        "food_processor",
                        new FoodProcessorLogic(),
                        () -> FoodProcessorMultiblock.INSTANCE,
                        builder -> builder.gui(MenuTypes.FOOD_PROCESSOR).redstone(s -> s.rsState));

        public static void forceClassLoad() {
        }
    }

    public static class MenuTypes {
        public static final IEMenuTypes.MultiblockContainer<GrillOvenLogic.State, GrillOvenMenu> GRILL_OVEN = IEMenuTypes
                .registerMultiblock(
                        "grill_oven",
                        GrillOvenMenu::makeServer,
                        GrillOvenMenu::makeClient);

        public static final IEMenuTypes.MultiblockContainer<CookpotLogic.State, CookpotMenu> COOKPOT = IEMenuTypes
                .registerMultiblock(
                        "cookpot",
                        CookpotMenu::makeServer,
                        CookpotMenu::makeClient);

        public static final IEMenuTypes.MultiblockContainer<FoodFermenterLogic.State, FoodFermenterMenu> FOOD_FERMENTER = IEMenuTypes
                .registerMultiblock(
                        "food_fermenter",
                        FoodFermenterMenu::makeServer,
                        FoodFermenterMenu::makeClient);

        public static final IEMenuTypes.MultiblockContainer<FoodProcessorLogic.State, FoodProcessorMenu> FOOD_PROCESSOR = IEMenuTypes
                .registerMultiblock(
                        "food_processor",
                        FoodProcessorMenu::makeServer,
                        FoodProcessorMenu::makeClient);

        public static void forceClassLoad() {
        }
    }

    public static class Tabs {
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = ICRegisters.registerCreativeTab("main",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup." + MODID))
                        .icon(() -> new ItemStack(IEItems.Tools.STEEL_HOE))
                        .displayItems((params, output) -> {
                            Collection<DeferredHolder<Item, ? extends Item>> itemRegistries = ICRegisters.ITEM_REGISTER.getEntries();
                            for (var itemRegistryObject : itemRegistries) {
                                if (!(itemRegistryObject.get() instanceof MultiblockItem)) {
                                    output.accept(itemRegistryObject.get());
                                }
                            }
                        })
                        .build());

        public static void forceClassLoad() {
        }
    }

    public static class Sounds {
        public static final DeferredHolder<SoundEvent, SoundEvent> COOKPOT_ACTIVE = ICRegisters.registerSoundEvent("block.cookpot.active");
        public static final DeferredHolder<SoundEvent, SoundEvent> FOOD_FERMENTER_ACTIVE = ICRegisters.registerSoundEvent("block.food_fermenter.active");
        public static final DeferredHolder<SoundEvent, SoundEvent> FOOD_PROCESSOR_ACTIVE = ICRegisters.registerSoundEvent("block.food_processor.active");

        public static void forceClassLoad() {
        }
    }

    public static class Fluids {
        public static final ICFluids.FluidEntry APPLE_JUICE = ICFluids.FluidEntry.make("apple_juice", 0xAFFFE74B);

        // Red Grapes
        public static final ICFluids.FluidEntry RED_GRAPE_JUICE = ICFluids.FluidEntry.make("red_grapejuice", 0xFF7D2ED8);
        public static final ICFluids.FluidEntry RED_TAIGA_GRAPE_JUICE = ICFluids.FluidEntry.make("red_taiga_grapejuice", 0xFF7D2ED8);
        public static final ICFluids.FluidEntry RED_JUNGLE_GRAPE_JUICE = ICFluids.FluidEntry.make("red_jungle_grapejuice", 0xFF7D2ED8);
        public static final ICFluids.FluidEntry RED_SAVANNA_GRAPE_JUICE = ICFluids.FluidEntry.make("red_savanna_grapejuice", 0xFF7D2ED8);

        // White Grapes
        public static final ICFluids.FluidEntry WHITE_GRAPE_JUICE = ICFluids.FluidEntry.make("white_grapejuice", 0xFF70812D);
        public static final ICFluids.FluidEntry WHITE_TAIGA_GRAPE_JUICE = ICFluids.FluidEntry.make("white_taiga_grapejuice", 0xFF70812D);
        public static final ICFluids.FluidEntry WHITE_JUNGLE_GRAPE_JUICE = ICFluids.FluidEntry.make("white_jungle_grapejuice", 0xFF70812D);
        public static final ICFluids.FluidEntry WHITE_SAVANNA_GRAPE_JUICE = ICFluids.FluidEntry.make("white_savanna_grapejuice", 0xFF70812D);

        public static void forceClassLoad() {
        }
    }

    public static void init() {
        Multiblock.forceClassLoad();
        MenuTypes.forceClassLoad();
        Tabs.forceClassLoad();
        Sounds.forceClassLoad();
        Fluids.forceClassLoad();
        MultiblockHandler.registerMultiblock(GrillOvenMultiblock.INSTANCE);
        MultiblockHandler.registerMultiblock(CookpotMultiblock.INSTANCE);
        MultiblockHandler.registerMultiblock(FoodFermenterMultiblock.INSTANCE);
        MultiblockHandler.registerMultiblock(FoodProcessorMultiblock.INSTANCE);
    }
}
