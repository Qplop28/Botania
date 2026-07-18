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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.state.FloatingFlowerRenderState;
import vazkii.botania.common.block.block_entity.FloatingFlowerBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.Random;

public class FloatingFlowerBlockEntityRenderer implements BlockEntityRenderer<FloatingFlowerBlockEntity, FloatingFlowerRenderState> {
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	private final BlockModelResolver blockModelResolver;

	public FloatingFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
	}

	@Override
	public FloatingFlowerRenderState createRenderState() {
		return new FloatingFlowerRenderState();
	}

	@Override
	public void extractRenderState(FloatingFlowerBlockEntity blockEntity, FloatingFlowerRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity, state, partialTicks, cameraPosition, breakProgress);
		extractFloatingIsland(blockEntity, state, partialTicks, blockModelResolver);
	}

	public static void extractFloatingIsland(BlockEntity blockEntity, FloatingFlowerRenderState state,
			float partialTicks, BlockModelResolver blockModelResolver) {
		state.floatingVisible = false;
		state.rotationYDegrees = 0F;
		state.translationY = 0F;
		state.tiltXDegrees = 0F;

		if (BotaniaConfig.client().staticFloaters()) {
			return;
		}

		double worldTime = ClientTickHandler.ticksInGame + partialTicks;
		if (blockEntity.getLevel() != null) {
			worldTime += new Random(blockEntity.getBlockPos().hashCode()).nextInt(1000);
		}

		state.floatingVisible = true;
		state.rotationYDegrees = -((float) worldTime * 0.5F);
		state.translationY = (float) Math.sin(worldTime * 0.05F) * 0.1F;
		state.tiltXDegrees = 4F * (float) Math.sin(worldTime * 0.04F);

		blockModelResolver.update(state.blockModel, blockEntity.getBlockState(), BLOCK_DISPLAY_CONTEXT);
	}

	@Override
	public void submit(FloatingFlowerRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		submitFloatingIsland(state, poseStack, submitNodeCollector);
	}

	public static void submitFloatingIsland(FloatingFlowerRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		if (!state.floatingVisible) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 0F, 0.5F);
		poseStack.mulPose(VecHelper.rotateY(state.rotationYDegrees));
		poseStack.translate(-0.5F, state.translationY, 0.5F);
		poseStack.mulPose(VecHelper.rotateX(state.tiltXDegrees));
		poseStack.mulPose(VecHelper.rotateY(90F));
		state.blockModel.submit(
				poseStack,
				submitNodeCollector,
				state.lightCoords,
				OverlayTexture.NO_OVERLAY,
				0);
		poseStack.popPose();
	}
}
