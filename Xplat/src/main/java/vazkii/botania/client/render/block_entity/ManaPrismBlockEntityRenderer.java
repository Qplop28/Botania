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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.state.ManaPrismRenderState;
import vazkii.botania.common.block.block_entity.mana.ManaPrismBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class ManaPrismBlockEntityRenderer implements BlockEntityRenderer<ManaPrismBlockEntity, ManaPrismRenderState> {
	private final ItemModelResolver itemModelResolver;

	public ManaPrismBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public ManaPrismRenderState createRenderState() {
		return new ManaPrismRenderState();
	}

	@Override
	public void extractRenderState(ManaPrismBlockEntity blockEntity, ManaPrismRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.itemPosition = (float) Math.sin((ClientTickHandler.ticksInGame + partialTicks) * 0.05F)
				* 0.5F * (1F - 1F / 16F) * 0.997F - 0.5F;

		ItemStack stack = blockEntity.getItemHandler().getItem(0);
		state.item.clear();
		if (stack.getItem() instanceof BasicLensItem) {
			itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.NONE,
					blockEntity.getLevel(), null, 0);
		}
	}

	@Override
	public void submit(ManaPrismRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.item.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateX(90));
		poseStack.translate(0.5F, 0.5F, state.itemPosition);
		poseStack.scale(1.003F, 1.003F, 1F);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
