package dev.emmanueltremblay.immersivefarming.block.entity;

import dev.emmanueltremblay.immersivefarming.block.IFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialComposterBlockEntity extends BlockEntity {
    public IndustrialComposterBlockEntity(BlockPos pos, BlockState blockState) {
        super(IFBlockEntities.COMPOSTER.get(), pos, blockState);
    }
}
