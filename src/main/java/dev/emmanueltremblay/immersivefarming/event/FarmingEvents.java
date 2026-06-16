package dev.emmanueltremblay.immersivefarming.event;

import dev.emmanueltremblay.immersivefarming.block.FertileSoilBlock;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.util.IFTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

public final class FarmingEvents {
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof HoeItem) || event.getHand() != event.getEntity().getUsedItemHand()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(IFTags.Blocks.TILLABLE_BLOCK)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, IFBlocks.SOIL.get().defaultBlockState().setValue(FertileSoilBlock.TILL, hoeSpeed(event.getItemStack().getItem())), 3);
                event.getItemStack().hurtAndBreak(1, event.getEntity(), event.getEntity().getEquipmentSlotForItem(event.getItemStack()));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (state.getBlock() instanceof FertileSoilBlock && state.getValue(FertileSoilBlock.TILL) < FertileSoilBlock.TILL_MAX) {
            if (!level.isClientSide()) {
                int nextTill = Math.min(FertileSoilBlock.TILL_MAX, state.getValue(FertileSoilBlock.TILL) + hoeSpeed(event.getItemStack().getItem()));
                level.setBlock(pos, state.setValue(FertileSoilBlock.TILL, nextTill), 3);
                event.getItemStack().hurtAndBreak(1, event.getEntity(), event.getEntity().getEquipmentSlotForItem(event.getItemStack()));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBoneMeal(BonemealEvent event) {
        if (!IFConfig.disableVanillaBoneMealOnCrops || event.getState().getBlock() instanceof GrassBlock) {
            return;
        }
        if (event.getState().getBlock() instanceof CropBlock || event.getState().getBlock() instanceof StemBlock) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (IFConfig.enableFarmlandTrampleProtection) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCropGrowPre(CropGrowEvent.Pre event) {
        BlockState crop = event.getState();
        if (!(crop.getBlock() instanceof CropBlock || crop.getBlock() instanceof StemBlock)) {
            return;
        }
        BlockState soil = event.getLevel().getBlockState(event.getPos().below());
        if (!(soil.getBlock() instanceof FertileSoilBlock)) {
            return;
        }
        if (soil.getValue(FertileSoilBlock.MOISTURE) <= 0) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        if (soil.getValue(FertileSoilBlock.FERTILITY) > 0
                && event.getLevel().getRandom().nextDouble() < Math.min(1.0D, IFConfig.compostGrowthMultiplier - 1.0D)) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }

    @SubscribeEvent
    public void onCropGrowPost(CropGrowEvent.Post event) {
        BlockState soil = event.getLevel().getBlockState(event.getPos().below());
        if (soil.getBlock() instanceof FertileSoilBlock && soil.getValue(FertileSoilBlock.FERTILITY) > 0) {
            event.getLevel().setBlock(event.getPos().below(), soil.setValue(FertileSoilBlock.FERTILITY, 0), 2);
        }
    }

    private static int hoeSpeed(Item item) {
        if (item == Items.NETHERITE_HOE || item == Items.DIAMOND_HOE) {
            return 4;
        }
        if (item == Items.IRON_HOE || item == Items.GOLDEN_HOE) {
            return 3;
        }
        if (item == Items.STONE_HOE) {
            return 2;
        }
        return 1;
    }
}
