package dev.emmanueltremblay.immersivefarming.integration.ie;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.ComposterMultiblock;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class IFIEMultiblocks {
    public static final ResourceLocation COMPOSTER_ID = ImmersiveFarming.id("multiblocks/composter");

    private IFIEMultiblocks() {
    }

    public static void bootstrap() {
        if (MultiblockHandler.getByUniqueName(COMPOSTER_ID) == null) {
            MultiblockHandler.registerMultiblock(new ComposterManualMultiblock());
        }
    }

    private static final class ComposterManualMultiblock implements MultiblockHandler.IMultiblock {
        @Override
        public ResourceLocation getUniqueName() {
            return COMPOSTER_ID;
        }

        @Override
        public boolean isBlockTrigger(BlockState state, Direction side, Level level) {
            for (BlockPos offset : ComposterMultiblock.templateOffsets()) {
                BlockState templateState = ComposterMultiblock.originalStateForOffset(offset);
                if (!templateState.isAir() && state.is(templateState.getBlock())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) {
            return ComposterMultiblock.tryForm(level, pos);
        }

        @Override
        public List<StructureTemplate.StructureBlockInfo> getStructure(Level level) {
            List<StructureTemplate.StructureBlockInfo> structure = new ArrayList<>();
            for (BlockPos offset : ComposterMultiblock.templateOffsets()) {
                BlockState state = ComposterMultiblock.originalStateForOffset(offset);
                if (!state.isAir()) {
                    structure.add(new StructureTemplate.StructureBlockInfo(offset, state, null));
                }
            }
            return structure;
        }

        @Override
        public float getManualScale() {
            return 22.0F;
        }

        @Override
        public Vec3i getSize(Level level) {
            return new Vec3i(ComposterMultiblock.WIDTH, ComposterMultiblock.HEIGHT, ComposterMultiblock.DEPTH);
        }

        @Override
        public void disassemble(Level level, BlockPos pos, boolean mirrored, Direction clickDirection) {
            ComposterMultiblock.breakFormed(level, pos, level.getBlockState(pos));
        }

        @Override
        public BlockPos getTriggerOffset() {
            return ComposterMultiblock.MASTER_OFFSET;
        }

        @Override
        public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
            consumer.accept(new ClientMultiblocks.MultiblockManualData() {
                @Override
                public NonNullList<ItemStack> getTotalMaterials() {
                    return buildMaterialList();
                }

                @Override
                public boolean canRenderFormedStructure() {
                    return false;
                }

                @Override
                public void renderFormedStructure(PoseStack poseStack, MultiBufferSource buffer) {
                }
            });
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("block.immersive_farming_mod_porting.composter");
        }

        @Override
        public Block getBlock() {
            return IFBlocks.COMPOSTER.get();
        }

        private static NonNullList<ItemStack> buildMaterialList() {
            Map<Item, Integer> counts = new LinkedHashMap<>();
            for (BlockPos offset : ComposterMultiblock.templateOffsets()) {
                Item item = ComposterMultiblock.originalStateForOffset(offset).getBlock().asItem();
                if (item != Items.AIR) {
                    counts.merge(item, 1, Integer::sum);
                }
            }

            NonNullList<ItemStack> materials = NonNullList.create();
            counts.forEach((item, count) -> materials.add(new ItemStack(item, count)));
            return materials;
        }
    }
}
