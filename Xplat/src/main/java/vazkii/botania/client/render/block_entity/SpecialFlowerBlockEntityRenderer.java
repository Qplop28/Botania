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
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.RadiusRenderData;
import vazkii.botania.client.render.block_entity.state.SpecialFlowerRenderState;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.equipment.bauble.ManaseerMonocleItem;

public class SpecialFlowerBlockEntityRenderer<T extends SpecialFlowerBlockEntity>
		implements BlockEntityRenderer<T, SpecialFlowerRenderState> {
	public static final int INNER_ALPHA = 32;
	public static final int OUTER_ALPHA = 64;
	public static final float FRAME_WIDTH = 1F / 16F;
	public static final float Y_OFFSET_INNER = 1F / 16F;
	public static final float Y_OFFSET_OUTER = FRAME_WIDTH + FRAME_WIDTH / 4F;
	public static final int TOTAL_ANGLES = 360;
	public static final double DEGREES_TO_RADIAN = Math.PI / (TOTAL_ANGLES / 2D);

	private final BlockModelResolver blockModelResolver;

	public SpecialFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
	}

	@Override
	public SpecialFlowerRenderState createRenderState() {
		return new SpecialFlowerRenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, SpecialFlowerRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.showRadii = false;
		state.bindingAttempt = false;
		state.primaryRadius = null;
		state.secondaryRadius = null;
		state.radiusColor = 0;

		state.floatingVisible = false;
		state.rotationYDegrees = 0F;
		state.translationY = 0F;
		state.tiltXDegrees = 0F;
		if (blockEntity.isFloating()) {
			FloatingFlowerBlockEntityRenderer.extractFloatingIsland(blockEntity, state, partialTicks, blockModelResolver);
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.cameraEntity instanceof LivingEntity view) || !ManaseerMonocleItem.hasMonocle(view)) {
			return;
		}

		BlockPos targetedPos = null;
		HitResult hitResult = minecraft.hitResult;
		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			targetedPos = ((BlockHitResult) hitResult).getBlockPos();
		}

		BlockPos blockPos = blockEntity.getBlockPos();
		boolean bindingAttempt = hasBindingAttempt(view, blockPos);
		if (bindingAttempt || blockPos.equals(targetedPos)) {
			state.showRadii = true;
			state.bindingAttempt = bindingAttempt;
			state.primaryRadius = RadiusRenderData.from(blockPos, blockEntity.getRadius());
			state.secondaryRadius = RadiusRenderData.from(blockPos, blockEntity.getSecondaryRadius());
			state.radiusColor = Mth.hsvToRgb(ClientTickHandler.ticksInGame % 200 / 200F, 0.6F, 1F);
		}
	}

	@Override
	public void submit(SpecialFlowerRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		FloatingFlowerBlockEntityRenderer.submitFloatingIsland(state, poseStack, submitNodeCollector);

		if (!state.showRadii) {
			return;
		}

		poseStack.pushPose();
		if (state.bindingAttempt) {
			poseStack.translate(0F, 0.005F, 0F);
		}
		submitRadius(state.primaryRadius, state.radiusColor, poseStack, submitNodeCollector);
		poseStack.translate(0F, 0.002F, 0F);
		submitRadius(state.secondaryRadius, state.radiusColor, poseStack, submitNodeCollector);
		poseStack.popPose();
	}

	public static void submitRadius(@Nullable RadiusRenderData radiusData, int radiusColor, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		if (radiusData == null) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0F, RenderHelper.getOffY(), 0F);
		if (radiusData instanceof RadiusRenderData.Circle circle) {
			submitCircle(circle, radiusColor, poseStack, submitNodeCollector);
		} else if (radiusData instanceof RadiusRenderData.Rectangle rectangle) {
			submitRectangle(rectangle, radiusColor, poseStack, submitNodeCollector);
		}
		RenderHelper.incrementOffY();
		poseStack.popPose();
	}

	public static boolean hasBindingAttempt(LivingEntity view, BlockPos tilePos) {
		ItemStack stackHeld = PlayerHelper.getFirstHeldItemClass(view, WandOfTheForestItem.class);
		if (!stackHeld.isEmpty() && WandOfTheForestItem.getBindMode(stackHeld)) {
			return WandOfTheForestItem.getBindingAttempt(stackHeld).filter(tilePos::equals).isPresent();
		}
		return false;
	}

	private static void submitCircle(RadiusRenderData.Circle circle, int radiusColor, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		poseStack.pushPose();
		poseStack.translate(circle.offsetX(), circle.offsetY(), circle.offsetZ());

		int r = radiusColor >> 16 & 0xFF;
		int g = radiusColor >> 8 & 0xFF;
		int b = radiusColor & 0xFF;
		float innerRadius = circle.radius() - FRAME_WIDTH;
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.CIRCLE, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			for (int i = 0; i < TOTAL_ANGLES; i++) {
				double currentRadians = (TOTAL_ANGLES - i) * DEGREES_TO_RADIAN;
				double nextRadians = (TOTAL_ANGLES - ((i + 1) % TOTAL_ANGLES)) * DEGREES_TO_RADIAN;
				submitCircleFanSegment(consumer, matrix, innerRadius, Y_OFFSET_INNER, INNER_ALPHA,
						currentRadians, nextRadians, r, g, b);
				submitCircleFanSegment(consumer, matrix, circle.radius(), Y_OFFSET_OUTER, OUTER_ALPHA,
						currentRadians, nextRadians, r, g, b);
			}
		});

		poseStack.popPose();
	}

	private static void submitCircleFanSegment(com.mojang.blaze3d.vertex.VertexConsumer consumer, Matrix4f matrix,
			float radius, float y, int alpha, double currentRadians, double nextRadians, int r, int g, int b) {
		consumer.addVertex(matrix, 0F, y, 0F).setColor(r, g, b, alpha);
		consumer.addVertex(matrix, (float) (Math.cos(currentRadians) * radius), y,
				(float) (Math.sin(currentRadians) * radius)).setColor(r, g, b, alpha);
		consumer.addVertex(matrix, (float) (Math.cos(nextRadians) * radius), y,
				(float) (Math.sin(nextRadians) * radius)).setColor(r, g, b, alpha);
	}

	private static void submitRectangle(RadiusRenderData.Rectangle rectangle, int radiusColor, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector) {
		poseStack.pushPose();
		poseStack.translate(rectangle.offsetX(), rectangle.offsetY(), rectangle.offsetZ());

		int r = radiusColor >> 16 & 0xFF;
		int g = radiusColor >> 8 & 0xFF;
		int b = radiusColor & 0xFF;
		float xSizeInner = rectangle.xSize() - FRAME_WIDTH;
		float zSizeInner = rectangle.zSize() - FRAME_WIDTH;
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.RECTANGLE, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			submitFlatRectangle(consumer, matrix, FRAME_WIDTH, xSizeInner, Y_OFFSET_INNER, FRAME_WIDTH, zSizeInner,
					r, g, b, INNER_ALPHA);
			submitFlatRectangle(consumer, matrix, 0F, rectangle.xSize(), Y_OFFSET_OUTER, 0F, rectangle.zSize(),
					r, g, b, OUTER_ALPHA);
		});

		poseStack.popPose();
	}

	private static void submitFlatRectangle(com.mojang.blaze3d.vertex.VertexConsumer consumer, Matrix4f matrix,
			float xMin, float xMax, float y, float zMin, float zMax, int r, int g, int b, int alpha) {
		consumer.addVertex(matrix, xMax, y, zMin).setColor(r, g, b, alpha);
		consumer.addVertex(matrix, xMin, y, zMin).setColor(r, g, b, alpha);
		consumer.addVertex(matrix, xMin, y, zMax).setColor(r, g, b, alpha);
		consumer.addVertex(matrix, xMax, y, zMax).setColor(r, g, b, alpha);
	}
}
