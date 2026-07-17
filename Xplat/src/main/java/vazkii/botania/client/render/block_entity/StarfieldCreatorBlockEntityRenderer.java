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
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.joml.Matrix4f;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.StarfieldCreatorBlockEntity;

public class StarfieldCreatorBlockEntityRenderer implements BlockEntityRenderer<StarfieldCreatorBlockEntity, BlockEntityRenderState> {
	public StarfieldCreatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public BlockEntityRenderState createRenderState() {
		return new BlockEntityRenderState();
	}

	@Override
	public void submit(BlockEntityRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		// [VanillaCopy] Adapted from TheEndPortalRenderer, only renders UP face and sets the offset low in the blockspace
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.STARFIELD, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			float offset = 0.24F;

			consumer.addVertex(matrix, 0.0F, offset, 1.0F);
			consumer.addVertex(matrix, 1.0F, offset, 1.0F);
			consumer.addVertex(matrix, 1.0F, offset, 0.0F);
			consumer.addVertex(matrix, 0.0F, offset, 0.0F);
		});
	}
}
