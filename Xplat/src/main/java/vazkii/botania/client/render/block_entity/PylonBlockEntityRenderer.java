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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.GaiaPylonModel;
import vazkii.botania.client.model.ManaPylonModel;
import vazkii.botania.client.model.NaturaPylonModel;
import vazkii.botania.client.model.PylonModel;
import vazkii.botania.client.render.block_entity.state.PylonRenderState;
import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.block.block_entity.PylonBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class PylonBlockEntityRenderer implements BlockEntityRenderer<PylonBlockEntity, PylonRenderState> {
	public static final Identifier MANA_TEXTURE = Identifier.parse(ResourcesLib.MODEL_PYLON_MANA);
	public static final Identifier NATURA_TEXTURE = Identifier.parse(ResourcesLib.MODEL_PYLON_NATURA);
	public static final Identifier GAIA_TEXTURE = Identifier.parse(ResourcesLib.MODEL_PYLON_GAIA);

	private final ManaPylonModel manaModel;
	private final NaturaPylonModel naturaModel;
	private final GaiaPylonModel gaiaModel;

	public PylonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		manaModel = new ManaPylonModel(context.bakeLayer(BotaniaModelLayers.PYLON_MANA));
		naturaModel = new NaturaPylonModel(context.bakeLayer(BotaniaModelLayers.PYLON_NATURA));
		gaiaModel = new GaiaPylonModel(context.bakeLayer(BotaniaModelLayers.PYLON_GAIA));
	}

	@Override
	public PylonRenderState createRenderState() {
		return new PylonRenderState();
	}

	@Override
	public void extractRenderState(PylonBlockEntity blockEntity, PylonRenderState state, float partialTicks,
			Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.variant = ((PylonBlock) blockEntity.getBlockState().getBlock()).variant;
		float time = ClientTickHandler.ticksInGame + partialTicks
				+ new Random(blockEntity.getBlockPos().hashCode()).nextInt(360);
		state.ringRotation = time * 1.5F;
		state.crystalRotation = -time;
		state.ringBob = (float) (Math.sin(time / 20D) / 20 - 0.025);
		state.crystalBob = (float) (Math.sin(time / 20D) / 17.5);
	}

	@Override
	public void submit(PylonRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		PylonModel model;
		Identifier texture;
		RenderType glow;
		switch (state.variant) {
			default -> { model = manaModel; texture = MANA_TEXTURE; glow = RenderHelper.MANA_PYLON_GLOW; }
			case NATURA -> { model = naturaModel; texture = NATURA_TEXTURE; glow = RenderHelper.NATURA_PYLON_GLOW; }
			case GAIA -> { model = gaiaModel; texture = GAIA_TEXTURE; glow = RenderHelper.GAIA_PYLON_GLOW; }
		}

		poseStack.pushPose();
		poseStack.translate(0, 1.5, 0);
		poseStack.scale(1F, -1F, -1F);
		poseStack.pushPose();
		poseStack.translate(0.5F, 0, -0.5F);
		poseStack.mulPose(VecHelper.rotateY(state.ringRotation));
		model.submitRing(poseStack, collector, RenderTypes.entityTranslucent(texture), state.lightCoords,
				OverlayTexture.NO_OVERLAY, false, 0);
		poseStack.translate(0, state.ringBob, 0);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0, state.crystalBob, 0);
		poseStack.translate(0.5F, 0, -0.5F);
		poseStack.mulPose(VecHelper.rotateY(state.crystalRotation));
		model.submitCrystal(poseStack, collector, glow, state.lightCoords, OverlayTexture.NO_OVERLAY, false, 0);
		poseStack.popPose();
		poseStack.popPose();
	}
}
