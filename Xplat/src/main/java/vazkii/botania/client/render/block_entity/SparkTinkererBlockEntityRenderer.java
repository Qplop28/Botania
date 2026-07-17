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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.render.block_entity.state.SparkTinkererRenderState;
import vazkii.botania.common.block.block_entity.SparkTinkererBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class SparkTinkererBlockEntityRenderer implements BlockEntityRenderer<SparkTinkererBlockEntity, SparkTinkererRenderState> {
	private final ItemModelResolver itemModelResolver;

	public SparkTinkererBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public SparkTinkererRenderState createRenderState() {
		return new SparkTinkererRenderState();
	}

	@Override
	public void extractRenderState(SparkTinkererBlockEntity blockEntity, SparkTinkererRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		itemModelResolver.updateForTopItem(state.item, blockEntity.getItemHandler().getItem(0),
				ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
	}

	@Override
	public void submit(SparkTinkererRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.item.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateX(90F));
		poseStack.translate(1.0F, -0.125F, -0.25F);
		poseStack.mulPose(VecHelper.rotateY(180F));
		poseStack.translate(0.5F, 0.5F, 0F);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
