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
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.TeruTeruBozuModel;
import vazkii.botania.client.render.block_entity.state.TeruTeruBozuRenderState;
import vazkii.botania.common.block.block_entity.TeruTeruBozuBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class TeruTeruBozuBlockEntityRenderer implements BlockEntityRenderer<TeruTeruBozuBlockEntity, TeruTeruBozuRenderState> {
	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_TERU_TERU_BOZU);
	private static final Identifier textureHalloween = Identifier.parse(ResourcesLib.MODEL_TERU_TERU_BOZU_HALLOWEEN);
	private final TeruTeruBozuModel model;

	public TeruTeruBozuBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new TeruTeruBozuModel(ctx.bakeLayer(BotaniaModelLayers.TERU_TERU_BOZU));
	}

	@Override
	public TeruTeruBozuRenderState createRenderState() {
		return new TeruTeruBozuRenderState();
	}

	@Override
	public void extractRenderState(TeruTeruBozuBlockEntity blockEntity, TeruTeruBozuRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		double time = ClientTickHandler.ticksInGame + partialTicks;
		time += new Random(blockEntity.getBlockPos().hashCode()).nextInt(1000);
		state.animationTime = time;
		state.raining = blockEntity.getLevel() != null && blockEntity.getLevel().isRaining();
		state.halloween = ClientProxy.dootDoot;
	}

	@Override
	public void submit(TeruTeruBozuRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateX(180));
		poseStack.translate(0.5F,
				-1.25F + (float) Math.sin(state.animationTime * 0.01F) * 0.05F,
				-0.5F);
		poseStack.mulPose(VecHelper.rotateY((float) (state.animationTime * 0.3)));
		poseStack.mulPose(VecHelper.rotateZ(4F * (float) Math.sin(state.animationTime * 0.05F)));
		poseStack.scale(0.75F, 0.75F, 0.75F);
		model.submit(state, poseStack, submitNodeCollector, state.halloween ? textureHalloween : texture);
		poseStack.popPose();
	}
}
