package dev.emmanueltremblay.immersivefarming.test;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.ComposterMultiblock;
import dev.emmanueltremblay.immersivefarming.block.FertileSoilBlock;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.IndustrialComposterBlock;
import dev.emmanueltremblay.immersivefarming.block.entity.IndustrialComposterBlockEntity;
import dev.emmanueltremblay.immersivefarming.block.SprinklerBlock;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.event.FarmingEvents;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import dev.emmanueltremblay.immersivefarming.integration.ie.IFIEMultiblocks;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import dev.emmanueltremblay.immersivefarming.menu.ComposterMenu;
import dev.emmanueltremblay.immersivefarming.recipe.ComposterRecipe;
import dev.emmanueltremblay.immersivefarming.recipe.IFRecipeSerializers;
import dev.emmanueltremblay.immersivefarming.util.FarmingLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICTags;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.CookpotMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.FoodFermenterMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.FoodProcessorMultiblock;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodFermenterLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.FoodProcessorLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks.logic.ICMultiblockLogic;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.CookpotRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodFermenterRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.FoodProcessorRecipe;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.DefaultCookpotRecipeProvider;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.DefaultFoodFermenterRecipeProvider;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.DefaultFoodProcessorRecipeProvider;
import com.oblixorprime.immersivefarming.immersivecooking.common.crafting.providers.SmokingRecipeProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;

@GameTestHolder(ImmersiveFarming.MOD_ID)
public final class IFFarmingGameTests {
    private static final String EMPTY = "empty";

    private IFFarmingGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_radius(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(8, 2, 8);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState().setValue(SprinklerBlock.ACTIVE, true));

        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(new BlockPos(14, 2, 8))),
                FarmingLogic.WATER_IRRIGATION, "regular sprinkler edge irrigation");
        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(new BlockPos(15, 2, 8))),
                FarmingLogic.NO_IRRIGATION, "regular sprinkler outside radius");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void high_pressure_sprinkler_radius(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(8, 2, 8);
        helper.setBlock(sprinkler, IFBlocks.HIGH_PRESSURE_SPRINKLER.get().defaultBlockState().setValue(SprinklerBlock.ACTIVE, true));

        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(new BlockPos(20, 2, 8))),
                FarmingLogic.WATER_IRRIGATION, "high-pressure sprinkler edge irrigation");
        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(new BlockPos(21, 2, 8))),
                FarmingLogic.NO_IRRIGATION, "high-pressure sprinkler outside radius");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_fluid_handler_respects_tank_bounds(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(4, 2, 4);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState());

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(sprinkler));
        helper.assertTrue(blockEntity instanceof SprinklerBlockEntity, "sprinkler should create a block entity");
        IFluidHandler handler = ((SprinklerBlockEntity) blockEntity).getFluidHandler(Direction.UP);

        handler.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(handler.getTanks(), 1, "sprinkler tank count");
        helper.assertValueEqual(handler.getTankCapacity(1), 0, "invalid tank capacity");
        helper.assertTrue(handler.getFluidInTank(1).isEmpty(), "invalid tank should be empty");
        helper.assertTrue(!handler.isFluidValid(1, new FluidStack(Fluids.WATER, 1)), "invalid tank should reject fluids");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_hammer_cycles_and_persists_fluid_faces(GameTestHelper helper) {
        resetConfig();
        BlockPos master = new BlockPos(4, 2, 4);
        BlockPos top = master.above();
        placeSprinklerStructure(helper, master, IFBlocks.SPRINKLER.get().defaultBlockState());

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(master));
        helper.assertTrue(blockEntity instanceof SprinklerBlockEntity, "sprinkler master should create a block entity");
        SprinklerBlockEntity sprinkler = (SprinklerBlockEntity) blockEntity;
        helper.assertValueEqual(sprinkler.getSideConfig(Direction.NORTH), IOSideConfig.INPUT,
                "sprinkler faces should default to fluid input");

        IFluidHandler input = sprinkler.getFluidHandler(Direction.NORTH);
        helper.assertTrue(input != null, "input face should expose a fluid handler");
        helper.assertValueEqual(input.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE),
                250, "input face should accept water");
        helper.assertTrue(input.drain(100, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "input face should reject extraction");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hammer = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("immersiveengineering", "hammer")));
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        BlockPos absoluteTop = helper.absolutePos(top);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteTop), Direction.NORTH, absoluteTop, false));

        helper.assertTrue(hammer.getItem().onItemUseFirst(hammer, context).consumesAction(),
                "engineer's hammer should react when the sprinkler top is clicked");
        helper.assertValueEqual(sprinkler.getSideConfig(Direction.NORTH), IOSideConfig.OUTPUT,
                "first hammer use should change input to output");
        IFluidHandler output = sprinkler.getFluidHandler(Direction.NORTH);
        helper.assertTrue(output != null, "output face should expose a fluid handler");
        helper.assertValueEqual(output.fill(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.EXECUTE),
                0, "output face should reject insertion");
        helper.assertValueEqual(output.drain(100, IFluidHandler.FluidAction.EXECUTE).getAmount(),
                100, "output face should allow extraction");

        helper.assertTrue(hammer.getItem().onItemUseFirst(hammer, context).consumesAction(),
                "second hammer use should react on the sprinkler top");
        helper.assertValueEqual(sprinkler.getSideConfig(Direction.NORTH), IOSideConfig.NONE,
                "second hammer use should disable the face");
        helper.assertTrue(sprinkler.getFluidHandler(Direction.NORTH) == null,
                "disabled face should not expose a fluid capability");

        CompoundTag saved = sprinkler.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(master), sprinkler.getBlockState(), saved,
                helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof SprinklerBlockEntity, "saved sprinkler should reload");
        helper.assertValueEqual(((SprinklerBlockEntity) loaded).getSideConfig(Direction.NORTH), IOSideConfig.NONE,
                "disabled fluid face should persist through reload");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_item_placement_creates_two_block_structure(GameTestHelper helper) {
        resetConfig();
        BlockPos support = new BlockPos(4, 1, 4);
        BlockPos master = support.above();
        BlockPos top = master.above();
        helper.setBlock(support, Blocks.STONE.defaultBlockState());

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(IFBlocks.SPRINKLER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absoluteSupport = helper.absolutePos(support);
        InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteSupport.above()), Direction.UP, absoluteSupport, false)));

        helper.assertTrue(result.consumesAction(), "sprinkler item placement should consume the use action");
        helper.assertBlockPresent(IFBlocks.SPRINKLER.get(), master);
        helper.assertBlockPresent(IFBlocks.SPRINKLER.get(), top);
        helper.assertBlockProperty(master, SprinklerBlock.SLAVE, false);
        helper.assertBlockProperty(top, SprinklerBlock.SLAVE, true);
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(master)) instanceof SprinklerBlockEntity,
                "sprinkler item placement should create a master block entity");
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(top)) == null,
                "sprinkler item placement should not create a top block entity");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_item_placement_fails_when_top_blocked(GameTestHelper helper) {
        resetConfig();
        BlockPos support = new BlockPos(4, 1, 4);
        BlockPos master = support.above();
        BlockPos blockedTop = master.above();
        helper.setBlock(support, Blocks.STONE.defaultBlockState());
        helper.setBlock(blockedTop, Blocks.STONE.defaultBlockState());

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(IFBlocks.SPRINKLER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absoluteSupport = helper.absolutePos(support);
        InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteSupport.above()), Direction.UP, absoluteSupport, false)));

        helper.assertTrue(!result.consumesAction(), "sprinkler placement should fail when the top block is occupied");
        helper.assertBlockPresent(Blocks.AIR, master);
        helper.assertBlockPresent(Blocks.STONE, blockedTop);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_rejects_mixed_fluids_until_empty(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(4, 2, 4);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState());

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(sprinkler));
        helper.assertTrue(blockEntity instanceof SprinklerBlockEntity, "sprinkler should create a block entity");
        IFluidHandler handler = ((SprinklerBlockEntity) blockEntity).getFluidHandler(null);

        helper.assertValueEqual(handler.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE),
                250, "sprinkler should accept water");
        helper.assertValueEqual(handler.fill(new FluidStack(IFFluids.TREATED_WATER.get(), 250), IFluidHandler.FluidAction.EXECUTE),
                0, "sprinkler should reject treated water while regular water is stored");
        helper.assertValueEqual(handler.drain(250, IFluidHandler.FluidAction.EXECUTE).getAmount(),
                250, "sprinkler should drain stored water");
        helper.assertValueEqual(handler.fill(new FluidStack(IFFluids.TREATED_WATER.get(), 250), IFluidHandler.FluidAction.EXECUTE),
                250, "sprinkler should accept treated water after emptying");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_multiblock_form_save_reload_break(GameTestHelper helper) {
        resetConfig();
        BlockPos master = new BlockPos(4, 2, 4);
        BlockPos top = master.above();
        placeSprinklerStructure(helper, master, IFBlocks.SPRINKLER.get().defaultBlockState());

        helper.assertBlockProperty(master, SprinklerBlock.SLAVE, false);
        helper.assertBlockProperty(top, SprinklerBlock.SLAVE, true);
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(top)) == null, "sprinkler slave should not create a block entity");

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(master));
        helper.assertTrue(blockEntity instanceof SprinklerBlockEntity, "sprinkler master should create a block entity");
        SprinklerBlockEntity sprinkler = (SprinklerBlockEntity) blockEntity;
        sprinkler.fillForTest(false, SprinklerBlockEntity.CAPACITY);

        helper.runAtTickTime(25, () -> {
            helper.assertBlockProperty(master, SprinklerBlock.ACTIVE, true);
            helper.assertBlockProperty(top, SprinklerBlock.ACTIVE, true);
            helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(master.offset(IFConfig.sprinklerRadius, 0, 0))),
                    FarmingLogic.WATER_IRRIGATION, "formed sprinkler should irrigate from its persisted master");

            CompoundTag saved = sprinkler.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(master), helper.getLevel().getBlockState(helper.absolutePos(master)),
                    saved, helper.getLevel().registryAccess());
            helper.assertTrue(loaded instanceof SprinklerBlockEntity, "saved sprinkler block entity should reload");

            helper.setBlock(top, Blocks.AIR.defaultBlockState());
            helper.assertBlockPresent(Blocks.AIR, master);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void high_pressure_sprinkler_multiblock_form_save_reload_break(GameTestHelper helper) {
        resetConfig();
        BlockPos master = new BlockPos(4, 2, 4);
        BlockPos top = master.above();
        placeSprinklerStructure(helper, master, IFBlocks.HIGH_PRESSURE_SPRINKLER.get().defaultBlockState());

        helper.assertBlockProperty(master, SprinklerBlock.SLAVE, false);
        helper.assertBlockProperty(top, SprinklerBlock.SLAVE, true);

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(master));
        helper.assertTrue(blockEntity instanceof SprinklerBlockEntity, "high-pressure sprinkler master should create a block entity");
        SprinklerBlockEntity sprinkler = (SprinklerBlockEntity) blockEntity;
        sprinkler.fillForTest(true, SprinklerBlockEntity.CAPACITY);

        helper.runAtTickTime(25, () -> {
            helper.assertBlockProperty(master, SprinklerBlock.ACTIVE, true);
            helper.assertBlockProperty(top, SprinklerBlock.USING_TREATED_WATER, true);
            helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(master.offset(IFConfig.highPressureSprinklerRadius, 0, 0))),
                    FarmingLogic.TREATED_IRRIGATION, "formed high-pressure sprinkler should carry treated-water irrigation");

            CompoundTag saved = sprinkler.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(master), helper.getLevel().getBlockState(helper.absolutePos(master)),
                    saved, helper.getLevel().registryAccess());
            helper.assertTrue(loaded instanceof SprinklerBlockEntity, "saved high-pressure sprinkler block entity should reload");

            helper.setBlock(master, Blocks.AIR.defaultBlockState());
            helper.assertBlockPresent(Blocks.AIR, top);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void sprinkler_slave_break_drops_only_slave(GameTestHelper helper) {
        resetConfig();
        BlockPos master = new BlockPos(4, 2, 4);
        BlockPos top = master.above();
        placeSprinklerStructure(helper, master, IFBlocks.SPRINKLER.get().defaultBlockState());

        helper.assertBlockProperty(master, SprinklerBlock.SLAVE, false);
        helper.assertBlockProperty(top, SprinklerBlock.SLAVE, true);

        BlockPos absoluteTop = helper.absolutePos(top);
        AABB dropSearch = new AABB(
                absoluteTop.getX() - 2.0D,
                absoluteTop.getY() - 1.0D,
                absoluteTop.getZ() - 2.0D,
                absoluteTop.getX() + 3.0D,
                absoluteTop.getY() + 3.0D,
                absoluteTop.getZ() + 3.0D
        );
        int previousDropCount = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropSearch).size();

        helper.getLevel().destroyBlock(absoluteTop, true);
        helper.runAfterDelay(2, () -> {
            int currentDropCount = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropSearch).size();
            helper.assertValueEqual(currentDropCount - previousDropCount, 1, "breaking sprinkler slave should drop only one block item");
            helper.assertBlockPresent(Blocks.AIR, top);
            helper.assertBlockPresent(Blocks.AIR, master);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void crop_disease_spread(GameTestHelper helper) {
        resetConfig();
        IFConfig.proximityDiseaseChance = 1.0D;
        IFConfig.diseaseLethalityChance = 0.0D;

        BlockPos sick = new BlockPos(4, 2, 4);
        BlockPos neighbor = new BlockPos(5, 2, 4);
        helper.setBlock(sick, diseasedSoil(true));
        helper.setBlock(neighbor, diseasedSoil(false));
        helper.setBlock(sick.above(), Blocks.WHEAT.defaultBlockState());
        helper.setBlock(neighbor.above(), Blocks.WHEAT.defaultBlockState());

        helper.randomTick(sick);
        helper.assertBlockProperty(neighbor, FertileSoilBlock.DISEASED, true);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dry_soil_under_stems_does_not_decay(GameTestHelper helper) {
        resetConfig();
        BlockPos stemSoil = new BlockPos(4, 2, 4);
        BlockPos attachedStemSoil = new BlockPos(6, 2, 4);
        helper.setBlock(stemSoil, diseasedSoil(false).setValue(FertileSoilBlock.MOISTURE, 0));
        helper.setBlock(attachedStemSoil, diseasedSoil(false).setValue(FertileSoilBlock.MOISTURE, 0));
        helper.setBlock(stemSoil.above(), Blocks.PUMPKIN_STEM.defaultBlockState());
        helper.setBlock(attachedStemSoil.above(), Blocks.ATTACHED_PUMPKIN_STEM.defaultBlockState());

        helper.randomTick(stemSoil);
        helper.randomTick(attachedStemSoil);

        helper.assertBlockPresent(IFBlocks.SOIL.get(), stemSoil);
        helper.assertBlockPresent(IFBlocks.SOIL.get(), attachedStemSoil);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void treated_water_disease_cure(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(8, 2, 8);
        BlockPos soil = new BlockPos(10, 2, 8);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState()
                .setValue(SprinklerBlock.ACTIVE, true)
                .setValue(SprinklerBlock.USING_TREATED_WATER, true));
        helper.setBlock(soil, diseasedSoil(true));
        helper.setBlock(soil.above(), Blocks.WHEAT.defaultBlockState());

        helper.randomTick(soil);
        helper.assertBlockProperty(soil, FertileSoilBlock.DISEASED, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void placed_treated_water_disease_cure(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        helper.setBlock(soil, diseasedSoil(true));
        helper.setBlock(soil.above(), Blocks.WHEAT.defaultBlockState());
        helper.setBlock(soil.east(), IFBlocks.TREATED_WATER.get().defaultBlockState());

        helper.randomTick(soil);
        helper.assertBlockProperty(soil, FertileSoilBlock.DISEASED, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void treated_water_overrides_regular_sprinkler_irrigation(GameTestHelper helper) {
        resetConfig();
        BlockPos sprinkler = new BlockPos(8, 2, 8);
        BlockPos soil = new BlockPos(10, 2, 8);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState().setValue(SprinklerBlock.ACTIVE, true));
        helper.setBlock(soil, diseasedSoil(true));
        helper.setBlock(soil.above(), Blocks.WHEAT.defaultBlockState());
        helper.setBlock(soil.east(), IFBlocks.TREATED_WATER.get().defaultBlockState());

        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(soil)),
                FarmingLogic.TREATED_IRRIGATION, "treated water should outrank regular sprinkler water");
        helper.randomTick(soil);
        helper.assertBlockProperty(soil, FertileSoilBlock.DISEASED, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void treated_water_overrides_nearby_water(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        helper.setBlock(soil.offset(-1, 0, -1), Blocks.WATER.defaultBlockState());
        helper.setBlock(soil.offset(1, 1, 1), IFBlocks.TREATED_WATER.get().defaultBlockState());

        helper.assertValueEqual(FarmingLogic.irrigationAt(helper.getLevel(), helper.absolutePos(soil)),
                FarmingLogic.TREATED_IRRIGATION, "treated water should outrank nearby water");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lethal_disease_replaces_soil_after_hydration(GameTestHelper helper) {
        resetConfig();
        IFConfig.diseaseLethalityChance = 1.0D;
        BlockPos sprinkler = new BlockPos(8, 2, 8);
        BlockPos soil = new BlockPos(10, 2, 8);
        helper.setBlock(sprinkler, IFBlocks.SPRINKLER.get().defaultBlockState().setValue(SprinklerBlock.ACTIVE, true));
        helper.setBlock(soil, diseasedSoil(true).setValue(FertileSoilBlock.MOISTURE, 0));
        helper.setBlock(soil.above(), Blocks.WHEAT.defaultBlockState());

        helper.randomTick(soil);
        helper.assertBlockPresent(Blocks.DIRT, soil);
        helper.assertBlockPresent(IFBlocks.DEAD_CROP.get(), soil.above());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void compost_growth_boost(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        helper.setBlock(soil, diseasedSoil(false).setValue(FertileSoilBlock.FERTILITY, 0));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(IFItems.COMPOST.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(soil);
        IFItems.COMPOST.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        IFItems.COMPOST.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));

        helper.assertBlockProperty(soil, FertileSoilBlock.FERTILITY, 2);
        helper.assertTrue(stack.isEmpty(), "two compost uses should consume two compost items");
        helper.assertTrue(IFConfig.compostGrowthMultiplier > 1.0D, "compost growth multiplier should boost growth");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void compost_fertility_decrements_after_growth(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        BlockPos crop = soil.above();
        helper.setBlock(soil, diseasedSoil(false).setValue(FertileSoilBlock.FERTILITY, 2));
        helper.setBlock(crop, Blocks.WHEAT.defaultBlockState());

        CropGrowEvent.Post event = new CropGrowEvent.Post(helper.getLevel(), helper.absolutePos(crop),
                Blocks.WHEAT.defaultBlockState(), Blocks.WHEAT.defaultBlockState());
        new FarmingEvents().onCropGrowPost(event);

        helper.assertBlockProperty(soil, FertileSoilBlock.FERTILITY, 1);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_multiblock_form_break(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        BlockPos master = origin.offset(ComposterMultiblock.MASTER_OFFSET);
        BlockPos clickedSlave = origin.offset(1, 0, 1);
        placeComposterStructure(helper, origin);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("immersiveengineering", "hammer"))));
        BlockPos absoluteClickedSlave = helper.absolutePos(clickedSlave);
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND,
                absoluteClickedSlave, new BlockHitResult(Vec3.atCenterOf(absoluteClickedSlave), Direction.UP, absoluteClickedSlave, false));
        new FarmingEvents().onRightClickBlock(event);

        helper.assertTrue(event.isCanceled(), "engineer's hammer should form the original composter structure");
        helper.assertBlockProperty(origin, IndustrialComposterBlock.FORMED, true);
        helper.assertBlockProperty(origin, IndustrialComposterBlock.SLAVE, true);
        helper.assertBlockProperty(origin, IndustrialComposterBlock.PART, 0);
        helper.assertBlockProperty(master, IndustrialComposterBlock.SLAVE, false);
        helper.assertBlockProperty(master, IndustrialComposterBlock.PART, ComposterMultiblock.partForOffset(ComposterMultiblock.MASTER_OFFSET));
        helper.assertBlockProperty(clickedSlave, IndustrialComposterBlock.SLAVE, true);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(origin.offset(0, 2, 2))).isAir(),
                "explicit air spaces in the upstream structure should stay empty after formation");

        helper.setBlock(clickedSlave, Blocks.AIR.defaultBlockState());
        assertOriginalComposterBlock(helper, origin, new BlockPos(0, 0, 0));
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_rejects_vanilla_composter_placeholder(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        BlockPos replacedRequiredBlock = origin.offset(0, 1, 0);
        placeComposterStructure(helper, origin);
        helper.setBlock(replacedRequiredBlock, Blocks.COMPOSTER.defaultBlockState());

        helper.assertTrue(!ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(replacedRequiredBlock)),
                "vanilla composter must not satisfy any industrial composter template position");
        helper.assertBlockPresent(Blocks.COMPOSTER, replacedRequiredBlock);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_internal_block_has_no_placeable_item(GameTestHelper helper) {
        helper.assertTrue(IFBlocks.COMPOSTER.get().asItem() == Items.AIR,
                "formed composter block must stay internal and not create a one-block machine item");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ie_manual_composter_multiblock_registered(GameTestHelper helper) {
        helper.assertTrue(MultiblockHandler.getByUniqueName(IFIEMultiblocks.COMPOSTER_ID) != null,
                "IE manual multiblock id immersive_farming_mod_porting:multiblocks/composter should be registered");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_multiblock_templates_load(GameTestHelper helper) {
        for (String path : new String[] { "cookpot", "food_fermenter", "food_processor", "grill_oven" }) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("immersivecooking", "multiblocks/" + path);
            helper.assertTrue(helper.getLevel().getStructureManager().get(id).isPresent(),
                    "IE template " + id + " should load from data/immersivecooking/structure");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_powered_multiblocks_accept_external_energy(GameTestHelper helper) {
        assertPoweredMultiblockAcceptsEnergy(helper, CookpotMultiblock.INSTANCE, new BlockPos(1, 2, 1), "Cookpot");
        assertPoweredMultiblockAcceptsEnergy(helper, FoodFermenterMultiblock.INSTANCE, new BlockPos(6, 2, 1),
                "Food Fermenter");
        assertPoweredMultiblockAcceptsEnergy(helper, FoodProcessorMultiblock.INSTANCE, new BlockPos(11, 2, 1),
                "Food Processor");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_has_distinct_active_sound_event(GameTestHelper helper) {
        ResourceLocation processorSound = ResourceLocation.fromNamespaceAndPath("immersivecooking", "block.food_processor.active");
        ResourceLocation fermenterSound = ResourceLocation.fromNamespaceAndPath("immersivecooking", "block.food_fermenter.active");

        helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(processorSound),
                "Food Processor should register its own active sound event");
        helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(fermenterSound),
                "Food Fermenter active sound event should remain registered");
        helper.assertTrue(!processorSound.equals(fermenterSound),
                "Food Processor active sound event should not reuse the Food Fermenter id");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grill_oven_smoking_provider_finds_vanilla_recipes(GameTestHelper helper) {
        helper.assertTrue(new SmokingRecipeProvider().findRecipe(new ItemStack(Items.BEEF), helper.getLevel()).isPresent(),
                "Grill Oven provider should find the vanilla beef smoking recipe");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_registers_default_recipe_provider(GameTestHelper helper) {
        try {
            Field providersField = ICMultiblockLogic.class.getDeclaredField("recipeProviders");
            providersField.setAccessible(true);
            List<?> providers = (List<?>) providersField.get(ICContent.Multiblock.FOOD_PROCESSOR.logic());
            helper.assertTrue(providers.stream().anyMatch(DefaultFoodProcessorRecipeProvider.class::isInstance),
                    "Food Processor logic should register its default recipe provider");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not inspect Food Processor recipe providers", e);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_recipe_matches_required_item_amounts(GameTestHelper helper) {
        NonNullList<IngredientWithSize> inputs = NonNullList.create();
        inputs.add(new IngredientWithSize(Ingredient.of(Items.APPLE), 2));
        FoodProcessorRecipe recipe = new FoodProcessorRecipe(inputs, null, new ItemStack(Items.SUGAR), 20, 10);
        SimpleContainer container = new SimpleContainer(8);

        container.setItem(0, new ItemStack(Items.APPLE));
        helper.assertTrue(!recipe.matches(container, helper.getLevel()),
                "Food Processor recipe should reject incomplete item amounts");

        container.setItem(1, new ItemStack(Items.APPLE));
        helper.assertTrue(recipe.matches(container, helper.getLevel()),
                "Food Processor recipe should match required item amounts across slots");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_machine_recipes_reject_empty_inputs(GameTestHelper helper) {
        assertThrowsIllegalArgument(helper, () -> new CookpotRecipe(
                        NonNullList.create(), new ItemStack(Items.MUSHROOM_STEW), new ItemStack(Items.BOWL), 20, 10),
                "Cookpot recipe should reject empty inputs");
        assertThrowsIllegalArgument(helper, () -> new FoodProcessorRecipe(
                        NonNullList.create(), null, new ItemStack(Items.SUGAR), 20, 10),
                "Food Processor recipe should reject empty inputs");
        assertThrowsIllegalArgument(helper, () -> new FoodFermenterRecipe(
                        NonNullList.create(), null, new ItemStack(Items.SUGAR), ItemStack.EMPTY, 20, 10),
                "Food Fermenter recipe should reject empty inputs");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_machine_recipes_reject_invalid_outputs_and_costs(GameTestHelper helper) {
        assertThrowsIllegalArgument(helper, () -> new CookpotRecipe(
                        appleInputs(1), ItemStack.EMPTY, new ItemStack(Items.BOWL), 20, 10),
                "Cookpot recipe should reject empty results");
        assertThrowsIllegalArgument(helper, () -> new FoodProcessorRecipe(
                        appleInputs(1), null, new ItemStack(Items.SUGAR), 0, 10),
                "Food Processor recipe should reject nonpositive time");
        assertThrowsIllegalArgument(helper, () -> new FoodFermenterRecipe(
                        appleInputs(1), null, new ItemStack(Items.SUGAR), ItemStack.EMPTY, 20, 0),
                "Food Fermenter recipe should reject nonpositive energy");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void food_fermenter_no_container_recipe_starts_without_container_stack(GameTestHelper helper) {
        FoodFermenterRecipe recipe = new FoodFermenterRecipe(
                appleInputs(1), null, new ItemStack(Items.SUGAR), ItemStack.EMPTY, 20, 10);
        RecipeHolder<FoodFermenterRecipe> holder = new RecipeHolder<>(
                testId("food_fermenter_no_container_process"), recipe);
        DefaultFoodFermenterRecipeProvider provider = new DefaultFoodFermenterRecipeProvider() {
            @Override
            public List<RecipeHolder<FoodFermenterRecipe>> getAllRecipes(Level level) {
                return List.of(holder);
            }
        };
        FoodFermenterLogic logic = (FoodFermenterLogic) ICContent.Multiblock.FOOD_FERMENTER.logic();
        List providers = reflectedRecipeProviders(logic);
        List originalProviders = List.copyOf(providers);

        providers.clear();
        providers.add(provider);
        try {
            FoodFermenterLogic.State state = new FoodFermenterLogic.State(testMultiblockContext(helper));
            state.getInventory().setStackInSlot(0, new ItemStack(Items.APPLE));
            helper.assertTrue(state.getEnergy().receiveEnergy(1000, false) > 0,
                    "Food Fermenter test state should accept energy");

            invokeFoodFermenterEnqueue(logic, state, helper.getLevel());

            helper.assertTrue(state.getProcess() != null,
                    "Food Fermenter no-container recipe should queue without a container stack");
            helper.assertTrue(state.getInventory().getStackInSlot(0).isEmpty(),
                    "Food Fermenter no-container recipe should consume its input item");
            helper.assertTrue(state.getInventory().getStackInSlot(FoodFermenterLogic.INPUT_CONTAINER_SLOT).isEmpty(),
                    "Food Fermenter no-container recipe should leave the container slot empty");
        } finally {
            providers.clear();
            providers.addAll(originalProviders);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void cookpot_provider_recipe_input_matches_all_slots(GameTestHelper helper) {
        NonNullList<IngredientWithSize> inputs = NonNullList.create();
        inputs.add(new IngredientWithSize(Ingredient.of(Items.APPLE), 2));
        CookpotRecipe recipe = new CookpotRecipe(inputs, new ItemStack(Items.MUSHROOM_STEW), new ItemStack(Items.BOWL), 20, 10);
        RecipeHolder<CookpotRecipe> holder = new RecipeHolder<>(testId("cookpot_provider_recipe_input"), recipe);
        DefaultCookpotRecipeProvider provider = new DefaultCookpotRecipeProvider() {
            @Override
            public List<RecipeHolder<CookpotRecipe>> getAllRecipes(Level level) {
                return List.of(holder);
            }
        };

        helper.assertTrue(provider.findRecipe(recipeInput(6, new ItemStack(Items.APPLE), new ItemStack(Items.APPLE)),
                        helper.getLevel()).isPresent(),
                "Cookpot provider should match RecipeInput ingredients across all machine slots");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_fermenter_provider_recipe_input_matches_all_slots_without_fluid(GameTestHelper helper) {
        NonNullList<IngredientWithSize> inputs = NonNullList.create();
        inputs.add(new IngredientWithSize(Ingredient.of(Items.APPLE), 2));
        FoodFermenterRecipe recipe = new FoodFermenterRecipe(
                inputs, null, new ItemStack(Items.SUGAR), new ItemStack(Items.GLASS_BOTTLE), 20, 10);
        RecipeHolder<FoodFermenterRecipe> holder = new RecipeHolder<>(
                testId("food_fermenter_provider_recipe_input"), recipe);
        DefaultFoodFermenterRecipeProvider provider = new DefaultFoodFermenterRecipeProvider() {
            @Override
            public List<RecipeHolder<FoodFermenterRecipe>> getAllRecipes(Level level) {
                return List.of(holder);
            }
        };

        helper.assertTrue(provider.findRecipe(recipeInput(6, new ItemStack(Items.APPLE), new ItemStack(Items.APPLE)),
                        FluidStack.EMPTY, helper.getLevel()).isPresent(),
                "Food Fermenter fluid-aware provider lookup should match no-fluid recipes across all slots");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_provider_recipe_input_matches_all_slots(GameTestHelper helper) {
        NonNullList<IngredientWithSize> inputs = NonNullList.create();
        inputs.add(new IngredientWithSize(Ingredient.of(Items.APPLE), 2));
        FoodProcessorRecipe recipe = new FoodProcessorRecipe(inputs, null, new ItemStack(Items.SUGAR), 20, 10);
        RecipeHolder<FoodProcessorRecipe> holder = new RecipeHolder<>(
                testId("food_processor_provider_recipe_input"), recipe);
        DefaultFoodProcessorRecipeProvider provider = new DefaultFoodProcessorRecipeProvider() {
            @Override
            public List<RecipeHolder<FoodProcessorRecipe>> getAllRecipes(Level level) {
                return List.of(holder);
            }
        };

        helper.assertTrue(provider.findRecipe(recipeInput(8, new ItemStack(Items.APPLE), new ItemStack(Items.APPLE)),
                        FluidStack.EMPTY, helper.getLevel()).isPresent(),
                "Food Processor provider should match RecipeInput ingredients across all machine slots");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_default_recipes_load(GameTestHelper helper) {
        DefaultFoodProcessorRecipeProvider provider = new DefaultFoodProcessorRecipeProvider();
        List<ResourceLocation> expected = List.of(
                ResourceLocation.fromNamespaceAndPath("immersivecooking", "food_processor/mushroom_stew"),
                ResourceLocation.fromNamespaceAndPath("immersivecooking", "food_processor/pumpkin_pie"),
                ResourceLocation.fromNamespaceAndPath("immersivecooking", "food_processor/wheat_dough"));
        List<RecipeHolder<FoodProcessorRecipe>> recipes = provider.getAllRecipes(helper.getLevel());
        List<ResourceLocation> ids = recipes.stream().map(RecipeHolder::id).toList();

        for (ResourceLocation id : expected) {
            helper.assertTrue(ids.contains(id), "Food Processor default recipe should load: " + id);
        }
        helper.assertTrue(recipes.stream().anyMatch(holder -> holder.value().fluidInput != null),
                "Food Processor should include at least one fluid-assisted default recipe");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_default_provider_finds_packaged_recipes(GameTestHelper helper) {
        DefaultFoodProcessorRecipeProvider provider = new DefaultFoodProcessorRecipeProvider();
        RecipeHolder<FoodProcessorRecipe> pumpkinPie = provider.findRecipe(
                        recipeInput(8, new ItemStack(Items.PUMPKIN), new ItemStack(Items.SUGAR), new ItemStack(Items.EGG)),
                        helper.getLevel())
                .orElseThrow(() -> new AssertionError("Food Processor should find packaged pumpkin pie recipe"));
        helper.assertValueEqual(pumpkinPie.id(),
                ResourceLocation.fromNamespaceAndPath("immersivecooking", "food_processor/pumpkin_pie"),
                "Food Processor dry recipe id");

        RecipeHolder<FoodProcessorRecipe> bread = provider.findRecipe(
                        recipeInput(8, new ItemStack(Items.WHEAT, 3)),
                        new FluidStack(Fluids.WATER, 250),
                        helper.getLevel())
                .orElseThrow(() -> new AssertionError("Food Processor should find packaged water-assisted bread recipe"));
        helper.assertValueEqual(bread.id(),
                ResourceLocation.fromNamespaceAndPath("immersivecooking", "food_processor/wheat_dough"),
                "Food Processor fluid recipe id");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_fluid_tags_use_common_namespace(GameTestHelper helper) {
        for (TagKey<Fluid> tag : ICTags.Fluids.ALL_FLUID_TAGS) {
            helper.assertValueEqual(tag.location().getNamespace(), "c",
                    "Immersive Cooking fluid tag namespace for " + tag.location());
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_fermenter_external_fluid_container_slots_are_directional(GameTestHelper helper) {
        FoodFermenterLogic.State state = new FoodFermenterLogic.State(testMultiblockContext(helper));
        IItemHandler inputHandler = reflectedItemHandler(state, "itemInputHandler");
        IItemHandler outputHandler = reflectedItemHandler(state, "itemOutputHandler");
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);

        helper.assertTrue(inputHandler.isItemValid(FoodFermenterLogic.FILLED_FLUID_SLOT, waterBucket),
                "Food Fermenter automation should accept filled fluid containers in the filled input slot");
        helper.assertTrue(!inputHandler.isItemValid(FoodFermenterLogic.EMPTY_FLUID_SLOT, waterBucket),
                "Food Fermenter automation should not insert into the empty-container output slot");

        ItemStack remainder = inputHandler.insertItem(FoodFermenterLogic.FILLED_FLUID_SLOT, waterBucket.copy(), false);
        helper.assertTrue(remainder.isEmpty(), "Food Fermenter filled fluid slot should accept a water bucket");
        helper.assertTrue(state.getInventory().getStackInSlot(FoodFermenterLogic.FILLED_FLUID_SLOT).is(Items.WATER_BUCKET),
                "Food Fermenter water bucket should land in the filled fluid slot");
        helper.assertTrue(outputHandler.extractItem(FoodFermenterLogic.FILLED_FLUID_SLOT, 1, true).isEmpty(),
                "Food Fermenter automation should not extract from the filled fluid input slot");

        state.getInventory().setStackInSlot(FoodFermenterLogic.EMPTY_FLUID_SLOT, new ItemStack(Items.BUCKET));
        helper.assertTrue(outputHandler.extractItem(FoodFermenterLogic.EMPTY_FLUID_SLOT, 1, true).is(Items.BUCKET),
                "Food Fermenter automation should extract empty containers from the empty output slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void food_processor_external_fluid_container_slots_are_directional(GameTestHelper helper) {
        FoodProcessorLogic.State state = new FoodProcessorLogic.State(testMultiblockContext(helper));
        IItemHandler inputHandler = reflectedItemHandler(state, "itemInputHandler");
        IItemHandler outputHandler = reflectedItemHandler(state, "itemOutputHandler");
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);

        helper.assertTrue(inputHandler.isItemValid(FoodProcessorLogic.FILLED_FLUID_SLOT, waterBucket),
                "Food Processor automation should accept filled fluid containers in the filled input slot");
        helper.assertTrue(!inputHandler.isItemValid(FoodProcessorLogic.EMPTY_FLUID_SLOT, waterBucket),
                "Food Processor automation should not insert into the empty-container output slot");

        ItemStack remainder = inputHandler.insertItem(FoodProcessorLogic.FILLED_FLUID_SLOT, waterBucket.copy(), false);
        helper.assertTrue(remainder.isEmpty(), "Food Processor filled fluid slot should accept a water bucket");
        helper.assertTrue(state.getInventory().getStackInSlot(FoodProcessorLogic.FILLED_FLUID_SLOT).is(Items.WATER_BUCKET),
                "Food Processor water bucket should land in the filled fluid slot");
        helper.assertTrue(outputHandler.extractItem(FoodProcessorLogic.FILLED_FLUID_SLOT, 1, true).isEmpty(),
                "Food Processor automation should not extract from the filled fluid input slot");

        state.getInventory().setStackInSlot(FoodProcessorLogic.EMPTY_FLUID_SLOT, new ItemStack(Items.BUCKET));
        helper.assertTrue(outputHandler.extractItem(FoodProcessorLogic.EMPTY_FLUID_SLOT, 1, true).is(Items.BUCKET),
                "Food Processor automation should extract empty containers from the empty output slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_recipe_resources_use_1_21_paths(GameTestHelper helper) {
        for (String path : new String[] {
                "compat/farm_and_charm/barley",
                "compat/farmersdelight/food_processor/assembly/bacon_sandwich",
                "compat/farmersdelight/food_processor/assembly/pie_crust",
                "compat/farmersdelight/food_processor/cutting/minced_beef",
                "compat/farmersdelight/food_processor/wheat_dough",
                "compat/farmersdelight/food_fermenting/rich_soil",
                "compat/vinery/food_fermenting/red_wine"
        }) {
            ResourceLocation singular = ResourceLocation.fromNamespaceAndPath("immersivecooking", "recipe/" + path + ".json");
            ResourceLocation plural = ResourceLocation.fromNamespaceAndPath("immersivecooking", "recipes/" + path + ".json");
            helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(singular).isPresent(),
                    "recipe resource " + singular + " should load from data/immersivecooking/recipe");
            helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(plural).isEmpty(),
                    "legacy recipe resource " + plural + " should not remain packaged");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void immersive_cooking_tag_resources_use_1_21_paths(GameTestHelper helper) {
        ResourceLocation commonFluidTag = ResourceLocation.fromNamespaceAndPath("c", "tags/fluid/apple_juice.json");
        ResourceLocation legacyForgeFluidTag = ResourceLocation.fromNamespaceAndPath("forge", "tags/fluids/apple_juice.json");
        ResourceLocation cookingFluidTag = ResourceLocation.fromNamespaceAndPath("immersivecooking", "tags/fluid/apple_juice.json");
        ResourceLocation legacyCookingFluidTag = ResourceLocation.fromNamespaceAndPath("immersivecooking", "tags/fluids/apple_juice.json");
        ResourceLocation cookingItemTag = ResourceLocation.fromNamespaceAndPath("immersivecooking", "tags/item/juice_ingredients/apple_juice.json");
        ResourceLocation legacyCookingItemTag = ResourceLocation.fromNamespaceAndPath("immersivecooking", "tags/items/juice_ingredients/apple_juice.json");

        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(commonFluidTag).isPresent(),
                "common fluid tag " + commonFluidTag + " should load from data/c/tags/fluid");
        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(legacyForgeFluidTag).isEmpty(),
                "legacy common fluid tag " + legacyForgeFluidTag + " should not remain packaged");
        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(cookingFluidTag).isPresent(),
                "Immersive Cooking fluid tag " + cookingFluidTag + " should load from data/immersivecooking/tags/fluid");
        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(legacyCookingFluidTag).isEmpty(),
                "legacy Immersive Cooking fluid tag " + legacyCookingFluidTag + " should not remain packaged");
        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(cookingItemTag).isPresent(),
                "Immersive Cooking item tag " + cookingItemTag + " should load from data/immersivecooking/tags/item");
        helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(legacyCookingItemTag).isEmpty(),
                "legacy Immersive Cooking item tag " + legacyCookingItemTag + " should not remain packaged");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_break_does_not_clear_adjacent_structure(GameTestHelper helper) {
        resetConfig();
        BlockPos firstOrigin = new BlockPos(2, 2, 2);
        BlockPos secondOrigin = firstOrigin.offset(ComposterMultiblock.WIDTH, 0, 0);
        BlockPos brokenSlave = firstOrigin.offset(1, 0, 1);
        placeComposterStructure(helper, firstOrigin);
        placeComposterStructure(helper, secondOrigin);

        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(firstOrigin)), "first composter should form");
        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(secondOrigin)), "second composter should form");

        helper.setBlock(brokenSlave, Blocks.AIR.defaultBlockState());
        assertOriginalComposterBlock(helper, firstOrigin, new BlockPos(0, 0, 0));
        helper.assertBlockProperty(secondOrigin, IndustrialComposterBlock.FORMED, true);
        helper.assertBlockProperty(secondOrigin.offset(1, 0, 1), IndustrialComposterBlock.FORMED, true);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_multiblock_save_reload_break(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        placeComposterStructure(helper, origin);

        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(origin)), "composter should form before save");
        for (BlockPos offset : ComposterMultiblock.templateOffsets()) {
            BlockPos pos = origin.offset(offset);
            if (ComposterMultiblock.originalStateForOffset(offset).isAir()) {
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(), "template air should remain air");
                continue;
            }
            helper.assertBlockProperty(pos, IndustrialComposterBlock.FORMED, true);
            helper.assertBlockProperty(pos, IndustrialComposterBlock.PART, ComposterMultiblock.partForOffset(offset));
        }

        BlockPos master = origin.offset(ComposterMultiblock.MASTER_OFFSET);
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(master));
        helper.assertTrue(blockEntity instanceof IndustrialComposterBlockEntity, "composter master block entity should exist");
        CompoundTag saved = blockEntity.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(master), helper.getLevel().getBlockState(helper.absolutePos(master)),
                saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof IndustrialComposterBlockEntity, "saved composter block entity should reload");

        BlockPos brokenSlave = origin.offset(1, 0, 1);
        helper.setBlock(brokenSlave, Blocks.AIR.defaultBlockState());
        assertOriginalComposterBlock(helper, origin, new BlockPos(0, 0, 0));
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_slave_capabilities_resolve_to_master(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        placeComposterStructure(helper, origin);
        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(origin)), "composter should form before capability checks");

        BlockPos masterPos = origin.offset(ComposterMultiblock.MASTER_OFFSET);
        BlockPos slavePos = origin.offset(1, 0, 1);
        BlockEntity masterBlockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(masterPos));
        BlockEntity slaveBlockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(slavePos));
        helper.assertTrue(masterBlockEntity instanceof IndustrialComposterBlockEntity, "master composter block entity should exist");
        helper.assertTrue(slaveBlockEntity instanceof IndustrialComposterBlockEntity, "slave composter block entity should exist");

        IndustrialComposterBlockEntity master = (IndustrialComposterBlockEntity) masterBlockEntity;
        IndustrialComposterBlockEntity slave = (IndustrialComposterBlockEntity) slaveBlockEntity;

        IEnergyStorage energyStorage = slave.getEnergyStorage(Direction.UP);
        helper.assertTrue(energyStorage != null, "slave composter should expose master energy");
        helper.assertValueEqual(energyStorage.receiveEnergy(100, false), 100, "slave energy insert should be accepted by master");
        helper.assertValueEqual(master.getEnergyForTest(), 100, "master should store energy inserted through slave");

        IFluidHandler fluidHandler = slave.getFluidHandler(Direction.NORTH);
        helper.assertTrue(fluidHandler != null, "slave composter should expose master fluids");
        helper.assertValueEqual(fluidHandler.fill(new FluidStack(Fluids.WATER, 25), IFluidHandler.FluidAction.EXECUTE),
                25, "slave fluid fill should be accepted by master");
        helper.assertValueEqual(master.getTankAmountForTest(0), 25, "master should store water inserted through slave");

        IItemHandler itemHandler = slave.getItemHandler(Direction.UP);
        helper.assertTrue(itemHandler != null, "slave composter should expose master items");
        ItemStack remainder = itemHandler.insertItem(IndustrialComposterBlockEntity.INPUT_SLOT, new ItemStack(Items.WHEAT), false);
        helper.assertTrue(remainder.isEmpty(), "slave item insert should be accepted by master");
        helper.assertTrue(master.getItemHandler(Direction.UP).getStackInSlot(IndustrialComposterBlockEntity.INPUT_SLOT).is(Items.WHEAT),
                "master should store item inserted through slave");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_break_drops_master_input(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        IndustrialComposterBlockEntity composter = setupFormedComposterWithEnergy(helper, origin, 0);
        IItemHandler itemHandler = composter.getItemHandler(Direction.UP);
        helper.assertTrue(itemHandler != null, "formed composter should expose item handler");
        ItemStack remainder = itemHandler.insertItem(IndustrialComposterBlockEntity.INPUT_SLOT, new ItemStack(Items.WHEAT), false);
        helper.assertTrue(remainder.isEmpty(), "formed composter should accept input before breaking");

        BlockPos master = origin.offset(ComposterMultiblock.MASTER_OFFSET);
        helper.setBlock(master, Blocks.AIR.defaultBlockState());

        helper.assertItemEntityPresent(Items.WHEAT, master, 2.0D);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 160)
    public static void composter_processes_fluid_recipe(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        IndustrialComposterBlockEntity composter = setupFormedComposterForCompost(helper, origin, 500);

        helper.runAfterDelay(120, () -> {
            helper.assertValueEqual(composter.getTankAmountForTest(0), 0, "composter should consume water");
            helper.assertValueEqual(composter.getTankAmountForTest(1), 0, "composter should consume green material");
            helper.assertValueEqual(composter.getTankAmountForTest(2), 0, "composter should consume brown material");
            helper.assertTrue(composter.getEnergyForTest() < 500, "composter should consume energy");
            helper.assertItemEntityPresent(IFItems.COMPOST.get(), origin.offset(1, ComposterMultiblock.HEIGHT, 1), 3.0D);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void composter_processes_log_into_brown_material(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        IndustrialComposterBlockEntity composter = setupFormedComposterWithEnergy(helper, origin, 50);
        IItemHandler itemHandler = composter.getItemHandler(Direction.UP);
        helper.assertTrue(itemHandler != null, "formed composter should expose item handler");
        ItemStack remainder = itemHandler.insertItem(IndustrialComposterBlockEntity.INPUT_SLOT, new ItemStack(Items.OAK_LOG), false);
        helper.assertTrue(remainder.isEmpty(), "formed composter should accept a log input");

        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(composter.getTankAmountForTest(2), 50, "logs should produce brown material");
            helper.assertTrue(itemHandler.getStackInSlot(IndustrialComposterBlockEntity.INPUT_SLOT).isEmpty(),
                    "log input should be consumed");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void composter_processes_seed_into_green_material(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        IndustrialComposterBlockEntity composter = setupFormedComposterWithEnergy(helper, origin, 50);
        IItemHandler itemHandler = composter.getItemHandler(Direction.UP);
        helper.assertTrue(itemHandler != null, "formed composter should expose item handler");
        ItemStack remainder = itemHandler.insertItem(IndustrialComposterBlockEntity.INPUT_SLOT, new ItemStack(Items.WHEAT_SEEDS), false);
        helper.assertTrue(remainder.isEmpty(), "formed composter should accept a seed input");

        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(composter.getTankAmountForTest(1), 10, "seeds should produce green material");
            helper.assertTrue(itemHandler.getStackInSlot(IndustrialComposterBlockEntity.INPUT_SLOT).isEmpty(),
                    "seed input should be consumed");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 160)
    public static void composter_processing_save_reload(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        IndustrialComposterBlockEntity composter = setupFormedComposterForCompost(helper, origin, 500);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(composter.getProcessTimeForTest() > 0, "composter should have active processing progress before save");
            CompoundTag saved = composter.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(origin), helper.getLevel().getBlockState(helper.absolutePos(origin)),
                    saved, helper.getLevel().registryAccess());
            helper.assertTrue(loaded instanceof IndustrialComposterBlockEntity, "saved processing composter should reload");
            helper.assertTrue(((IndustrialComposterBlockEntity) loaded).getProcessTimeForTest() > 0, "processing progress should persist");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void composter_menu_quick_move_rejects_invalid_slot_indices(GameTestHelper helper) {
        resetConfig();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        IndustrialComposterBlockEntity composter = new IndustrialComposterBlockEntity(
                helper.absolutePos(new BlockPos(1, 2, 1)),
                IFBlocks.COMPOSTER.get().defaultBlockState());
        ComposterMenu menu = new ComposterMenu(1, player.getInventory(), composter);

        helper.assertTrue(menu.quickMoveStack(player, -1).isEmpty(), "negative slot quick move should be ignored");
        helper.assertTrue(menu.quickMoveStack(player, 999).isEmpty(), "oversized slot quick move should be ignored");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_recipe_rejects_nonpositive_energy_and_fluids(GameTestHelper helper) {
        resetConfig();
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);

        helper.assertTrue(IFRecipeSerializers.COMPOSTER.get().codec().codec()
                        .parse(ops, composterRecipeJson(0, 10)).result().isEmpty(),
                "composter recipe codec should reject nonpositive energy");
        helper.assertTrue(IFRecipeSerializers.COMPOSTER.get().codec().codec()
                        .parse(ops, composterRecipeJson(500, 0)).result().isEmpty(),
                "composter recipe codec should reject nonpositive fluid amounts");
        assertThrowsIllegalArgument(helper, () -> new ComposterRecipe(
                false,
                Optional.empty(),
                FluidStack.EMPTY,
                FluidStack.EMPTY,
                Optional.of(fluidIngredient("water", 10)),
                Optional.of(fluidIngredient("wet_matter_fluid", 10)),
                Optional.of(fluidIngredient("dry_matter_fluid", 10)),
                new ItemStack(IFItems.COMPOST.get()),
                0
        ), "direct composter recipe construction should reject nonpositive energy");
        assertThrowsIllegalArgument(helper, () -> fluidIngredient("water", 0),
                "direct fluid ingredient construction should reject nonpositive amounts");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void offhand_hoe_tills_soil(GameTestHelper helper) {
        resetConfig();
        BlockPos dirt = new BlockPos(2, 2, 2);
        helper.setBlock(dirt, Blocks.DIRT.defaultBlockState());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND_HOE));
        BlockPos absolute = helper.absolutePos(dirt);

        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.OFF_HAND, absolute,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        new FarmingEvents().onRightClickBlock(event);

        helper.assertTrue(event.isCanceled(), "hoe tilling should consume the right-click event");
        helper.assertBlockPresent(IFBlocks.SOIL.get(), dirt);
        helper.assertBlockProperty(dirt, FertileSoilBlock.TILL, 4);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hoe_tilling_requires_air_above(GameTestHelper helper) {
        resetConfig();
        BlockPos dirt = new BlockPos(2, 2, 2);
        helper.setBlock(dirt, Blocks.DIRT.defaultBlockState());
        helper.setBlock(dirt.above(), Blocks.STONE.defaultBlockState());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
        BlockPos absolute = helper.absolutePos(dirt);

        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, absolute,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        new FarmingEvents().onRightClickBlock(event);

        helper.assertTrue(!event.isCanceled(), "hoe tilling should not consume the event when the block above is occupied");
        helper.assertBlockPresent(Blocks.DIRT, dirt);
        helper.assertBlockPresent(Blocks.STONE, dirt.above());
        helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 0, "failed tilling should not damage the hoe");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hoe_tilling_rejects_down_face(GameTestHelper helper) {
        resetConfig();
        BlockPos dirt = new BlockPos(2, 2, 2);
        helper.setBlock(dirt, Blocks.DIRT.defaultBlockState());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
        BlockPos absolute = helper.absolutePos(dirt);

        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, absolute,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.DOWN, absolute, false));
        new FarmingEvents().onRightClickBlock(event);

        helper.assertTrue(!event.isCanceled(), "hoe tilling should not consume the event when clicking the bottom face");
        helper.assertBlockPresent(Blocks.DIRT, dirt);
        helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 0, "failed tilling should not damage the hoe");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void incomplete_till_blocks_crop_growth(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        BlockPos crop = soil.above();
        helper.setBlock(soil, diseasedSoil(false).setValue(FertileSoilBlock.TILL, 0));
        helper.setBlock(crop, Blocks.WHEAT.defaultBlockState());

        CropGrowEvent.Pre event = new CropGrowEvent.Pre(helper.getLevel(), helper.absolutePos(crop), Blocks.WHEAT.defaultBlockState());
        new FarmingEvents().onCropGrowPre(event);

        helper.assertTrue(event.getResult() == CropGrowEvent.Pre.Result.DO_NOT_GROW, "incomplete till should block crop growth");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void farmland_cannot_be_trampled(GameTestHelper helper) {
        resetConfig();
        BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(
                helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), Blocks.FARMLAND.defaultBlockState(), 1.0F, null);
        new FarmingEvents().onFarmlandTrample(event);
        helper.assertTrue(event.isCanceled(), "farmland trample event should be canceled");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bone_meal_blocked_if_config_enabled(GameTestHelper helper) {
        resetConfig();
        BonemealEvent event = new BonemealEvent(null, helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)),
                Blocks.WHEAT.defaultBlockState(), new ItemStack(Items.BONE_MEAL));
        new FarmingEvents().onBoneMeal(event);
        helper.assertTrue(event.isCanceled(), "bone meal on crops should be canceled");
        helper.succeed();
    }

    private static void resetConfig() {
        IFConfig.enableDisease = true;
        IFConfig.enableFarmlandTrampleProtection = true;
        IFConfig.disableVanillaBoneMealOnCrops = true;
        IFConfig.sprinklerRadius = 6;
        IFConfig.highPressureSprinklerRadius = 12;
        IFConfig.compostGrowthMultiplier = 1.5D;
        IFConfig.astikorReduxIntegration = true;
        IFConfig.startDiseaseChance = 0.0D;
        IFConfig.proximityDiseaseChance = 0.0D;
        IFConfig.diseaseLethalityChance = 0.0D;
    }

    @SuppressWarnings("unchecked")
    private static <S extends IMultiblockState> IInitialMultiblockContext<S> testMultiblockContext(GameTestHelper helper) {
        Supplier<Level> levelSupplier = helper::getLevel;
        Runnable noop = () -> {
        };
        Supplier<Object> emptyCapability = () -> null;

        return (IInitialMultiblockContext<S>) Proxy.newProxyInstance(
                IInitialMultiblockContext.class.getClassLoader(),
                new Class<?>[] { IInitialMultiblockContext.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "levelSupplier" -> levelSupplier;
                    case "getMarkDirtyRunnable", "getSyncRunnable", "getBlockUpdateRunnable" -> noop;
                    case "getCapabilityAt", "getVoidCapabilityAt" -> emptyCapability;
                    case "toString" -> "test multiblock context";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length == 1 && proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unhandled test context method: " + method);
                });
    }

    private static IItemHandler reflectedItemHandler(Object state, String fieldName) {
        try {
            Field field = state.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (IItemHandler) field.get(state);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not inspect " + state.getClass().getSimpleName() + "." + fieldName, e);
        }
    }

    private static ResourceLocation testId(String path) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveFarming.MOD_ID, "test/" + path);
    }

    private static NonNullList<IngredientWithSize> appleInputs(int count) {
        NonNullList<IngredientWithSize> inputs = NonNullList.create();
        inputs.add(new IngredientWithSize(Ingredient.of(Items.APPLE), count));
        return inputs;
    }

    private static List<?> reflectedRecipeProviders(ICMultiblockLogic<?, ?> logic) {
        try {
            Field field = ICMultiblockLogic.class.getDeclaredField("recipeProviders");
            field.setAccessible(true);
            return (List<?>) field.get(logic);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not inspect recipe providers", e);
        }
    }

    private static void assertPoweredMultiblockAcceptsEnergy(GameTestHelper helper, TemplateMultiblock multiblock,
            BlockPos origin, String machineName) {
        for (var blockInfo : multiblock.getStructure(helper.getLevel())) {
            helper.setBlock(origin.offset(blockInfo.pos()), blockInfo.state());
        }

        BlockPos trigger = origin.offset(multiblock.getTriggerOffset());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        boolean formed = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (multiblock.createStructure(helper.getLevel(), helper.absolutePos(trigger), direction, player)) {
                formed = true;
                break;
            }
        }
        helper.assertTrue(formed, machineName + " test structure should form");

        IEnergyStorage energy = null;
        for (BlockPos local : BlockPos.betweenClosed(origin, origin.offset(2, 2, 2))) {
            BlockPos absolute = helper.absolutePos(local);
            for (Direction side : Direction.values()) {
                energy = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, absolute, side);
                if (energy != null) {
                    break;
                }
            }
            if (energy != null) {
                break;
            }
        }

        helper.assertTrue(energy != null, machineName + " should expose an external energy capability");
        helper.assertTrue(energy.receiveEnergy(1000, false) > 0,
                machineName + " external energy capability should accept power");
    }

    private static void invokeFoodFermenterEnqueue(FoodFermenterLogic logic, FoodFermenterLogic.State state,
            Level level) {
        try {
            Method method = FoodFermenterLogic.class.getDeclaredMethod("enqueueProcesses",
                    FoodFermenterLogic.State.class, Level.class);
            method.setAccessible(true);
            method.invoke(logic, state, level);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not invoke Food Fermenter process enqueue", e);
        }
    }

    private static ComposterRecipe.FluidIngredient fluidIngredient(String tagPath, int amount) {
        String namespace = tagPath.equals("water") ? "minecraft" : ImmersiveFarming.MOD_ID;
        return new ComposterRecipe.FluidIngredient(
                TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, tagPath)),
                amount);
    }

    private static JsonObject composterRecipeJson(int energy, int fluidAmount) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("fluidProduct", false);
        recipe.add("result", itemStackJson(ImmersiveFarming.MOD_ID + ":compost", 1));
        recipe.add("input0", fluidIngredientJson("minecraft:water", fluidAmount));
        recipe.add("input1", fluidIngredientJson(ImmersiveFarming.MOD_ID + ":wet_matter_fluid", 10));
        recipe.add("input2", fluidIngredientJson(ImmersiveFarming.MOD_ID + ":dry_matter_fluid", 10));
        recipe.addProperty("energy", energy);
        return recipe;
    }

    private static JsonObject fluidIngredientJson(String tag, int amount) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", tag);
        ingredient.addProperty("amount", amount);
        return ingredient;
    }

    private static JsonObject itemStackJson(String id, int count) {
        JsonObject stack = new JsonObject();
        stack.addProperty("id", id);
        stack.addProperty("count", count);
        return stack;
    }

    private static void assertThrowsIllegalArgument(GameTestHelper helper, Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        helper.fail(message);
    }

    private static RecipeInput recipeInput(int slots, ItemStack... stacks) {
        ItemStackHandler handler = new ItemStackHandler(slots);
        for (int slot = 0; slot < stacks.length; slot++) {
            handler.setStackInSlot(slot, stacks[slot]);
        }
        return new RecipeWrapper(handler);
    }

    private static net.minecraft.world.level.block.state.BlockState diseasedSoil(boolean diseased) {
        return IFBlocks.SOIL.get().defaultBlockState()
                .setValue(FertileSoilBlock.MOISTURE, 7)
                .setValue(FertileSoilBlock.TILL, FertileSoilBlock.TILL_MAX)
                .setValue(FertileSoilBlock.DISEASED, diseased);
    }

    private static void placeComposterStructure(GameTestHelper helper, BlockPos origin) {
        for (BlockPos offset : ComposterMultiblock.templateOffsets()) {
            helper.setBlock(origin.offset(offset), ComposterMultiblock.originalStateForOffset(offset));
        }
    }

    private static void assertOriginalComposterBlock(GameTestHelper helper, BlockPos origin, BlockPos offset) {
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(origin.offset(offset)))
                        .is(ComposterMultiblock.originalStateForOffset(offset).getBlock()),
                "composter should restore original template block at " + offset);
    }

    private static IndustrialComposterBlockEntity setupFormedComposterForCompost(GameTestHelper helper, BlockPos origin, int energy) {
        IndustrialComposterBlockEntity composter = setupFormedComposterWithEnergy(helper, origin, energy);

        IFluidHandler fluidHandler = composter.getFluidHandler(Direction.NORTH);
        helper.assertTrue(fluidHandler != null, "formed composter should expose fluids");
        helper.assertValueEqual(fluidHandler.fill(new FluidStack(Fluids.WATER, 10), IFluidHandler.FluidAction.EXECUTE), 10, "composter water fill");
        helper.assertValueEqual(fluidHandler.fill(new FluidStack(IFFluids.WET_MATTER.get(), 10), IFluidHandler.FluidAction.EXECUTE), 10, "composter green material fill");
        helper.assertValueEqual(fluidHandler.fill(new FluidStack(IFFluids.DRY_MATTER.get(), 10), IFluidHandler.FluidAction.EXECUTE), 10, "composter brown material fill");

        IItemHandler itemHandler = composter.getItemHandler(Direction.UP);
        helper.assertTrue(itemHandler != null, "formed composter should expose item handler");
        return composter;
    }

    private static IndustrialComposterBlockEntity setupFormedComposterWithEnergy(GameTestHelper helper, BlockPos origin, int energy) {
        placeComposterStructure(helper, origin);
        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(origin)), "composter should form before processing");
        BlockPos master = origin.offset(ComposterMultiblock.MASTER_OFFSET);
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(master));
        helper.assertTrue(blockEntity instanceof IndustrialComposterBlockEntity, "composter master block entity should exist");
        IndustrialComposterBlockEntity composter = (IndustrialComposterBlockEntity) blockEntity;

        IEnergyStorage energyStorage = composter.getEnergyStorage(Direction.UP);
        helper.assertTrue(energyStorage != null, "formed composter should expose energy");
        int inserted = 0;
        while (inserted < energy) {
            int accepted = energyStorage.receiveEnergy(energy - inserted, false);
            helper.assertTrue(accepted > 0, "formed composter should accept energy");
            inserted += accepted;
        }
        return composter;
    }

    private static void placeSprinklerStructure(GameTestHelper helper, BlockPos master, net.minecraft.world.level.block.state.BlockState state) {
        BlockPos absoluteMaster = helper.absolutePos(master);
        helper.getLevel().setBlock(absoluteMaster, state, 3);
        ((SprinklerBlock) state.getBlock()).setPlacedBy(helper.getLevel(), absoluteMaster, state, null, ItemStack.EMPTY);
    }
}
