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

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.ManaPoolRenderState;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Random;


public class ManaPoolBlockEntityRenderer implements BlockEntityRenderer<ManaPoolBlockEntity, ManaPoolRenderState> {
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	private final BlockModelResolver blockModelResolver;
	private final SpriteGetter sprites;
	private final TextureAtlasSprite waterSprite;

	public ManaPoolBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
		this.sprites = context.sprites();
		this.waterSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "block/mana_water")));
	}

	@Override
	public ManaPoolRenderState createRenderState() {
		return new ManaPoolRenderState();
	}

	@Override
	public void extractRenderState(ManaPoolBlockEntity blockEntity, ManaPoolRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity,
				state,
				partialTicks,
				cameraPosition,
				breakProgress);

		state.fabulousVisible = false;
		state.fabulousTint = 0xFFFFFFFF;
		state.insideUvStart = 2;
		state.insideUvEnd = 14;
		state.poolBottom = 2F / 16F + 0.001F;
		state.poolTop = 7F / 16F;
		state.manaLevel = 0F;
		state.overlayTexture = null;
		state.overlayAlpha = 0F;

		ManaPoolBlock.Variant variant = ((ManaPoolBlock) blockEntity.getBlockState().getBlock()).variant;
		boolean fabulous = variant == ManaPoolBlock.Variant.FABULOUS;
		boolean diluted = variant == ManaPoolBlock.Variant.DILUTED;
		boolean creative = variant == ManaPoolBlock.Variant.CREATIVE;

		state.insideUvStart = diluted ? 1 : 2;
		state.insideUvEnd = 16 - state.insideUvStart;
		state.poolBottom = state.insideUvStart / 16F + 0.001F;
		state.poolTop = (diluted ? 5 : creative ? 9 : 7) / 16F;

		if (fabulous) {
			float time = ClientTickHandler.ticksInGame + partialTicks;
			time += new Random(blockEntity.getBlockPos().getX()
					^ blockEntity.getBlockPos().getY()
					^ blockEntity.getBlockPos().getZ()).nextInt(100000);
			time *= 0.005F;

			int poolColor = blockEntity.getColor().map(ColorHelper::getColorValue).orElse(-1);
			int color = vazkii.botania.common.helper.MathHelper.multiplyColor(
					Mth.hsvToRgb(Mth.frac(time), 0.6F, 1F), poolColor);
			state.fabulousTint = 0xFF000000 | color;
			blockModelResolver.update(state.fabulousModel, blockEntity.getBlockState(), BLOCK_DISPLAY_CONTEXT);
			var tintLayers = state.fabulousModel.tintLayers();
			if (tintLayers.isEmpty()) {
				tintLayers.add(state.fabulousTint);
			} else {
				tintLayers.set(0, state.fabulousTint);
			}
			state.fabulousVisible = true;
		}

		Block below = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().below()).getBlock();
		if (below instanceof PoolOverlayProvider overlayProvider) {
			state.overlayTexture = overlayProvider.getIcon(blockEntity.getLevel(), blockEntity.getBlockPos());
		}

		state.overlayAlpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 20D) + 1D) * 0.3D + 0.2D);

		int maxMana = blockEntity.getMaxMana();
		if (maxMana == -1) {
			maxMana = ManaPoolBlockEntity.MAX_MANA;
		}
		state.manaLevel = maxMana == 0 ? 0F : (float) blockEntity.getCurrentMana() / (float) maxMana;
	}

	@Override
	public void submit(ManaPoolRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		if (state.fabulousVisible) {
			state.fabulousModel.submit(
					poseStack,
					submitNodeCollector,
					state.lightCoords,
					OverlayTexture.NO_OVERLAY,
					0);
		}

		if (state.overlayTexture != null) {
			TextureAtlasSprite overlaySprite = sprites.get(
					new SpriteId(TextureAtlas.LOCATION_BLOCKS, state.overlayTexture));
			poseStack.pushPose();
			poseStack.translate(0F, state.poolBottom, 0F);
			poseStack.mulPose(VecHelper.rotateX(90F));
			submitCroppedIcon(poseStack, submitNodeCollector, RenderHelper.ICON_OVERLAY,
					overlaySprite, state.insideUvStart, state.insideUvEnd, 0xFFFFFF,
					state.overlayAlpha, state.lightCoords);
			poseStack.popPose();
		}

		if (state.manaLevel > 0) {
			float manaY = Mth.clampedMap(state.manaLevel, 0F, 1F, state.poolBottom, state.poolTop);
			poseStack.pushPose();
			poseStack.translate(0F, manaY, 0F);
			poseStack.mulPose(VecHelper.rotateX(90F));
			submitCroppedIcon(poseStack, submitNodeCollector, RenderHelper.MANA_POOL_WATER,
					waterSprite, state.insideUvStart, state.insideUvEnd, 0xFFFFFF, 1F, state.lightCoords);
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	public static void submitCroppedIcon(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType,
			TextureAtlasSprite sprite, int uvStartPixels, int uvEndPixels, int color, float alpha, int light) {
		ClientXplatAbstractions.instance().markSpriteActive(sprite);
		float uvStart = uvStartPixels / 16F;
		float uvEnd = uvEndPixels / 16F;
		float start = uvStartPixels / 16F;
		float end = uvEndPixels / 16F;
		int alphaByte = (int) (alpha * 255F);
		int red = color >> 16 & 0xFF;
		int green = color >> 8 & 0xFF;
		int blue = color & 0xFF;
		collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
			consumer.addVertex(pose, start, end, 0F).setColor(red, green, blue, alphaByte).setUv(sprite.getU(uvStart), sprite.getV(uvEnd)).setLight(light);
			consumer.addVertex(pose, end, end, 0F).setColor(red, green, blue, alphaByte).setUv(sprite.getU(uvEnd), sprite.getV(uvEnd)).setLight(light);
			consumer.addVertex(pose, end, start, 0F).setColor(red, green, blue, alphaByte).setUv(sprite.getU(uvEnd), sprite.getV(uvStart)).setLight(light);
			consumer.addVertex(pose, start, start, 0F).setColor(red, green, blue, alphaByte).setUv(sprite.getU(uvStart), sprite.getV(uvStart)).setLight(light);
		});
	}
}
