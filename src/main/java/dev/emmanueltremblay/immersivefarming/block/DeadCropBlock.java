package dev.emmanueltremblay.immersivefarming.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DeadCropBlock extends BushBlock {
    public static final MapCodec<DeadCropBlock> CODEC = simpleCodec(DeadCropBlock::new);

    public DeadCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND) || state.is(IFBlocks.SOIL.get());
    }
}
