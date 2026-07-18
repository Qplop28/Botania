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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.state.RedStringRenderState;
import vazkii.botania.common.block.block_entity.red_string.RedStringBlockEntity;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.WandOfTheForestItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RedStringBlockEntityRenderer<T extends RedStringBlockEntity> implements BlockEntityRenderer<T, RedStringRenderState> {
	// 0 -> none, 10 -> full
	private static int transparency = 0;

	public static void tick() {
		Player player = Minecraft.getInstance().player;
		boolean hasWand = player != null && PlayerHelper.hasHeldItemClass(player, WandOfTheForestItem.class);
		if (transparency > 0 && !hasWand) {
			transparency--;
		} else if (transparency < 10 && hasWand) {
			transparency++;
		}
	}

	public RedStringBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public RedStringRenderState createRenderState() {
		return new RedStringRenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, RedStringRenderState state, float partialTicks,
			Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(
				blockEntity,
				state,
				partialTicks,
				cameraPosition,
				breakProgress
		);
		state.vertices = List.of();
		state.alpha = 0;
		state.normalX = 0F;
		state.normalY = 0F;
		state.normalZ = 0F;

		int transparencySnapshot = transparency;
		if (transparencySnapshot <= 0) {
			return;
		}

		BlockPos binding = blockEntity.getBinding();
		if (binding == null) {
			return;
		}

		float sizeAlpha = transparencySnapshot / 10.0F;
		state.alpha = (int) (sizeAlpha * 255F);

		Direction direction = blockEntity.getOrientation();
		switch (direction.getAxis().getPlane()) {
			case HORIZONTAL -> {
				state.normalX = 0F;
				state.normalY = 1F;
				state.normalZ = 0F;
			}
			case VERTICAL -> {
				state.normalX = 1F;
				state.normalY = 0F;
				state.normalZ = 0F;
			}
		}

		Vec3 span = new Vec3(
				binding.getX() - blockEntity.getBlockPos().getX(),
				binding.getY() - blockEntity.getBlockPos().getY(),
				binding.getZ() - blockEntity.getBlockPos().getZ()
		);
		Vec3 step = span.normalize().scale(0.025);
		Vec3 current = step;

		int stepCount = (int) (span.length() / step.length());
		List<RedStringRenderState.Vertex> vertices = new ArrayList<>(stepCount * 2);

		double length = (double) -ClientTickHandler.ticksInGame / 100F
				+ new Random(
						direction.ordinal()
								^ blockEntity.getBlockPos().hashCode()
				).nextInt(10000);
		double add = step.length();
		double randomOffset = Math.random() - 0.5;
		for (int i = 0; i < stepCount; i++) {
			addVertex(vertices, direction, current.x, current.y, current.z, randomOffset, length, sizeAlpha);
			randomOffset = Math.random() - 0.5;
			current = current.add(step);
			length += add;
			addVertex(vertices, direction, current.x, current.y, current.z, randomOffset, length, sizeAlpha);
		}

		state.vertices = List.copyOf(vertices);
	}

	@Override
	public void submit(RedStringRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.vertices.isEmpty()) {
			return;
		}

		List<RedStringRenderState.Vertex> vertices = state.vertices;
		int alpha = state.alpha;
		float normalX = state.normalX;
		float normalY = state.normalY;
		float normalZ = state.normalZ;

		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);
		submitNodeCollector.submitCustomGeometry(
				poseStack,
				RenderHelper.RED_STRING,
				(pose, consumer) -> {
					for (RedStringRenderState.Vertex vertex : vertices) {
						consumer.addVertex(
								pose,
								vertex.x(),
								vertex.y(),
								vertex.z()
						)
								.setColor(255, 0, 0, alpha)
								.setNormal(
										pose,
										normalX,
										normalY,
										normalZ
								);
					}
				}
		);
		poseStack.popPose();
	}

	/**
	 * Add a vertex at the given position, but spiraled out perpendicular to {@code direction}
	 */
	private static void addVertex(List<RedStringRenderState.Vertex> vertices, Direction direction,
			double xPosition, double yPosition, double zPosition,
			double randomOffset, double length, float sizeAlpha) {
		float amplitude = (float) (0.15 * (Mth.sin((float) length * 2F) * 0.5 + 0.5) + 0.1) * sizeAlpha;

		float trigInput = (float) (length * 20.0);
		float sin = Mth.sin(trigInput);
		float cos = Mth.cos(trigInput);
		float lastTerm = (float) (randomOffset * 0.05);

		float x = (float) xPosition
				+ sin * amplitude * killNonZero(direction.getStepX())
				+ lastTerm;
		float y = (float) yPosition
				+ cos * amplitude * killNonZero(direction.getStepY())
				+ lastTerm;
		float z = (float) zPosition
				+ (direction.getStepY() == 0 ? sin : cos) * amplitude * killNonZero(direction.getStepZ())
				+ lastTerm;

		vertices.add(new RedStringRenderState.Vertex(x, y, z));
	}

	private static int killNonZero(int diff) {
		if (diff != 0) {
			return 0;
		} else {
			return 1;
		}
	}

}
