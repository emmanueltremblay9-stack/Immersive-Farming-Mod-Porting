package dev.emmanueltremblay.immersivefarming.block;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerBlockInteraction;
import com.mojang.serialization.MapCodec;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class SprinklerBlock extends BaseEntityBlock implements IHammerBlockInteraction {
    public static final MapCodec<SprinklerBlock> REGULAR_CODEC = simpleCodec(properties -> new SprinklerBlock(false, properties));
    public static final MapCodec<SprinklerBlock> HIGH_PRESSURE_CODEC = simpleCodec(properties -> new SprinklerBlock(true, properties));
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty USING_TREATED_WATER = BooleanProperty.create("using_treated_water");
    public static final BooleanProperty SLAVE = BooleanProperty.create("slave");

    private final boolean highPressure;

    public SprinklerBlock(boolean highPressure, Properties properties) {
        super(properties);
        this.highPressure = highPressure;
        registerDefaultState(stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(USING_TREATED_WATER, false)
                .setValue(SLAVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return highPressure ? HIGH_PRESSURE_CODEC : REGULAR_CODEC;
    }

    public boolean isHighPressure() {
        return highPressure;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ACTIVE, USING_TREATED_WATER, SLAVE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos topPos = context.getClickedPos().above();
        if (topPos.getY() >= context.getLevel().getMaxBuildHeight()) {
            return null;
        }
        BlockState topState = context.getLevel().getBlockState(topPos);
        return topState.canBeReplaced(context) ? defaultBlockState() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && !state.getValue(SLAVE)) {
            BlockState topState = state
                    .setValue(SLAVE, true)
                    .setValue(ACTIVE, false)
                    .setValue(USING_TREATED_WATER, false);
            level.setBlock(pos.above(), topState, 3);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(SLAVE)) {
            return null;
        }
        return new SprinklerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(SLAVE)) {
            return null;
        }
        BlockEntityType<SprinklerBlockEntity> expected = highPressure
                ? IFBlockEntities.HIGH_PRESSURE_SPRINKLER.get()
                : IFBlockEntities.SPRINKLER.get();
        return createTickerHelper(type, expected, level.isClientSide()
                ? SprinklerBlockEntity::clientTick
                : SprinklerBlockEntity::serverTick);
    }

    @Override
    public InteractionResult useHammer(BlockState state, Level level, BlockPos pos, Player player, UseOnContext context) {
        if (!state.getValue(SLAVE)) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos.below());
        if (!(blockEntity instanceof SprinklerBlockEntity sprinkler)) {
            return InteractionResult.FAIL;
        }
        Direction side = player != null && player.isShiftKeyDown()
                ? context.getClickedFace().getOpposite()
                : context.getClickedFace();
        return sprinkler.toggleSide(side, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(SLAVE)) {
            if (direction == Direction.DOWN && (!neighborState.is(this) || neighborState.getValue(SLAVE))) {
                return Blocks.AIR.defaultBlockState();
            }
        } else if (direction == Direction.UP && (!neighborState.is(this) || !neighborState.getValue(SLAVE))) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            BlockPos counterpart = state.getValue(SLAVE) ? pos.below() : pos.above();
            BlockState counterpartState = level.getBlockState(counterpart);
            if (counterpartState.is(this) && counterpartState.getValue(SLAVE) != state.getValue(SLAVE)) {
                level.destroyBlock(counterpart, state.getValue(SLAVE));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
