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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.TerrestrialAgglomerationPlateRenderState;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class TerrestrialAgglomerationPlateBlockEntityRenderer implements
		BlockEntityRenderer<TerrestrialAgglomerationPlateBlockEntity, TerrestrialAgglomerationPlateRenderState> {
	private final TextureAtlasSprite overlaySprite;

	public TerrestrialAgglomerationPlateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		overlaySprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/terra_plate_overlay")));
	}

	@Override
	public TerrestrialAgglomerationPlateRenderState createRenderState() {
		return new TerrestrialAgglomerationPlateRenderState();
	}

	@Override
	public void extractRenderState(TerrestrialAgglomerationPlateBlockEntity blockEntity,
			TerrestrialAgglomerationPlateRenderState state, float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		float alphaModifier = Math.min(1.0F, blockEntity.getCompletion() / 0.1F);
		state.overlayAlpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 8D) + 1D)
				/ 5D + 0.6D) * alphaModifier;
	}

	@Override
	public void submit(TerrestrialAgglomerationPlateRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		ClientXplatAbstractions.instance().markSpriteActive(overlaySprite);
		if (state.overlayAlpha <= 0) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0F, 3F / 16F + 0.001F, 0F);
		poseStack.mulPose(VecHelper.rotateX(90F));
		int alpha = Mth.clamp(Math.round(state.overlayAlpha * 255F), 0, 255);
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.TERRA_PLATE, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			consumer.addVertex(matrix, 0F, 1F, 0F)
					.setColor(255, 255, 255, alpha)
					.setUv(overlaySprite.getU0(), overlaySprite.getV1())
					.setLight(0xF000F0);
			consumer.addVertex(matrix, 1F, 1F, 0F)
					.setColor(255, 255, 255, alpha)
					.setUv(overlaySprite.getU1(), overlaySprite.getV1())
					.setLight(0xF000F0);
			consumer.addVertex(matrix, 1F, 0F, 0F)
					.setColor(255, 255, 255, alpha)
					.setUv(overlaySprite.getU1(), overlaySprite.getV0())
					.setLight(0xF000F0);
			consumer.addVertex(matrix, 0F, 0F, 0F)
					.setColor(255, 255, 255, alpha)
					.setUv(overlaySprite.getU0(), overlaySprite.getV0())
					.setLight(0xF000F0);
		});
		poseStack.popPose();
	}
}
