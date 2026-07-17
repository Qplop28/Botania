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

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.state.BabylonWeaponRenderState;
import vazkii.botania.common.entity.BabylonWeaponEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BabylonWeaponRenderer extends EntityRenderer<BabylonWeaponEntity, BabylonWeaponRenderState> {
	private static final int[] EMPTY_TINTS = {};
	private static final long MODEL_PART_SEED = 42L;

	public BabylonWeaponRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public BabylonWeaponRenderState createRenderState() {
		return new BabylonWeaponRenderState();
	}

	@Override
	public void extractRenderState(BabylonWeaponEntity entity, BabylonWeaponRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		int live = entity.getLiveTicks();
		int delay = entity.getDelay();
		state.charge = Math.min(10F, Math.max(live, entity.getChargeTicks()) + partialTicks);
		state.chargeMultiplier = state.charge / 10F;

		state.iconScale = state.chargeMultiplier;
		if (live > delay) {
			state.iconScale -= Math.min(1F, (live - delay + partialTicks) * 0.2F);
		}
		state.iconScale *= 2F;

		Random random = new Random(entity.getUUID().getMostSignificantBits());
		state.iconYOffset = -0.3F + random.nextFloat() * 0.1F;
		state.iconRotation = state.charge * 9F
				+ (entity.tickCount + partialTicks) * 0.5F
				+ random.nextFloat() * 360F;
		state.weaponRotation = entity.getRotation();

		BlockStateModel model = MiscellaneousModels.INSTANCE.kingKeyWeaponModel(entity.getVariety());
		List<BlockStateModelPart> modelParts = new ArrayList<>();
		model.collectParts(RandomSource.create(MODEL_PART_SEED), modelParts);
		state.weaponModelParts = List.copyOf(modelParts);
	}

	@Override
	public void submit(BabylonWeaponRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateY(state.weaponRotation));

		poseStack.pushPose();
		poseStack.translate(-0.75, 0, 1);
		poseStack.scale(1.5F, 1.5F, 1.5F);
		poseStack.mulPose(VecHelper.rotateY(90F));
		poseStack.mulPose(VecHelper.rotateZ(-45F));
		submitNodeCollector.submitBlockModel(poseStack, Sheets.translucentItemSheet(), state.weaponModelParts,
				EMPTY_TINTS, 0xF000F0, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();

		poseStack.mulPose(VecHelper.rotateX(-90F));
		poseStack.translate(0F, state.iconYOffset, 0F);
		poseStack.scale(state.iconScale, state.iconScale, state.iconScale);
		poseStack.mulPose(VecHelper.rotateY(state.iconRotation));
		int alpha = Mth.clamp(Math.round(state.chargeMultiplier * 255F), 0, 255);
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.BABYLON_ICON, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			consumer.addVertex(matrix, -1, 0, -1).setColor(255, 255, 255, alpha).setUv(0, 0);
			consumer.addVertex(matrix, -1, 0, 1).setColor(255, 255, 255, alpha).setUv(0, 1);
			consumer.addVertex(matrix, 1, 0, 1).setColor(255, 255, 255, alpha).setUv(1, 1);
			consumer.addVertex(matrix, 1, 0, -1).setColor(255, 255, 255, alpha).setUv(1, 0);
		});

		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
