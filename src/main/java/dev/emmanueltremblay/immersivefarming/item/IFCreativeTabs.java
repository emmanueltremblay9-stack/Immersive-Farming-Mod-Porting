package dev.emmanueltremblay.immersivefarming.item;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.immersivefarming"))
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .icon(() -> IFBlocks.SPRINKLER.asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(IFBlocks.SOIL.get());
                        output.accept(IFBlocks.SPRINKLER.get());
                        output.accept(IFBlocks.HIGH_PRESSURE_SPRINKLER.get());
                        output.accept(IFBlocks.COMPOSTER.get());
                        output.accept(IFBlocks.DEAD_CROP.get());
                        output.accept(IFItems.TREATED_WATER_BUCKET.get());
                        output.accept(IFItems.COMPOST.get());
                        output.accept(IFItems.WHEEL.get());
                        output.accept(IFItems.PLOW.get());
                        output.accept(IFItems.SOWER.get());
                    })
                    .build()
    );

    private IFCreativeTabs() {
    }
}
