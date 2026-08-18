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

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.block_entity.state.CorporeaCrystalCubeRenderState;
import vazkii.botania.common.block.block_entity.corporea.CorporeaCrystalCubeBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.mixin.ItemEntityAccessor;

import java.util.ArrayList;
import java.util.List;

public class CorporeaCrystalCubeBlockEntityRenderer implements BlockEntityRenderer<CorporeaCrystalCubeBlockEntity, CorporeaCrystalCubeRenderState> {
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final Font font;
	private @Nullable ItemEntity itemEntity;

	public CorporeaCrystalCubeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderDispatcher = context.entityRenderer();
		this.font = context.font();
	}

	@Override
	public CorporeaCrystalCubeRenderState createRenderState() {
		return new CorporeaCrystalCubeRenderState();
	}

	@Override
	public void extractRenderState(CorporeaCrystalCubeBlockEntity blockEntity, CorporeaCrystalCubeRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.itemEntity = null;
		state.cubeBobTranslation = 0F;
		state.glassParts = List.of();
		state.showCount = false;
		state.countText = net.minecraft.util.FormattedCharSequence.EMPTY;
		state.countTextWidth = 0;
		state.countColor = 0;
		state.countShadeColor = 0;

		ItemStack stack = blockEntity.getRequestTarget();
		if (itemEntity == null || itemEntity.level() != blockEntity.getLevel()) {
			itemEntity = new ItemEntity(blockEntity.getLevel(), blockEntity.getBlockPos().getX(),
					blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), new ItemStack(Blocks.STONE));
		}
		((ItemEntityAccessor) itemEntity).setAge(ClientTickHandler.ticksInGame);
		itemEntity.setItem(stack);
		EntityRenderState extracted = entityRenderDispatcher.extractEntity(itemEntity, partialTicks);
		if (extracted instanceof ItemEntityRenderState itemState) {
			state.cubeBobTranslation = (Mth.sin(itemState.ageInTicks / 10F + itemState.bobOffset) * 0.1F + 0.1F) / -7F;
			if (!stack.isEmpty()) {
				state.itemEntity = itemState;
			}
		}

		BlockStateModel glassModel = MiscellaneousModels.INSTANCE.corporeaCrystalCubeGlass();
		List<BlockStateModelPart> parts = new ArrayList<>();
		glassModel.collectParts(RandomSource.create(42L), parts);
		state.glassParts = List.copyOf(parts);

		if (!stack.isEmpty() && !blockEntity.hideCount) {
			int count = blockEntity.getItemCount();
			String countString = String.valueOf(count);
			int color = 0xFFFFFF;
			if (count > 9_999) {
				countString = count / 1_000 + "K";
				color = 0xFFFF00;
				if (count > 9_999_999) {
					countString = count / 1_000_000 + "M";
					color = 0x00FF00;
				}
			}
			color |= 0xA0 << 24;
			state.countColor = color;
			state.countShadeColor = (color & 16579836) >> 2 | color & -16777216;
			state.countText = Component.literal(countString).getVisualOrderText();
			state.countTextWidth = font.width(state.countText);
			state.showCount = true;
		}
	}

	@Override
	public void submit(CorporeaCrystalCubeRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.scale(1F, -1F, -1F);
		poseStack.translate(0F, state.cubeBobTranslation, 0F);
		if (state.itemEntity != null) {
			poseStack.pushPose();
			poseStack.translate(0F, 0.96F, 0F);
			poseStack.scale(0.64F, 0.64F, 0.64F);
			poseStack.mulPose(VecHelper.rotateZ(180F));
			entityRenderDispatcher.submit(state.itemEntity, camera, 0, 0, 0, poseStack, submitNodeCollector);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(-0.5F, 0.25F, -0.5F);
		submitNodeCollector.submitBlockModel(poseStack, Sheets.translucentBlockSheet(), state.glassParts,
				BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();

		if (state.showCount) {
			poseStack.scale(1F / 64F, 1F / 64F, 1F / 64F);
			poseStack.translate(0F, 55F, 0F);
			float tr = -16.5F;
			for (int i = 0; i < 4; i++) {
				poseStack.mulPose(VecHelper.rotateY(90F));
				poseStack.translate(0F, 0F, tr);
				submitNodeCollector.submitText(poseStack, -state.countTextWidth / 2F, 0F, state.countText,
						state.countColor, false, Font.DisplayMode.NORMAL, state.lightCoords, 0, 0);
				poseStack.translate(0F, 0F, 0.1F);
				submitNodeCollector.submitText(poseStack, -state.countTextWidth / 2F + 1F, 1F, state.countText,
						state.countShadeColor, false, Font.DisplayMode.NORMAL, state.lightCoords, 0, 0);
				poseStack.translate(0F, 0F, -tr - 0.1F);
			}
		}
		poseStack.popPose();
	}
}
