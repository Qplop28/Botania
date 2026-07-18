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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.HourglassModel;
import vazkii.botania.client.render.block_entity.state.HoveringHourglassRenderState;
import vazkii.botania.common.block.block_entity.HoveringHourglassBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class HoveringHourglassBlockEntityRenderer implements BlockEntityRenderer<HoveringHourglassBlockEntity, HoveringHourglassRenderState> {
	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_HOURGLASS);
	private final HourglassModel model;

	public HoveringHourglassBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new HourglassModel(ctx.bakeLayer(BotaniaModelLayers.HOURGLASS));
	}

	@Override
	public HoveringHourglassRenderState createRenderState() {
		return new HoveringHourglassRenderState();
	}

	@Override
	public void extractRenderState(HoveringHourglassBlockEntity blockEntity, HoveringHourglassRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		int worldTime = ClientTickHandler.ticksInGame;
		if (worldTime != 0) {
			worldTime += new Random(blockEntity.getBlockPos().hashCode()).nextInt(360);
		}
		state.animationTime = worldTime == 0 ? 0 : worldTime + partialTicks;

		ItemStack stack = blockEntity.getItemHandler().getItem(0);
		float activeFraction = stack.isEmpty()
				? 0
				: blockEntity.lastFraction
						+ (blockEntity.timeFraction - blockEntity.lastFraction) * partialTicks;
		state.upperSandFraction = stack.isEmpty() ? 0 : activeFraction;
		state.lowerSandFraction = stack.isEmpty() ? 0 : 1F - activeFraction;
		state.flip = blockEntity.flip;
		state.flipRotationDegrees = state.flip ? 180F : 1F;
		if (blockEntity.flipTicks > 0) {
			state.flipRotationDegrees += (blockEntity.flipTicks - partialTicks) * (180F / 4F);
		}
		state.sandColor = blockEntity.getColor();
	}

	@Override
	public void submit(HoveringHourglassRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		float time = state.animationTime;
		float x = 0.5F + (float) Math.cos(time * 0.05F) * 0.025F;
		float y = 0.55F + (float) (Math.sin(time * 0.04F) + 1F) * 0.05F;
		float z = 0.5F + (float) Math.sin(time * 0.05F) * 0.025F;
		poseStack.translate(x, y, z);
		poseStack.mulPose(VecHelper.rotateZ(state.flipRotationDegrees));
		poseStack.scale(1F, -1F, -1F);
		model.submit(state, poseStack, submitNodeCollector, texture);
		poseStack.popPose();
	}
}
