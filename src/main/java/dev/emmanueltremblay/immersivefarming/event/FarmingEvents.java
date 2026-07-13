package dev.emmanueltremblay.immersivefarming.event;

import dev.emmanueltremblay.immersivefarming.block.ComposterMultiblock;
import dev.emmanueltremblay.immersivefarming.block.FertileSoilBlock;
import dev.emmanueltremblay.immersivefarming.block.IFBlocks;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;
import dev.emmanueltremblay.immersivefarming.util.IFTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.common.ItemAbilities;

public final class FarmingEvents {
    private static final ResourceLocation ENGINEERS_HAMMER = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "hammer");
    private static final ResourceLocation REDIA_TOOL = ResourceLocation.fromNamespaceAndPath("engineers_decor_reforged", "redia_tool");

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isEngineersHammer(event.getItemStack().getItem())) {
            Level level = event.getLevel();
            if (!level.isClientSide() && ComposterMultiblock.tryForm(level, event.getPos())) {
                event.getEntity().displayClientMessage(Component.translatable("message.immersive_farming_mod_porting.composter_formed"), true);
                event.getItemStack().hurtAndBreak(1, event.getEntity(), slotForHand(event.getHand()));
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!isHoeLike(stack)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        EquipmentSlot slot = slotForHand(event.getHand());

        if (!canTillFromClick(level, pos, event.getHitVec().getDirection())) {
            return;
        }

        if (state.is(IFTags.Blocks.TILLABLE_BLOCK)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, IFBlocks.SOIL.get().defaultBlockState().setValue(FertileSoilBlock.TILL, hoeSpeed(stack)), 3);
                stack.hurtAndBreak(1, event.getEntity(), slot);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (state.getBlock() instanceof FertileSoilBlock && state.getValue(FertileSoilBlock.TILL) < FertileSoilBlock.TILL_MAX) {
            if (!level.isClientSide()) {
                int nextTill = Math.min(FertileSoilBlock.TILL_MAX, state.getValue(FertileSoilBlock.TILL) + hoeSpeed(stack));
                level.setBlock(pos, state.setValue(FertileSoilBlock.TILL, nextTill), 3);
                stack.hurtAndBreak(1, event.getEntity(), slot);
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
        if (soil.getValue(FertileSoilBlock.TILL) < FertileSoilBlock.TILL_MAX) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
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
            event.getLevel().setBlock(event.getPos().below(),
                    soil.setValue(FertileSoilBlock.FERTILITY, soil.getValue(FertileSoilBlock.FERTILITY) - 1), 2);
        }
    }

    private static EquipmentSlot slotForHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    private static boolean isHoeLike(ItemStack stack) {
        return stack.getItem() instanceof HoeItem || stack.canPerformAction(ItemAbilities.HOE_TILL);
    }

    private static boolean canTillFromClick(Level level, BlockPos pos, Direction clickedFace) {
        return clickedFace != Direction.DOWN && level.getBlockState(pos.above()).is(Blocks.AIR);
    }

    private static int hoeSpeed(ItemStack stack) {
        Item item = stack.getItem();
        if (isRediaTool(item)) {
            return 4;
        }
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

    private static boolean isEngineersHammer(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).equals(ENGINEERS_HAMMER);
    }

    private static boolean isRediaTool(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).equals(REDIA_TOOL);
    }
}
