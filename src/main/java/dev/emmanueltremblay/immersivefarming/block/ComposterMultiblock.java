package dev.emmanueltremblay.immersivefarming.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ComposterMultiblock {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 2;
    public static final int DEPTH = 3;

    private ComposterMultiblock() {
    }

    public static boolean tryForm(Level level, BlockPos origin) {
        BlockPos min = origin;
        for (BlockPos pos : positions(min)) {
            if (!level.getBlockState(pos).is(IFBlocks.COMPOSTER.get())) {
                return false;
            }
        }
        for (BlockPos pos : positions(min)) {
            boolean slave = !pos.equals(origin);
            BlockState state = level.getBlockState(pos)
                    .setValue(IndustrialComposterBlock.FORMED, true)
                    .setValue(IndustrialComposterBlock.SLAVE, slave);
            level.setBlock(pos, state, 3);
        }
        return true;
    }

    public static void breakFormed(Level level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-WIDTH, -HEIGHT, -DEPTH), origin.offset(WIDTH, HEIGHT, DEPTH))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(IFBlocks.COMPOSTER.get()) && state.getValue(IndustrialComposterBlock.FORMED)) {
                level.setBlock(pos, state.setValue(IndustrialComposterBlock.FORMED, false).setValue(IndustrialComposterBlock.SLAVE, false), 3);
            }
        }
    }

    private static Iterable<BlockPos> positions(BlockPos origin) {
        return BlockPos.betweenClosed(origin, origin.offset(WIDTH - 1, HEIGHT - 1, DEPTH - 1));
    }
}
