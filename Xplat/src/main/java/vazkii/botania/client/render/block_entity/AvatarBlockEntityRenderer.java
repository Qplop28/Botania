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
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.AvatarWieldable;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.AvatarModel;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.render.block_entity.state.AvatarRenderState;
import vazkii.botania.common.block.block_entity.AvatarBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.XplatAbstractions;

public class AvatarBlockEntityRenderer implements BlockEntityRenderer<AvatarBlockEntity, AvatarRenderState> {
	private static final float[] ROTATIONS = { 180F, 0F, 90F, 270F };
	private static final Identifier TEXTURE = Identifier.parse(ResourcesLib.MODEL_AVATAR);
	private final AvatarModel model;
	private final ItemModelResolver itemModelResolver;

	public AvatarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		model = new AvatarModel(context.bakeLayer(BotaniaModelLayers.AVATAR));
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public AvatarRenderState createRenderState() {
		return new AvatarRenderState();
	}

	@Override
	public void extractRenderState(AvatarBlockEntity blockEntity, AvatarRenderState state, float partialTicks,
			Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		state.rotationDegrees = ROTATIONS[Math.max(Math.min(ROTATIONS.length - 1, facing.get3DDataValue() - 2), 0)];
		state.heldItem.clear();
		state.hasHeldItem = false;
		state.hasOverlay = false;
		state.overlayTexture = null;
		state.overlayAlpha = 0;

		ItemStack stack = blockEntity.getItemHandler().getItem(0);
		if (!stack.isEmpty()) {
			state.hasHeldItem = true;
			itemModelResolver.updateForTopItem(state.heldItem, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
					blockEntity.getLevel(), null, 0);
			AvatarWieldable wieldable = XplatAbstractions.INSTANCE.findAvatarWieldable(stack);
			if (wieldable != null) {
				state.hasOverlay = true;
				state.overlayTexture = wieldable.getOverlayResource(blockEntity);
				state.overlayAlpha = (float) Math.sin(ClientTickHandler.ticksInGame / 20D) / 2F + 0.5F;
			}
		}
	}

	@Override
	public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.6F, 0.5F);
		poseStack.scale(1F, -1F, -1F);
		poseStack.mulPose(VecHelper.rotateY(state.rotationDegrees));
		collector.submitModelPart(model.root(), poseStack, model.renderType(TEXTURE), state.lightCoords,
				OverlayTexture.NO_OVERLAY, null, false, false, -1, state.breakProgress, 0);

		if (state.hasHeldItem) {
			poseStack.pushPose();
			poseStack.scale(0.6F, 0.6F, 0.6F);
			poseStack.translate(-0.5F, 2F, -0.25F);
			poseStack.mulPose(VecHelper.rotateX(-70));
			state.heldItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}

		if (state.hasOverlay && state.overlayTexture != null) {
			poseStack.pushPose();
			poseStack.scale(1.01F, 1.01F, 1.01F);
			poseStack.translate(0F, -0.01F, 0F);
			collector.submitModelPart(model.root(), poseStack, RenderTypes.entityTranslucent(state.overlayTexture),
					0xF000F0, OverlayTexture.NO_OVERLAY, null, false, false,
					(int) (state.overlayAlpha * 255) << 24 | 0xFFFFFF, state.breakProgress, 0);
			poseStack.popPose();
		}
		poseStack.popPose();
	}
}
