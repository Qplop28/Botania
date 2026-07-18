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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.ManaEnchanterRenderState;
import vazkii.botania.common.block.block_entity.ManaEnchanterBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.mixin.ItemEntityAccessor;
import vazkii.botania.xplat.ClientXplatAbstractions;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaEnchanterBlockEntityRenderer implements BlockEntityRenderer<ManaEnchanterBlockEntity, ManaEnchanterRenderState> {
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final TextureAtlasSprite overlaySprite;
	private @Nullable ItemEntity itemEntity;

	public ManaEnchanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderDispatcher = context.entityRenderer();
		this.overlaySprite = context.sprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/enchanter_overlay"))
		);
	}

	@Override
	public ManaEnchanterRenderState createRenderState() {
		return new ManaEnchanterRenderState();
	}

	@Override
	public void extractRenderState(ManaEnchanterBlockEntity blockEntity, ManaEnchanterRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.itemEntity = null;
		state.overlayAngleDegrees = 0F;
		state.overlayTranslation = 0F;
		state.overlayScale = 1F;

		float alphaModifier = 0F;
		if (blockEntity.stage == ManaEnchanterBlockEntity.State.GATHER_MANA) {
			alphaModifier = Math.min(20, blockEntity.stageTicks) / 20F;
		} else if (blockEntity.stage == ManaEnchanterBlockEntity.State.RESET) {
			alphaModifier = (20 - blockEntity.stageTicks) / 20F;
		} else if (blockEntity.stage == ManaEnchanterBlockEntity.State.DO_ENCHANT) {
			alphaModifier = 1F;
		}

		state.overlayAlpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 8D) + 1D) / 5D + 0.4D) * alphaModifier;
		state.animateOverlay = blockEntity.stage == ManaEnchanterBlockEntity.State.DO_ENCHANT
				|| blockEntity.stage == ManaEnchanterBlockEntity.State.RESET;
		if (state.animateOverlay) {
			float ticks = blockEntity.stageTicks + blockEntity.stage3EndTicks + partialTicks;
			state.overlayAngleDegrees = ticks * 2F;
			state.overlayTranslation = Math.min(20F, ticks) / 20F * 1.15F;
			state.overlayScale = ticks < 10F ? 1F : 1F - Math.min(20F, ticks - 10F) / 20F * 0.75F;
		}

		if (!blockEntity.itemToEnchant.isEmpty()) {
			if (itemEntity == null || itemEntity.level() != blockEntity.getLevel()) {
				itemEntity = new ItemEntity(blockEntity.getLevel(), blockEntity.getBlockPos().getX(),
						blockEntity.getBlockPos().getY() + 1, blockEntity.getBlockPos().getZ(), blockEntity.itemToEnchant);
			}
			((ItemEntityAccessor) itemEntity).setAge(ClientTickHandler.ticksInGame);
			itemEntity.setItem(blockEntity.itemToEnchant);
			EntityRenderState extracted = entityRenderDispatcher.extractEntity(itemEntity, partialTicks);
			if (extracted instanceof ItemEntityRenderState itemState) {
				state.itemEntity = itemState;
			}
		}
	}

	@Override
	public void submit(ManaEnchanterRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		if (state.itemEntity != null) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 1.25F, 0.5F);
			entityRenderDispatcher.submit(state.itemEntity, camera, 0, 0, 0, poseStack, submitNodeCollector);
			poseStack.popPose();
		}

		poseStack.mulPose(VecHelper.rotateX(90F));
		poseStack.translate(-2F, -2F, -0.001F);
		if (state.overlayAlpha > 0) {
			if (state.animateOverlay) {
				poseStack.translate(2.5F, 2.5F, -state.overlayTranslation);
				poseStack.scale(state.overlayScale, state.overlayScale, 1F);
				poseStack.mulPose(VecHelper.rotateZ(state.overlayAngleDegrees));
				poseStack.translate(-2.5F, -2.5F, 0F);
			}
			ClientXplatAbstractions.instance().markSpriteActive(overlaySprite);
			int alpha = Mth.clamp((int) (state.overlayAlpha * 255F), 0, 255);
			submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.ENCHANTER, (pose, consumer) -> {
				consumer.addVertex(pose, 0F, 5F, 0F).setColor(255, 255, 255, alpha).setUv(overlaySprite.getU0(), overlaySprite.getV1()).setLight(state.lightCoords);
				consumer.addVertex(pose, 5F, 5F, 0F).setColor(255, 255, 255, alpha).setUv(overlaySprite.getU1(), overlaySprite.getV1()).setLight(state.lightCoords);
				consumer.addVertex(pose, 5F, 0F, 0F).setColor(255, 255, 255, alpha).setUv(overlaySprite.getU1(), overlaySprite.getV0()).setLight(state.lightCoords);
				consumer.addVertex(pose, 0F, 0F, 0F).setColor(255, 255, 255, alpha).setUv(overlaySprite.getU0(), overlaySprite.getV0()).setLight(state.lightCoords);
			});
		}
		poseStack.popPose();
	}
}
