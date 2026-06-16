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

    public static boolean tryForm(Level level, BlockPos clickedPos) {
        BlockPos origin = findOrigin(level, clickedPos);
        if (origin == null) {
            return false;
        }
        for (BlockPos pos : positions(origin)) {
            boolean slave = !pos.equals(origin);
            BlockState state = level.getBlockState(pos)
                    .setValue(IndustrialComposterBlock.FORMED, true)
                    .setValue(IndustrialComposterBlock.SLAVE, slave);
            level.setBlock(pos, state, 3);
        }
        return true;
    }

    private static BlockPos findOrigin(Level level, BlockPos clickedPos) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    BlockPos origin = clickedPos.offset(-x, -y, -z);
                    if (isComplete(level, origin)) {
                        return origin;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isComplete(Level level, BlockPos origin) {
        for (BlockPos pos : positions(origin)) {
            if (!level.getBlockState(pos).is(IFBlocks.COMPOSTER.get())) {
                return false;
            }
        }
        return true;
    }

    public static void breakFormed(Level level, BlockPos brokenPos, BlockState brokenState) {
        BlockPos origin = brokenState.getValue(IndustrialComposterBlock.SLAVE) ? findFormedOrigin(level, brokenPos) : brokenPos;
        if (origin == null) {
            return;
        }
        for (BlockPos pos : positions(origin)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(IFBlocks.COMPOSTER.get()) && state.getValue(IndustrialComposterBlock.FORMED)) {
                level.setBlock(pos, state.setValue(IndustrialComposterBlock.FORMED, false).setValue(IndustrialComposterBlock.SLAVE, false), 3);
            }
        }
    }

    private static BlockPos findFormedOrigin(Level level, BlockPos memberPos) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    BlockPos origin = memberPos.offset(-x, -y, -z);
                    if (isFormedStructureExcept(level, origin, memberPos)) {
                        return origin;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isFormedStructureExcept(Level level, BlockPos origin, BlockPos missingPos) {
        for (BlockPos pos : positions(origin)) {
            if (pos.equals(missingPos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(IFBlocks.COMPOSTER.get()) || !state.getValue(IndustrialComposterBlock.FORMED)) {
                return false;
            }
            boolean expectedSlave = !pos.equals(origin);
            if (state.getValue(IndustrialComposterBlock.SLAVE) != expectedSlave) {
                return false;
            }
        }
        return true;
    }

    private static Iterable<BlockPos> positions(BlockPos origin) {
        return BlockPos.betweenClosed(origin, origin.offset(WIDTH - 1, HEIGHT - 1, DEPTH - 1));
    }
}
