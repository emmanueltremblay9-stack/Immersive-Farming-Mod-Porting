package dev.emmanueltremblay.immersivefarming.block;

import com.mojang.serialization.MapCodec;
import dev.emmanueltremblay.immersivefarming.block.entity.IndustrialComposterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class IndustrialComposterBlock extends BaseEntityBlock {
    public static final MapCodec<IndustrialComposterBlock> CODEC = simpleCodec(IndustrialComposterBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty SLAVE = BooleanProperty.create("slave");

    public IndustrialComposterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(SLAVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED, SLAVE);
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(FORMED)) {
            player.displayClientMessage(Component.translatable("message.immersivefarming.composter_already_formed"), true);
            return InteractionResult.CONSUME;
        }
        boolean formed = ComposterMultiblock.tryForm(level, pos);
        player.displayClientMessage(Component.translatable(formed
                ? "message.immersivefarming.composter_formed"
                : "message.immersivefarming.composter_incomplete"), true);
        return formed ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getValue(FORMED) && !state.is(newState.getBlock())) {
            ComposterMultiblock.breakFormed(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
