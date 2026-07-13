package dev.emmanueltremblay.immersivefarming.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public final class ComposterMultiblock {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 4;
    public static final int DEPTH = 3;
    public static final int PART_COUNT = WIDTH * HEIGHT * DEPTH;
    public static final BlockPos MASTER_OFFSET = new BlockPos(0, 1, 2);
    private static final ResourceLocation AIR = ResourceLocation.withDefaultNamespace("air");
    private static final TemplatePart[] TEMPLATE_PARTS = new TemplatePart[]{
            part(0, 0, 0, "immersiveengineering:steel_scaffolding_standard"),
            part(0, 0, 1, "immersiveengineering:steel_scaffolding_standard"),
            part(0, 0, 2, "immersiveengineering:light_engineering"),
            part(0, 1, 0, "immersiveengineering:sheetmetal_iron"),
            part(0, 1, 1, "immersiveengineering:sheetmetal_iron"),
            part(0, 1, 2, "immersiveengineering:light_engineering"),
            part(0, 2, 0, "immersiveengineering:sheetmetal_iron"),
            part(0, 2, 1, "immersiveengineering:sheetmetal_iron"),
            part(0, 2, 2, "minecraft:air"),
            part(1, 0, 0, "immersiveengineering:steel_scaffolding_standard"),
            part(1, 0, 1, "immersiveengineering:steel_scaffolding_standard"),
            part(1, 0, 2, "immersiveengineering:light_engineering"),
            part(1, 1, 0, "immersiveengineering:sheetmetal_iron"),
            part(1, 1, 1, "immersiveengineering:sheetmetal_iron"),
            part(1, 1, 2, "immersiveengineering:fluid_pipe"),
            part(1, 2, 0, "immersiveengineering:sheetmetal_iron"),
            part(1, 2, 1, "immersiveengineering:sheetmetal_iron"),
            part(1, 2, 2, "immersiveengineering:fluid_pipe"),
            part(1, 3, 1, "immersiveengineering:fluid_pipe"),
            part(1, 3, 2, "immersiveengineering:fluid_pipe"),
            part(2, 0, 0, "immersiveengineering:steel_scaffolding_standard"),
            part(2, 0, 1, "immersiveengineering:steel_scaffolding_standard"),
            part(2, 0, 2, "immersiveengineering:steel_scaffolding_standard"),
            part(2, 1, 0, "immersiveengineering:sheetmetal_iron"),
            part(2, 1, 1, "immersiveengineering:sheetmetal_iron"),
            part(2, 1, 2, "immersiveengineering:rs_engineering"),
            part(2, 2, 0, "immersiveengineering:sheetmetal_iron"),
            part(2, 2, 1, "immersiveengineering:sheetmetal_iron"),
            part(2, 2, 2, "minecraft:air")
    };

    private ComposterMultiblock() {
    }

    public static boolean tryForm(Level level, BlockPos clickedPos) {
        BlockPos origin = findOrigin(level, clickedPos);
        if (origin == null) {
            return false;
        }
        for (TemplatePart templatePart : TEMPLATE_PARTS) {
            if (templatePart.isAir()) {
                continue;
            }
            BlockPos pos = origin.offset(templatePart.offset());
            int part = partForOffset(templatePart.offset());
            boolean slave = !templatePart.offset().equals(MASTER_OFFSET);
            BlockState state = IFBlocks.COMPOSTER.get().defaultBlockState()
                    .setValue(IndustrialComposterBlock.FORMED, true)
                    .setValue(IndustrialComposterBlock.SLAVE, slave)
                    .setValue(IndustrialComposterBlock.PART, part);
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
        for (TemplatePart part : TEMPLATE_PARTS) {
            if (!matchesOriginal(level.getBlockState(origin.offset(part.offset())), part)) {
                return false;
            }
        }
        return true;
    }

    public static void breakFormed(Level level, BlockPos brokenPos, BlockState brokenState) {
        BlockPos origin = originFromPart(brokenPos, brokenState);
        if (origin == null) {
            origin = findFormedOrigin(level, brokenPos);
        }
        if (origin == null) {
            return;
        }
        for (TemplatePart part : TEMPLATE_PARTS) {
            BlockPos pos = origin.offset(part.offset());
            if (pos.equals(brokenPos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(IFBlocks.COMPOSTER.get())
                    && state.hasProperty(IndustrialComposterBlock.FORMED)
                    && state.getValue(IndustrialComposterBlock.FORMED)) {
                level.setBlock(pos, originalState(part), 3);
            }
        }
    }

    public static BlockPos originFromPart(BlockPos memberPos, BlockState memberState) {
        if (!memberState.hasProperty(IndustrialComposterBlock.PART)) {
            return null;
        }
        int part = memberState.getValue(IndustrialComposterBlock.PART);
        return memberPos.subtract(offsetForPart(part));
    }

    public static BlockPos masterPosFromPart(BlockPos memberPos, BlockState memberState) {
        BlockPos origin = originFromPart(memberPos, memberState);
        return origin == null ? null : origin.offset(MASTER_OFFSET);
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
        for (TemplatePart part : TEMPLATE_PARTS) {
            BlockPos pos = origin.offset(part.offset());
            if (pos.equals(missingPos)) {
                continue;
            }
            if (part.isAir()) {
                if (!level.getBlockState(pos).isAir()) {
                    return false;
                }
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(IFBlocks.COMPOSTER.get()) || !state.getValue(IndustrialComposterBlock.FORMED)) {
                return false;
            }
            int expectedPart = partForOffset(part.offset());
            boolean expectedSlave = !part.offset().equals(MASTER_OFFSET);
            if (state.getValue(IndustrialComposterBlock.SLAVE) != expectedSlave) {
                return false;
            }
            if (state.hasProperty(IndustrialComposterBlock.PART)
                    && state.getValue(IndustrialComposterBlock.PART) != expectedPart) {
                return false;
            }
        }
        return true;
    }

    public static Iterable<BlockPos> templateOffsets() {
        return Arrays.stream(TEMPLATE_PARTS).map(TemplatePart::offset)::iterator;
    }

    public static BlockState originalStateForOffset(BlockPos offset) {
        for (TemplatePart part : TEMPLATE_PARTS) {
            if (part.offset().equals(offset)) {
                return originalState(part);
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    public static int partForOffset(BlockPos offset) {
        return offset.getY() * WIDTH * DEPTH + offset.getZ() * WIDTH + offset.getX();
    }

    public static BlockPos offsetForPart(int part) {
        int y = part / (WIDTH * DEPTH);
        int withinLayer = part % (WIDTH * DEPTH);
        int z = withinLayer / WIDTH;
        int x = withinLayer % WIDTH;
        return new BlockPos(x, y, z);
    }

    private static TemplatePart part(int x, int y, int z, String blockId) {
        return new TemplatePart(new BlockPos(x, y, z), ResourceLocation.parse(blockId));
    }

    private static boolean matchesOriginal(BlockState state, TemplatePart part) {
        if (part.isAir()) {
            return state.isAir();
        }
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(part.blockId());
    }

    private static BlockState originalState(TemplatePart part) {
        if (part.isAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.get(part.blockId());
        return block.defaultBlockState();
    }

    private record TemplatePart(BlockPos offset, ResourceLocation blockId) {
        boolean isAir() {
            return blockId.equals(AIR);
        }
    }
}
