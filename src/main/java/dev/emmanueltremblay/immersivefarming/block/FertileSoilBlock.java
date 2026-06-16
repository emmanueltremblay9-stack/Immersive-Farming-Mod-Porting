package dev.emmanueltremblay.immersivefarming.block;

import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.util.FarmingLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FertileSoilBlock extends FarmBlock {
    public static final int TILL_MAX = 7;
    public static final IntegerProperty TILL = IntegerProperty.create("till", 0, TILL_MAX);
    public static final int FERTILITY_MAX = 2;
    public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 0, FERTILITY_MAX);
    public static final BooleanProperty DISEASED = BooleanProperty.create("diseased");

    public FertileSoilBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(MOISTURE, 0)
                .setValue(TILL, 0)
                .setValue(FERTILITY, 0)
                .setValue(DISEASED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TILL, FERTILITY, DISEASED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int irrigation = FarmingLogic.irrigationAt(level, pos);
        boolean hydrated = irrigation > 0 || level.isRainingAt(pos.above());
        int moisture = state.getValue(MOISTURE);
        BlockState next = state;

        if (hydrated && moisture < 7) {
            next = next.setValue(MOISTURE, 7);
        } else if (!hydrated && moisture > 0) {
            next = next.setValue(MOISTURE, moisture - 1);
        } else if (!hydrated && !isUnderCrop(level, pos)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
            return;
        }

        if (IFConfig.enableDisease) {
            next = updateDisease(next, level, pos, random, irrigation);
        } else if (next.getValue(DISEASED)) {
            next = next.setValue(DISEASED, false);
        }

        if (next != state) {
            level.setBlock(pos, next, 2);
        }
    }

    private BlockState updateDisease(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, int irrigation) {
        boolean diseased = state.getValue(DISEASED);
        boolean underCrop = isUnderCrop(level, pos);

        if (irrigation == FarmingLogic.TREATED_IRRIGATION) {
            return diseased ? state.setValue(DISEASED, false) : state;
        }
        if (!underCrop) {
            return diseased ? state.setValue(DISEASED, false) : state;
        }
        if (!diseased && random.nextDouble() < IFConfig.startDiseaseChance) {
            return state.setValue(DISEASED, true);
        }
        if (diseased && random.nextDouble() < IFConfig.diseaseLethalityChance) {
            level.setBlock(pos.above(), IFBlocks.DEAD_CROP.get().defaultBlockState(), 3);
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
            return state;
        }
        if (diseased) {
            for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
                if (neighbor.equals(pos)) {
                    continue;
                }
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.is(this) && isUnderCrop(level, neighbor)
                        && !neighborState.getValue(DISEASED)
                        && random.nextDouble() < IFConfig.proximityDiseaseChance) {
                    level.setBlock(neighbor, neighborState.setValue(DISEASED, true), 2);
                }
            }
        }
        return state;
    }

    public static boolean isUnderCrop(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof CropBlock;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }

    public static boolean canSustainCrop(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FertileSoilBlock && state.getValue(TILL) >= TILL_MAX;
    }
}
