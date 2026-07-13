package dev.emmanueltremblay.immersivefarming.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

public final class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity> {
    private static final ModelResourceLocation NORMAL_TOP = new ModelResourceLocation(
            ImmersiveFarming.id("dynamic/sprinkler_top"), "standalone");
    private static final ModelResourceLocation HIGH_PRESSURE_TOP = new ModelResourceLocation(
            ImmersiveFarming.id("dynamic/sprinkler_extended_top"), "standalone");

    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SprinklerBlockEntity sprinkler, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelResourceLocation modelLocation = sprinkler.isHighPressure() ? HIGH_PRESSURE_TOP : NORMAL_TOP;
        BakedModel model = blockRenderer.getBlockModelShaper().getModelManager().getModel(modelLocation);
        if (model == null || sprinkler.getLevel() == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(sprinkler.getSprinklerRotation(partialTicks))));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        int topLight = LevelRenderer.getLightColor(sprinkler.getLevel(), sprinkler.getBlockPos().above());
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), buffers.getBuffer(RenderType.solid()), null, model,
                1.0F, 1.0F, 1.0F, topLight, packedOverlay, ModelData.EMPTY, RenderType.solid());
        poseStack.popPose();
    }
}
