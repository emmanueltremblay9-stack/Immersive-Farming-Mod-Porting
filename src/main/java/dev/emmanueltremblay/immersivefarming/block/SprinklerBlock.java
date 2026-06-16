package dev.emmanueltremblay.immersivefarming.block;

import com.mojang.serialization.MapCodec;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class SprinklerBlock extends BaseEntityBlock {
    public static final MapCodec<SprinklerBlock> CODEC = simpleCodec(properties -> new SprinklerBlock(false, properties));
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty USING_TREATED_WATER = BooleanProperty.create("using_treated_water");

    private final boolean highPressure;

    public SprinklerBlock(boolean highPressure, Properties properties) {
        super(properties);
        this.highPressure = highPressure;
        registerDefaultState(stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(USING_TREATED_WATER, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public boolean isHighPressure() {
        return highPressure;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ACTIVE, USING_TREATED_WATER);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        BlockEntityType<SprinklerBlockEntity> expected = highPressure
                ? IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get()
                : IFBlockEntities.SPRINKLER.get();
        return createTickerHelper(type, expected, SprinklerBlockEntity::serverTick);
    }
}
