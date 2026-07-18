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

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.block_entity.state.ManaSpreaderRenderState;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.helper.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManaSpreaderBlockEntityRenderer implements BlockEntityRenderer<ManaSpreaderBlockEntity, ManaSpreaderRenderState> {
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	private final BlockModelResolver blockModelResolver;
	private final ItemModelResolver itemModelResolver;

	public ManaSpreaderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public ManaSpreaderRenderState createRenderState() {
		return new ManaSpreaderRenderState();
	}

	@Override
	public void extractRenderState(ManaSpreaderBlockEntity blockEntity, ManaSpreaderRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.coreParts = List.of();
		state.paddingParts = List.of();
		state.scaffoldingParts = List.of();
		state.lensItem.clear();
		state.lensVisible = false;
		state.paddingVisible = false;
		state.scaffoldingVisible = false;
		state.spreaderRotationX = blockEntity.rotationX;
		state.spreaderRotationY = blockEntity.rotationY;
		state.animationTime = ClientTickHandler.ticksInGame + partialTicks;
		state.modelTint = 0xFFFFFFFF;

		ManaSpreaderBlock.Variant variant = blockEntity.getVariant();
		if (variant == ManaSpreaderBlock.Variant.GAIA) {
			int color = Mth.hsvToRgb(
					(float) ((state.animationTime * 2D
							+ new Random(blockEntity.getBlockPos().hashCode()).nextInt(10000)) % 360D) / 360F,
					0.4F,
					0.9F);
			state.modelTint = 0xFF000000 | color;
		}

		blockModelResolver.update(state.bodyModel, blockEntity.getBlockState(), BLOCK_DISPLAY_CONTEXT);
		state.bodyModel.setupTints(new int[] { state.modelTint });

		state.coreParts = collectParts(getCoreModel(variant));

		DyeColor paddingColor = blockEntity.paddingColor;
		if (paddingColor != null) {
			state.paddingParts = collectParts(getPaddingModel(paddingColor));
			state.paddingVisible = true;
		}

		if (blockEntity.getBlockState().getValue(BotaniaStateProperties.HAS_SCAFFOLDING)) {
			state.scaffoldingParts = collectParts(getScaffoldingModel(variant));
			state.scaffoldingVisible = true;
		}

		itemModelResolver.updateForTopItem(
				state.lensItem,
				blockEntity.getItemHandler().getItem(0),
				ItemDisplayContext.NONE,
				blockEntity.getLevel(),
				null,
				0);
		state.lensVisible = !state.lensItem.isEmpty();
	}

	@Override
	public void submit(ManaSpreaderRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		int[] tints = { state.modelTint };

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		Quaternionf transform = VecHelper.rotateY(state.spreaderRotationX + 90F);
		transform.mul(VecHelper.rotateX(state.spreaderRotationY));
		poseStack.mulPose(transform);
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		state.bodyModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(VecHelper.rotateY((float) state.animationTime % 360F));
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		poseStack.translate(0F, (float) Math.sin(state.animationTime / 20D) * 0.05F, 0F);
		submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), state.coreParts,
				tints, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();

		if (state.lensVisible) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.5F, 0.094F);
			poseStack.mulPose(VecHelper.rotateZ(180));
			poseStack.mulPose(VecHelper.rotateX(180));
			poseStack.scale(0.997F, 0.997F, 1F);
			state.lensItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}

		if (state.paddingVisible) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(VecHelper.rotateX(-90));
			poseStack.mulPose(VecHelper.rotateY(180));
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), state.paddingParts,
					tints, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}

		poseStack.popPose();

		if (state.scaffoldingVisible) {
			submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), state.scaffoldingParts,
					tints, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		}
	}

	private static List<BlockStateModelPart> collectParts(BlockStateModel model) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42L), parts);
		return List.copyOf(parts);
	}

	private static BlockStateModel getCoreModel(ManaSpreaderBlock.Variant variant) {
		return switch (variant) {
			case GAIA -> MiscellaneousModels.INSTANCE.gaiaSpreaderCore();
			case REDSTONE -> MiscellaneousModels.INSTANCE.redstoneSpreaderCore();
			case ELVEN -> MiscellaneousModels.INSTANCE.elvenSpreaderCore();
			case MANA -> MiscellaneousModels.INSTANCE.manaSpreaderCore();
		};
	}

	private static BlockStateModel getPaddingModel(DyeColor color) {
		return MiscellaneousModels.INSTANCE.spreaderPadding(color);
	}

	private static BlockStateModel getScaffoldingModel(ManaSpreaderBlock.Variant variant) {
		return switch (variant) {
			case MANA, REDSTONE -> MiscellaneousModels.INSTANCE.manaSpreaderScaffolding();
			case ELVEN -> MiscellaneousModels.INSTANCE.elvenSpreaderScaffolding();
			case GAIA -> MiscellaneousModels.INSTANCE.gaiaSpreaderScaffolding();
		};
	}
}
