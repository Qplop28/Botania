/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.render.block_entity.state.CocoonRenderState;
import vazkii.botania.common.block.block_entity.CocoonBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class CocoonBlockEntityRenderer implements BlockEntityRenderer<CocoonBlockEntity, CocoonRenderState> {
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	private final BlockModelResolver blockModelResolver;

	public CocoonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
	}

	@Override
	public CocoonRenderState createRenderState() {
		return new CocoonRenderState();
	}

	@Override
	public void extractRenderState(CocoonBlockEntity blockEntity, CocoonRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity, state, partialTicks, cameraPosition, breakProgress);

		float rotation = 0F;
		float modval = 60F - (float) blockEntity.timePassed / (float) CocoonBlockEntity.TOTAL_TIME * 30F;
		if (blockEntity.timePassed % modval < 10F) {
			float mod = (blockEntity.timePassed + partialTicks) % modval;
			float phase = mod / 5F * (float) Math.PI * 2F;
			rotation = (float) Math.sin(phase) * (float) Math.log(blockEntity.timePassed + partialTicks);
		}
		state.rotationDegrees = rotation;

		blockModelResolver.update(state.blockModel, blockEntity.getBlockState(), BLOCK_DISPLAY_CONTEXT);
	}

	@Override
	public void submit(CocoonRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0);
		poseStack.mulPose(VecHelper.rotateX(state.rotationDegrees));
		poseStack.translate(-0.5, 0, 0);
		state.blockModel.submit(
				poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
