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

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.RunicAltarRenderState;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.ArrayList;
import java.util.List;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class RunicAltarBlockEntityRenderer implements BlockEntityRenderer<RunicAltarBlockEntity, RunicAltarRenderState> {
	private final ItemModelResolver itemModelResolver;
	private final ModelPart spinningCube;
	private static final Identifier cubeTexture = prefix("textures/block/runic_altar_cube.png");

	public RunicAltarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
		var mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().addBox(0, 0, 0, 1, 1, 1), PartPose.ZERO);
		spinningCube = LayerDefinition.create(mesh, 16, 16).bakeRoot();
	}

	@Override
	public RunicAltarRenderState createRenderState() {
		return new RunicAltarRenderState();
	}

	@Override
	public void extractRenderState(RunicAltarBlockEntity blockEntity, RunicAltarRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.items = List.of();
		state.contiguousItemCount = 0;
		state.animationTime = ClientTickHandler.ticksInGame + partialTicks;
		state.manaStarScale = blockEntity.getTargetMana() == 0 ? 0F
				: (float) blockEntity.getCurrentMana() / (float) blockEntity.getTargetMana() / 75F;
		state.manaStarSeed = blockEntity.getBlockPos().getX() ^ blockEntity.getBlockPos().getY() ^ blockEntity.getBlockPos().getZ();

		List<ItemStackRenderState> items = new ArrayList<>();
		boolean counting = true;
		for (int i = 0; i < blockEntity.inventorySize(); i++) {
			if (counting) {
				if (blockEntity.getItemHandler().getItem(i).isEmpty()) {
					counting = false;
				} else {
					state.contiguousItemCount++;
				}
			}
			ItemStackRenderState itemState = new ItemStackRenderState();
			itemModelResolver.updateForTopItem(itemState, blockEntity.getItemHandler().getItem(i),
					ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
			items.add(itemState);
		}
		state.items = List.copyOf(items);
	}

	@Override
	public void submit(RunicAltarRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		submitItems(state, poseStack, submitNodeCollector);
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		submitSpinningCubes(state, poseStack, submitNodeCollector, 2, 15);
		poseStack.popPose();
		poseStack.translate(0F, 0.2F, 0F);
		if (state.manaStarScale != 0F) {
			poseStack.translate(0.5F, 0.7F, 0.5F);
			RenderHelper.submitStar(poseStack, submitNodeCollector, 0x00E4D7,
					state.manaStarScale, state.manaStarScale, state.manaStarScale,
					state.manaStarSeed, (float) state.animationTime);
		}
		poseStack.popPose();
	}

	private static void submitItems(RunicAltarRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		if (state.contiguousItemCount <= 0) {
			return;
		}
		float anglePer = 360F / state.contiguousItemCount;
		float angle = 0F;
		for (int i = 0; i < state.items.size(); i++) {
			angle += anglePer;
			ItemStackRenderState item = state.items.get(i);
			if (item.isEmpty()) {
				continue;
			}
			poseStack.pushPose();
			poseStack.translate(0.5F, 1.25F, 0.5F);
			poseStack.mulPose(VecHelper.rotateY(angle + (float) state.animationTime));
			poseStack.translate(1.125F, 0F, 0.25F);
			poseStack.mulPose(VecHelper.rotateY(90F));
			poseStack.translate(0D, 0.075 * Math.sin((state.animationTime + i * 10) / 5D), 0F);
			item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	private void submitSpinningCubes(RunicAltarRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, int cubes, int iters) {
		for (int curIter = iters; curIter > 0; curIter--) {
			final float modifier = 6F;
			final float rotationModifier = 0.2F;
			final float radiusBase = 0.35F;
			final float radiusMod = 0.05F;
			double ticks = state.animationTime - 1.3 * (iters - curIter);
			float offsetPerCube = 360F / cubes;
			poseStack.pushPose();
			poseStack.translate(-0.025F, 0.85F, -0.025F);
			for (int i = 0; i < cubes; i++) {
				float offset = offsetPerCube * i;
				float deg = (int) (ticks / rotationModifier % 360F + offset);
				float rad = VecHelper.toRadians(deg);
				float radiusX = (float) (radiusBase + radiusMod * Math.sin(ticks / modifier));
				float radiusZ = (float) (radiusBase + radiusMod * Math.cos(ticks / modifier));
				float x = (float) (radiusX * Math.cos(rad));
				float z = (float) (radiusZ * Math.sin(rad));
				float y = (float) Math.cos((ticks + 50 * i) / 5F) / 10F;
				poseStack.pushPose();
				poseStack.translate(x, y, z);
				float xRotate = (float) Math.sin(ticks * rotationModifier) / 2F;
				float yRotate = (float) Math.max(0.6F, Math.sin(ticks * 0.1F) / 2F + 0.5F);
				float zRotate = (float) Math.cos(ticks * rotationModifier) / 2F;
				poseStack.mulPose(new Quaternionf().rotateAxis(rad, xRotate, yRotate, zRotate));
				float alpha = curIter < iters ? (float) curIter / (float) iters * 0.4F : 1F;
				int tintedColor = curIter < iters ? ((int) (alpha * 255F) << 24) | 0xFFFFFF : 0xFFFFFFFF;
				submitNodeCollector.submitModelPart(spinningCube, poseStack,
						curIter < iters ? RenderTypes.entityTranslucent(cubeTexture) : RenderTypes.entitySolid(cubeTexture),
						0xF000F0, OverlayTexture.NO_OVERLAY, null, false, false, tintedColor,
						state.breakProgress, 0);
				poseStack.popPose();
			}
			poseStack.popPose();
		}
	}
}
