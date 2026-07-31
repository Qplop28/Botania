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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.state.AnimatedTorchRenderState;
import vazkii.botania.common.block.block_entity.AnimatedTorchBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class AnimatedTorchBlockEntityRenderer implements BlockEntityRenderer<AnimatedTorchBlockEntity, AnimatedTorchRenderState> {
	private final ItemModelResolver itemModelResolver;

	public AnimatedTorchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public AnimatedTorchRenderState createRenderState() {
		return new AnimatedTorchRenderState();
	}

	@Override
	public void extractRenderState(AnimatedTorchBlockEntity blockEntity, AnimatedTorchRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		int worldTime = blockEntity.getLevel() == null ? 0 : ClientTickHandler.ticksInGame;
		if (worldTime != 0) {
			worldTime += new Random(blockEntity.getBlockPos().hashCode()).nextInt(360);
		}

		float time = worldTime == 0 ? 0 : worldTime + partialTicks;
		state.translateX = 0.5F + (float) Math.cos(time * 0.05F) * 0.025F;
		state.translateY = 0.1F + ((float) Math.sin(time * 0.04F) + 1F) * 0.05F;
		state.translateZ = 0.5F + (float) Math.sin(time * 0.05F) * 0.025F;
		state.rotation = (float) blockEntity.rotation;
		if (blockEntity.rotating) {
			state.rotation += (float) (blockEntity.anglePerTick * partialTicks);
		}

		itemModelResolver.updateForTopItem(state.item, new ItemStack(Blocks.REDSTONE_TORCH),
				ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
	}

	@Override
	public void submit(AnimatedTorchRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(state.translateX, state.translateY, state.translateZ);
		poseStack.scale(2, 2, 2);
		poseStack.mulPose(VecHelper.rotateX(90));
		poseStack.mulPose(VecHelper.rotateZ(state.rotation));
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
