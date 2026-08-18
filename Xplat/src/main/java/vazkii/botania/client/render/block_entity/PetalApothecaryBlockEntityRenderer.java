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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.api.block.PetalApothecary;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.state.PetalApothecaryRenderState;
import vazkii.botania.common.block.block_entity.PetalApothecaryBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;

public class PetalApothecaryBlockEntityRenderer implements
		BlockEntityRenderer<PetalApothecaryBlockEntity, PetalApothecaryRenderState> {
	private final ItemModelResolver itemModelResolver;
	private final TextureAtlasSprite waterSprite;
	private final TextureAtlasSprite lavaSprite;

	public PetalApothecaryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
		this.waterSprite = context.sprites().get(
				new SpriteId(
						TextureAtlas.LOCATION_BLOCKS,
						Identifier.withDefaultNamespace("block/water_still")
				)
		);
		this.lavaSprite = context.sprites().get(
				new SpriteId(
						TextureAtlas.LOCATION_BLOCKS,
						Identifier.withDefaultNamespace("block/lava_still")
				)
		);
	}

	@Override
	public PetalApothecaryRenderState createRenderState() {
		return new PetalApothecaryRenderState();
	}

	@Override
	public void extractRenderState(PetalApothecaryBlockEntity blockEntity, PetalApothecaryRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.petalItems = List.of();
		state.animationTicks = (ClientTickHandler.ticksInGame + partialTicks) * 0.5D;
		state.fluidVisible = false;
		state.lava = false;
		state.fluidColor = -1;
		state.fluidAlpha = 0F;
		state.fluidLight = state.lightCoords;

		PetalApothecary.State fluid = blockEntity.getFluid();
		boolean water = fluid == PetalApothecary.State.WATER;
		boolean lava = fluid == PetalApothecary.State.LAVA;
		state.fluidVisible = water || lava;
		state.lava = lava;

		if (lava) {
			state.fluidColor = -1;
			state.fluidAlpha = 1F;
			state.fluidLight = 0xF000F0;
		} else if (water) {
			ClientLevel level = (ClientLevel) blockEntity.getLevel();
			state.fluidColor = BiomeColors.getAverageWaterColor(level, blockEntity.getBlockPos());
			state.fluidAlpha = 0.7F;
			state.fluidLight = state.lightCoords;

			List<ItemStackRenderState> items = new ArrayList<>();
			for (int i = 0; i < blockEntity.inventorySize(); i++) {
				if (blockEntity.getItemHandler().getItem(i).isEmpty()) {
					break;
				}

				ItemStackRenderState itemState = new ItemStackRenderState();
				itemModelResolver.updateForTopItem(itemState, blockEntity.getItemHandler().getItem(i),
						ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
				items.add(itemState);
			}
			state.petalItems = List.copyOf(items);
		}
	}

	@Override
	public void submit(PetalApothecaryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.25D, 0.5D);
		submitPetalItems(state, poseStack, submitNodeCollector);
		submitFluid(state, poseStack, submitNodeCollector);
		poseStack.popPose();
	}

	private static void submitPetalItems(PetalApothecaryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		if (state.petalItems.isEmpty()) {
			return;
		}

		float modelScale = 1F / 8F;
		double ticks = state.animationTicks;
		float offsetPerPetal = 360F / state.petalItems.size();

		poseStack.pushPose();
		poseStack.translate(-0.05F, -0.38F, 0F);
		poseStack.scale(modelScale, modelScale, modelScale);
		for (int i = 0; i < state.petalItems.size(); i++) {
			float offset = offsetPerPetal * i;
			float degrees = (int) (ticks / 0.25F % 360F + offset);
			float radians = VecHelper.toRadians(degrees);
			float radiusX = (float) (1.2F + 0.1F * Math.sin(ticks / 6F));
			float radiusZ = (float) (1.2F + 0.1F * Math.cos(ticks / 6F));
			float x = (float) (radiusX * Math.cos(radians));
			float z = (float) (radiusZ * Math.sin(radians));
			float y = (float) Math.cos((ticks + 50F * i) / 5F) / 10F;

			poseStack.pushPose();
			poseStack.translate(x, y, z);
			float xRotate = (float) Math.sin(ticks * 0.25F) / 2F;
			float yRotate = (float) Math.max(0.6F, Math.sin(ticks * 0.1F) / 2F + 0.5F);
			float zRotate = (float) Math.cos(ticks * 0.25F) / 2F;
			float halfScale = modelScale / 2F;

			poseStack.translate(halfScale, halfScale, halfScale);
			poseStack.mulPose(new Quaternionf().rotateAxis(radians, xRotate, yRotate, zRotate));
			poseStack.translate(-halfScale, -halfScale, -halfScale);

			state.petalItems.get(i).submit(
					poseStack,
					submitNodeCollector,
					state.lightCoords,
					OverlayTexture.NO_OVERLAY,
					0
			);
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	private void submitFluid(PetalApothecaryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		if (!state.fluidVisible) {
			return;
		}

		TextureAtlasSprite sprite = state.lava ? lavaSprite : waterSprite;
		ClientXplatAbstractions.instance().markSpriteActive(sprite);

		int red = state.fluidColor >> 16 & 0xFF;
		int green = state.fluidColor >> 8 & 0xFF;
		int blue = state.fluidColor & 0xFF;
		int alpha = (int) (state.fluidAlpha * 255F);
		float uvStart = 3F / 16F;
		float uvEnd = 13F / 16F;

		poseStack.pushPose();
		poseStack.translate(-8F / 16F, -0.3125F, -8F / 16F);
		poseStack.mulPose(VecHelper.rotateX(90F));
		poseStack.scale(1F / 16F, 1F / 16F, 1F / 16F);

		submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, consumer) -> {
			consumer.addVertex(pose, 3F, 13F, 0F)
					.setColor(red, green, blue, alpha)
					.setUv(sprite.getU(uvStart), sprite.getV(uvEnd))
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(state.fluidLight)
					.setNormal(pose, 0F, 0F, 1F);
			consumer.addVertex(pose, 13F, 13F, 0F)
					.setColor(red, green, blue, alpha)
					.setUv(sprite.getU(uvEnd), sprite.getV(uvEnd))
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(state.fluidLight)
					.setNormal(pose, 0F, 0F, 1F);
			consumer.addVertex(pose, 13F, 3F, 0F)
					.setColor(red, green, blue, alpha)
					.setUv(sprite.getU(uvEnd), sprite.getV(uvStart))
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(state.fluidLight)
					.setNormal(pose, 0F, 0F, 1F);
			consumer.addVertex(pose, 3F, 3F, 0F)
					.setColor(red, green, blue, alpha)
					.setUv(sprite.getU(uvStart), sprite.getV(uvStart))
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(state.fluidLight)
					.setNormal(pose, 0F, 0F, 1F);
		});

		poseStack.popPose();
	}
}
