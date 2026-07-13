package dev.emmanueltremblay.immersivefarming.block;

import com.mojang.serialization.MapCodec;
import dev.emmanueltremblay.immersivefarming.block.entity.IndustrialComposterBlockEntity;
import dev.emmanueltremblay.immersivefarming.menu.ComposterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class IndustrialComposterBlock extends BaseEntityBlock {
    public static final MapCodec<IndustrialComposterBlock> CODEC = simpleCodec(IndustrialComposterBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, ComposterMultiblock.PART_COUNT - 1);

    public IndustrialComposterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(SLAVE, false)
                .setValue(PART, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED, SLAVE, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialComposterBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, IFBlockEntities.COMPOSTER.get(), IndustrialComposterBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(FORMED)) {
            return InteractionResult.PASS;
        }
        BlockPos masterPos = ComposterMultiblock.masterPosFromPart(pos, state);
        if (masterPos == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(masterPos);
            if (blockEntity instanceof IndustrialComposterBlockEntity composter && composter.isFormedMaster()) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (containerId, playerInventory, menuPlayer) -> new ComposterMenu(containerId, playerInventory, composter),
                                Component.translatable("block.immersive_farming_mod_porting.composter")
                        ),
                        buffer -> buffer.writeBlockPos(masterPos)
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getValue(FORMED) && !state.is(newState.getBlock())) {
            dropFormedMasterContents(level, pos, state);
            ComposterMultiblock.breakFormed(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void dropFormedMasterContents(Level level, BlockPos pos, BlockState state) {
        BlockPos masterPos = ComposterMultiblock.masterPosFromPart(pos, state);
        if (masterPos == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(masterPos);
        if (blockEntity instanceof IndustrialComposterBlockEntity composter && composter.isFormedMaster()) {
            composter.dropContents();
        }
    }
}
