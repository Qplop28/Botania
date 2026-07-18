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

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.client.render.entity.state.ManaPoolMinecartRenderState;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.entity.ManaPoolMinecartEntity;
import vazkii.botania.common.helper.VecHelper;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaPoolMinecartRenderer extends AbstractMinecartRenderer<ManaPoolMinecartEntity, ManaPoolMinecartRenderState> {
	private final TextureAtlasSprite waterSprite;

	public ManaPoolMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.MINECART);
		this.waterSprite = context.getSprites().get(
				new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("block/mana_water")));
	}

	@Override
	public ManaPoolMinecartRenderState createRenderState() {
		return new ManaPoolMinecartRenderState();
	}

	@Override
	public void extractRenderState(ManaPoolMinecartEntity entity, ManaPoolMinecartRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.manaLevel = (float) entity.getMana() / (float) ManaPoolBlockEntity.MAX_MANA;
	}

	@Override
	protected void submitMinecartContents(ManaPoolMinecartRenderState state, BlockModelRenderState blockModel,
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
		super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
		int insideUvStart = 2;
		int insideUvEnd = 14;
		float poolBottom = 2F / 16F + 0.001F;
		float poolTop = 7F / 16F;

		if (state.manaLevel > 0) {
			poseStack.pushPose();
			poseStack.translate(0F, Mth.clampedMap(state.manaLevel, 0F, 1F, poolBottom, poolTop), 0F);
			poseStack.mulPose(VecHelper.rotateX(90F));
			ManaPoolBlockEntityRenderer.submitCroppedIcon(poseStack, submitNodeCollector,
					RenderHelper.MANA_POOL_WATER, waterSprite, insideUvStart, insideUvEnd,
					0xFFFFFF, 1F, lightCoords);
			poseStack.popPose();
		}
	}
}
