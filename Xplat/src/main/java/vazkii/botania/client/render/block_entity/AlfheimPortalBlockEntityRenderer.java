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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.state.AlfheimPortalRenderState;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AlfheimPortalBlockEntityRenderer implements
		BlockEntityRenderer<AlfheimPortalBlockEntity, AlfheimPortalRenderState> {
	private final TextureAtlasSprite portalSprite;

	public AlfheimPortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.portalSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/alfheim_portal_swirl")));
	}

	@Override
	public AlfheimPortalRenderState createRenderState() {
		return new AlfheimPortalRenderState();
	}

	@Override
	public void extractRenderState(AlfheimPortalBlockEntity blockEntity, AlfheimPortalRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity, state, partialTicks, cameraPosition, breakProgress);

		AlfheimPortalState portalState = blockEntity.getBlockState().getValue(BotaniaStateProperties.ALFPORTAL_STATE);
		state.active = portalState != AlfheimPortalState.OFF;
		state.alongX = portalState == AlfheimPortalState.ON_X;
		state.alpha = (float) Math.min(1F,
				(Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 8D) + 1D) / 7D + 0.6D)
				* (Math.min(60, blockEntity.ticksOpen) / 60F) * 0.5F;
	}

	@Override
	public void submit(AlfheimPortalRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		ClientXplatAbstractions.instance().markSpriteActive(portalSprite);
		if (!state.active || state.alpha <= 0F) {
			return;
		}

		int alpha = Mth.clamp(Math.round(state.alpha * 255F), 0, 255);

		poseStack.pushPose();
		if (state.alongX) {
			poseStack.translate(0.3125, 1, 2);
			poseStack.mulPose(VecHelper.rotateY(90F));
		} else {
			poseStack.translate(-1, 1, 0.3125);
		}
		submitPortalQuad(poseStack, submitNodeCollector, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		if (state.alongX) {
			poseStack.translate(0.6875, 1, -1);
			poseStack.mulPose(VecHelper.rotateY(90F));
		} else {
			poseStack.translate(2, 1, 0.6875);
		}
		poseStack.mulPose(VecHelper.rotateY(180F));
		submitPortalQuad(poseStack, submitNodeCollector, alpha);
		poseStack.popPose();
	}

	private void submitPortalQuad(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int alpha) {
		submitNodeCollector.submitCustomGeometry(
				poseStack, Sheets.translucentBlockSheet(), (pose, consumer) -> {
					consumer.addVertex(pose, 0F, 3F, 0F)
							.setColor(255, 255, 255, alpha)
							.setUv(portalSprite.getU0(), portalSprite.getV1())
							.setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0)
							.setNormal(pose, 1F, 0F, 0F);
					consumer.addVertex(pose, 3F, 3F, 0F)
							.setColor(255, 255, 255, alpha)
							.setUv(portalSprite.getU1(), portalSprite.getV1())
							.setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0)
							.setNormal(pose, 1F, 0F, 0F);
					consumer.addVertex(pose, 3F, 0F, 0F)
							.setColor(255, 255, 255, alpha)
							.setUv(portalSprite.getU1(), portalSprite.getV0())
							.setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0)
							.setNormal(pose, 1F, 0F, 0F);
					consumer.addVertex(pose, 0F, 0F, 0F)
							.setColor(255, 255, 255, alpha)
							.setUv(portalSprite.getU0(), portalSprite.getV0())
							.setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0)
							.setNormal(pose, 1F, 0F, 0F);
				});
	}
}
