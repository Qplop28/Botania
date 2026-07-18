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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BellowsModel;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.render.block_entity.state.BellowsRenderState;
import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class BellowsBlockEntityRenderer implements BlockEntityRenderer<BellowsBlockEntity, BellowsRenderState> {
	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_BELLOWS);
	private final BellowsModel model;

	public BellowsBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new BellowsModel(ctx.bakeLayer(BotaniaModelLayers.BELLOWS));
	}

	@Override
	public BellowsRenderState createRenderState() {
		return new BellowsRenderState();
	}

	@Override
	public void extractRenderState(BellowsBlockEntity blockEntity, BellowsRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		switch (blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
			case SOUTH -> state.rotationDegrees = 0F;
			case NORTH -> state.rotationDegrees = 180F;
			case EAST -> state.rotationDegrees = 270F;
			case WEST -> state.rotationDegrees = 90F;
		}
		state.contractionFraction = Math.max(0.1F,
				1F - (blockEntity.movePos + blockEntity.moving * partialTicks + 0.1F));
	}

	@Override
	public void submit(BellowsRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.scale(1F, -1F, -1F);
		poseStack.mulPose(VecHelper.rotateY(state.rotationDegrees));
		model.submit(state, poseStack, submitNodeCollector, texture);
		poseStack.popPose();
	}
}
