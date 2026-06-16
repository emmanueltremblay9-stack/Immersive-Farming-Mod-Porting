package dev.emmanueltremblay.immersivefarming.item;

import dev.emmanueltremblay.immersivefarming.block.FertileSoilBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class CompostItem extends Item {
    public CompostItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof FertileSoilBlock) || state.getValue(FertileSoilBlock.FERTILITY) >= 1) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            context.getLevel().setBlock(context.getClickedPos(), state.setValue(FertileSoilBlock.FERTILITY, 1), 3);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
