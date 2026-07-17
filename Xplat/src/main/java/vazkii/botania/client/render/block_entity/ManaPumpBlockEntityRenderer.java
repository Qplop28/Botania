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

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.block_entity.state.ManaPumpRenderState;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.ArrayList;
import java.util.List;

public class ManaPumpBlockEntityRenderer implements BlockEntityRenderer<ManaPumpBlockEntity, ManaPumpRenderState> {
	private static final int[] EMPTY_TINTS = {};

	public ManaPumpBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public ManaPumpRenderState createRenderState() {
		return new ManaPumpRenderState();
	}

	@Override
	public void extractRenderState(ManaPumpBlockEntity blockEntity, ManaPumpRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.rotationDegrees = switch (blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
			default -> 0F;
			case SOUTH -> 180F;
			case EAST -> -90F;
			case WEST -> 90F;
		};
		double movement = Math.max(0F,
				Math.min(8F, blockEntity.innerRingPos + blockEntity.moving * partialTicks));
		state.headTranslation = (float) (movement / 14D);

		BlockStateModel model = MiscellaneousModels.INSTANCE.manaPumpHead();
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42L), parts);
		state.headModelParts = List.copyOf(parts);
	}

	@Override
	public void submit(ManaPumpRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		poseStack.mulPose(VecHelper.rotateY(state.rotationDegrees));
		poseStack.translate(-0.5, 0, -0.5);
		poseStack.translate(0, 0, state.headTranslation);
		submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), state.headModelParts,
				EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
