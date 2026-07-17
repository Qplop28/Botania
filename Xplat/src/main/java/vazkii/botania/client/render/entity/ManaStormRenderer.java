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
import net.minecraft.client.renderer.state.level.CameraRenderState;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.state.ManaStormRenderState;
import vazkii.botania.common.entity.ManaStormEntity;

public class ManaStormRenderer extends EntityRenderer<ManaStormEntity, ManaStormRenderState> {
	public ManaStormRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public ManaStormRenderState createRenderState() {
		return new ManaStormRenderState();
	}

	@Override
	public void extractRenderState(ManaStormEntity storm, ManaStormRenderState state, float partialTicks) {
		super.extractRenderState(storm, state, partialTicks);
		float maxScale = 1.95F;
		state.starScale = 0.05F + ((float) storm.burstsFired / ManaStormEntity.TOTAL_BURSTS
				- (storm.deathTime == 0 ? 0 : storm.deathTime + partialTicks) / ManaStormEntity.DEATH_TIME) * maxScale;
		state.seed = storm.getUUID().getMostSignificantBits();
	}

	@Override
	public void submit(ManaStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState camera) {
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.STAR, (pose, consumer) -> {
			PoseStack starPose = new PoseStack();
			starPose.last().pose().set(pose.pose());
			starPose.last().normal().set(pose.normal());
			RenderHelper.renderStar(starPose, consumer, 0x00FF00, state.starScale, state.starScale,
					state.starScale, state.seed);
		});
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
