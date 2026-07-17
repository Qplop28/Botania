/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CameraRenderState;
import net.minecraft.world.phys.AABB;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.state.MagicLandmineRenderState;
import vazkii.botania.common.entity.MagicLandmineEntity;

public class MagicLandmineRenderer extends EntityRenderer<MagicLandmineEntity, MagicLandmineRenderState> {
	public MagicLandmineRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public MagicLandmineRenderState createRenderState() {
		return new MagicLandmineRenderState();
	}

	@Override
	public void extractRenderState(MagicLandmineEntity entity, MagicLandmineRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		AABB bounds = entity.getBoundingBox().move(entity.position().scale(-1));
		state.minX = bounds.minX;
		state.minY = bounds.minY;
		state.minZ = bounds.minZ;
		state.width = (float) bounds.getXsize();
		state.depth = (float) bounds.getZsize();
		float pulse = (float) (Math.sin(ClientTickHandler.total() / 20) + 1) * 0.2F + 0.6F;
		state.red = (int) (105 * pulse);
		state.green = (int) (25 * pulse);
		state.blue = (int) (145 * pulse);
		float alphaMultiplier = 1F;
		if (entity.tickCount < 8) {
			alphaMultiplier = Math.min((entity.tickCount + partialTicks) / 8F, 1F);
		} else if (entity.tickCount > 47) {
			alphaMultiplier = Math.min(1F - (entity.tickCount - 47 + partialTicks) / 8F, 1F);
		}
		state.alpha = Math.clamp(Math.round(32F * alphaMultiplier), 0, 32);
	}

	@Override
	public void submit(MagicLandmineRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState camera) {
		double offY = RenderHelper.getOffY();
		poseStack.pushPose();
		poseStack.translate(state.minX, state.minY + offY, state.minZ);
		float inset = 1F / 16F;
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.RECTANGLE, (pose, consumer) ->
				RenderHelper.flatRectangle(consumer, pose.pose(), inset, state.width - inset, 0,
						inset, state.depth - inset, state.red, state.green, state.blue, state.alpha));
		RenderHelper.incrementOffY();
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
