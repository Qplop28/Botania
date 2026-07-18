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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.LuminizerRenderState;
import vazkii.botania.client.render.block_entity.state.RadiusRenderData;
import vazkii.botania.common.block.LuminizerBlock;
import vazkii.botania.common.block.block_entity.LuminizerBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.equipment.bauble.ManaseerMonocleItem;
import vazkii.botania.xplat.ClientXplatAbstractions;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class LuminizerBlockEntityRenderer implements BlockEntityRenderer<LuminizerBlockEntity, LuminizerRenderState> {
	private static final int DEFAULT_SPRITE = 0;
	private static final int DETECTOR_SPRITE = 1;
	private static final int FORK_SPRITE = 2;
	private static final int TOGGLE_SPRITE = 3;

	private final TextureAtlasSprite luminizerWorldSprite;
	private final TextureAtlasSprite detectorLuminizerWorldSprite;
	private final TextureAtlasSprite forkLuminizerWorldSprite;
	private final TextureAtlasSprite toggleLuminizerWorldSprite;

	public LuminizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.luminizerWorldSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/light_relay")));
		this.detectorLuminizerWorldSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/detector_light_relay")));
		this.forkLuminizerWorldSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/fork_light_relay")));
		this.toggleLuminizerWorldSprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/toggle_light_relay")));
	}

	@Override
	public LuminizerRenderState createRenderState() {
		return new LuminizerRenderState();
	}

	@Override
	public void extractRenderState(LuminizerBlockEntity blockEntity, LuminizerRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.bindingRadius = null;
		state.radiusColor = 0;
		state.spriteIndex = DEFAULT_SPRITE;
		state.iconRotationDegrees = (float) (ClientTickHandler.ticksInGame + partialTicks);

		if (blockEntity.getBlockState().getBlock() instanceof LuminizerBlock luminizerBlock) {
			state.spriteIndex = switch (luminizerBlock.variant) {
				case DEFAULT -> DEFAULT_SPRITE;
				case DETECTOR -> DETECTOR_SPRITE;
				case FORK -> FORK_SPRITE;
				case TOGGLE -> TOGGLE_SPRITE;
			};
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.cameraEntity instanceof LivingEntity view
				&& ManaseerMonocleItem.hasMonocle(view)
				&& SpecialFlowerBlockEntityRenderer.hasBindingAttempt(view, blockEntity.getBlockPos())) {
			state.bindingRadius = new RadiusRenderData.Circle(0.5F, 0F, 0.5F, LuminizerBlockEntity.MAX_DIST);
			state.radiusColor = Mth.hsvToRgb(ClientTickHandler.ticksInGame % 200 / 200F, 0.6F, 1F);
		}
	}

	@Override
	public void submit(LuminizerRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		SpecialFlowerBlockEntityRenderer.submitRadius(
				state.bindingRadius, state.radiusColor, poseStack, submitNodeCollector);

		TextureAtlasSprite sprite = spriteFor(state.spriteIndex);
		ClientXplatAbstractions.instance().markSpriteActive(sprite);

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.3F, 0.5F);
		poseStack.scale(0.75F, 0.75F, 0.75F);
		poseStack.mulPose(camera.orientation);
		poseStack.mulPose(VecHelper.rotateY(180F));
		poseStack.translate(0F, 0.25F, 0F);
		poseStack.mulPose(VecHelper.rotateZ(state.iconRotationDegrees));
		poseStack.translate(0F, -0.25F, 0F);

		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.LIGHT_RELAY, (pose, consumer) -> {
			float spriteWidth = sprite.getU1() - sprite.getU0();
			float padding = spriteWidth / 8F;
			float u0 = sprite.getU0() + padding;
			float u1 = sprite.getU1() - padding;
			float v0 = sprite.getV0() + padding;
			float v1 = sprite.getV1() - padding;
			Matrix4f matrix = pose.pose();
			consumer.addVertex(matrix, -0.5F, -0.25F, 0F).setColor(255, 255, 255, 255).setUv(u0, v1);
			consumer.addVertex(matrix, 0.5F, -0.25F, 0F).setColor(255, 255, 255, 255).setUv(u1, v1);
			consumer.addVertex(matrix, 0.5F, 0.75F, 0F).setColor(255, 255, 255, 255).setUv(u1, v0);
			consumer.addVertex(matrix, -0.5F, 0.75F, 0F).setColor(255, 255, 255, 255).setUv(u0, v0);
		});

		poseStack.popPose();
	}

	private TextureAtlasSprite spriteFor(int spriteIndex) {
		return switch (spriteIndex) {
			case DETECTOR_SPRITE -> detectorLuminizerWorldSprite;
			case FORK_SPRITE -> forkLuminizerWorldSprite;
			case TOGGLE_SPRITE -> toggleLuminizerWorldSprite;
			default -> luminizerWorldSprite;
		};
	}
}
