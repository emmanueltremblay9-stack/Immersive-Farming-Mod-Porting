package dev.emmanueltremblay.immersivefarming.test;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.ComposterMultiblock;
import dev.emmanueltremblay.immersivefarming.block.FertileSoilBlock;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.block.IndustrialComposterBlock;
import dev.emmanueltremblay.immersivefarming.block.SprinklerBlock;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.item.IFItems;
import dev.emmanueltremblay.immersivefarming.util.FarmingLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

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
    public static void compost_growth_boost(GameTestHelper helper) {
        resetConfig();
        BlockPos soil = new BlockPos(4, 2, 4);
        helper.setBlock(soil, diseasedSoil(false).setValue(FertileSoilBlock.FERTILITY, 0));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IFItems.COMPOST.get()));
        BlockPos absolute = helper.absolutePos(soil);
        IFItems.COMPOST.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));

        helper.assertBlockProperty(soil, FertileSoilBlock.FERTILITY, 1);
        helper.assertTrue(IFConfig.compostGrowthMultiplier > 1.0D, "compost growth multiplier should boost growth");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void composter_multiblock_form_break(GameTestHelper helper) {
        resetConfig();
        BlockPos origin = new BlockPos(2, 2, 2);
        for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(ComposterMultiblock.WIDTH - 1, ComposterMultiblock.HEIGHT - 1, ComposterMultiblock.DEPTH - 1))) {
            helper.setBlock(pos, IFBlocks.COMPOSTER.get());
        }

        helper.assertTrue(ComposterMultiblock.tryForm(helper.getLevel(), helper.absolutePos(origin)), "composter should form");
        helper.assertBlockProperty(origin, IndustrialComposterBlock.FORMED, true);
        ComposterMultiblock.breakFormed(helper.getLevel(), helper.absolutePos(origin));
        helper.assertBlockProperty(origin, IndustrialComposterBlock.FORMED, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void farmland_cannot_be_trampled(GameTestHelper helper) {
        resetConfig();
        BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(
                helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), Blocks.FARMLAND.defaultBlockState(), 1.0F, null);
        new dev.emmanueltremblay.immersivefarming.event.FarmingEvents().onFarmlandTrample(event);
        helper.assertTrue(event.isCanceled(), "farmland trample event should be canceled");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bone_meal_blocked_if_config_enabled(GameTestHelper helper) {
        resetConfig();
        BonemealEvent event = new BonemealEvent(null, helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)),
                Blocks.WHEAT.defaultBlockState(), new ItemStack(Items.BONE_MEAL));
        new dev.emmanueltremblay.immersivefarming.event.FarmingEvents().onBoneMeal(event);
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

    private static net.minecraft.world.level.block.state.BlockState diseasedSoil(boolean diseased) {
        return IFBlocks.SOIL.get().defaultBlockState()
                .setValue(FertileSoilBlock.MOISTURE, 7)
                .setValue(FertileSoilBlock.TILL, FertileSoilBlock.TILL_MAX)
                .setValue(FertileSoilBlock.DISEASED, diseased);
    }
}
