package dev.emmanueltremblay.immersivefarming.block;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class IFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersiveFarming.MOD_ID);

    public static final DeferredBlock<FertileSoilBlock> SOIL = registerBlock(
            "soil",
            () -> new FertileSoilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .randomTicks())
    );

    public static final DeferredBlock<SprinklerBlock> SPRINKLER = registerBlock(
            "sprinkler",
            () -> new SprinklerBlock(false, metalNoOcclusion())
    );

    public static final DeferredBlock<SprinklerBlock> HIGH_PRESSURE_SPRINKLER = registerBlock(
            "sprinkler_extended",
            () -> new SprinklerBlock(true, metalNoOcclusion())
    );

    public static final DeferredBlock<IndustrialComposterBlock> COMPOSTER = registerBlock(
            "composter",
            () -> new IndustrialComposterBlock(metalNoOcclusion())
    );

    public static final DeferredBlock<DeadCropBlock> DEAD_CROP = registerBlock(
            "dead_crop",
            () -> new DeadCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.CROP))
    );

    public static final DeferredBlock<LiquidBlock> TREATED_WATER = BLOCKS.register(
            "treated_water",
            () -> new LiquidBlock(IFFluids.TREATED_WATER.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    private IFBlocks() {
    }

    private static BlockBehaviour.Properties metalNoOcclusion() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0F, 15.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        IFItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
