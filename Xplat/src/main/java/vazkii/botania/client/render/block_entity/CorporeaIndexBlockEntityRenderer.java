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

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.render.block_entity.state.CorporeaIndexRenderState;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class CorporeaIndexBlockEntityRenderer implements BlockEntityRenderer<CorporeaIndexBlockEntity, CorporeaIndexRenderState> {
	private static final Identifier TEXTURE = Identifier.parse(ResourcesLib.MODEL_CORPOREA_INDEX);
	private static final float ANGLE = (float) Math.sin(Math.toRadians(45));
	private final ModelPart ring;
	private final ModelPart cube;

	public CorporeaIndexBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(BotaniaModelLayers.CORPOREA_INDEX);
		ring = root.getChild("ring");
		cube = root.getChild("cube");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("ring", CubeListBuilder.create().addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
		root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
		return mesh;
	}

	@Override
	public CorporeaIndexRenderState createRenderState() {
		return new CorporeaIndexRenderState();
	}

	@Override
	public void extractRenderState(CorporeaIndexBlockEntity blockEntity, CorporeaIndexRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.rotation = (ClientTickHandler.ticksInGame + partialTicks) * 2;
		state.translation = (float) ((Math.cos((blockEntity.ticksWithCloseby
				+ (blockEntity.hasCloseby ? partialTicks : 0)) / 10F) * 0.5 + 0.5) * 0.25);
		state.renderStars = blockEntity.closeby > 0F;
		state.starX = state.starZ = 0;
		state.starSeed = blockEntity.getBlockPos().getX() ^ blockEntity.getBlockPos().getY() ^ blockEntity.getBlockPos().getZ();
		state.starAnimationTicks = ClientTickHandler.ticksInGame + partialTicks;
		if (state.renderStars) {
			float radius = (float) CorporeaIndexBlockEntity.RADIUS * blockEntity.closeby
					+ (blockEntity.closeby == 1F ? 0F : blockEntity.hasCloseby ? partialTicks : -partialTicks) * 0.2F;
			double radians = (blockEntity.ticksWithCloseby + partialTicks) * 2 * Math.PI / 180;
			state.starX = Math.cos(radians) * radius;
			state.starZ = Math.sin(radians) * radius;
		}
	}

	@Override
	public void submit(CorporeaIndexRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		var layer = RenderTypes.entityCutoutNoCull(TEXTURE);
		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		poseStack.pushPose();
		poseStack.translate(0, -1, 0);
		poseStack.mulPose(VecHelper.rotateY(state.rotation));
		poseStack.translate(0, 1.5F + state.translation / 2, 0);
		poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), ANGLE, 0, ANGLE));
		submitPart(ring, state, poseStack, collector, layer);
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), ANGLE, 0, ANGLE));
		poseStack.mulPose(VecHelper.rotateY(state.rotation));
		submitPart(ring, state, poseStack, collector, layer);
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), ANGLE, 0, ANGLE));
		poseStack.mulPose(VecHelper.rotateY(state.rotation));
		submitPart(cube, state, poseStack, collector, layer);
		poseStack.popPose();

		if (state.renderStars) {
			submitStars(state, poseStack, collector, state.starX, state.starZ);
			submitStars(state, poseStack, collector, state.starX, -state.starZ);
		}
		poseStack.popPose();
	}

	private static void submitStars(CorporeaIndexRenderState state, PoseStack poseStack,
			SubmitNodeCollector collector, double x, double z) {
		poseStack.translate(x, 0.3, z);
		RenderHelper.submitStar(poseStack, collector, 0xFF00FF, 0.02F, 0.02F, 0.02F, state.starSeed, state.starAnimationTicks);
		poseStack.translate(-x * 2, 0, -z * 2);
		RenderHelper.submitStar(poseStack, collector, 0xFF00FF, 0.02F, 0.02F, 0.02F, state.starSeed, state.starAnimationTicks);
		poseStack.translate(x, -0.3, z);
	}

	private static void submitPart(ModelPart part, CorporeaIndexRenderState state, PoseStack poseStack,
			SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType layer) {
		collector.submitModelPart(part, poseStack, layer, state.lightCoords, OverlayTexture.NO_OVERLAY,
				null, false, false, -1, state.breakProgress, 0);
	}

	@Override
	public boolean shouldRenderOffScreen(@NotNull CorporeaIndexBlockEntity blockEntity) {
		return true;
	}
}
