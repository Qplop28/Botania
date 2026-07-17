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
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.render.block_entity.state.IncensePlateRenderState;
import vazkii.botania.common.block.block_entity.IncensePlateBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class IncensePlateBlockEntityRenderer implements BlockEntityRenderer<IncensePlateBlockEntity, IncensePlateRenderState> {
	private final ItemModelResolver itemModelResolver;

	public IncensePlateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public IncensePlateRenderState createRenderState() {
		return new IncensePlateRenderState();
	}

	@Override
	public void extractRenderState(IncensePlateBlockEntity blockEntity, IncensePlateRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		ItemStack stack = blockEntity.getItemHandler().getItem(0);
		itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND,
				blockEntity.getLevel(), null, 0);

		Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		state.rotationDegrees = switch (facing) {
			default -> 0;
			case WEST -> 90;
			case SOUTH -> 180;
			case EAST -> 270;
		};
	}

	@Override
	public void submit(IncensePlateRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.item.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(VecHelper.rotateY(state.rotationDegrees));
		poseStack.translate(-0.11F, -1.35F, 0F);
		poseStack.scale(0.6F, 0.6F, 0.6F);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
