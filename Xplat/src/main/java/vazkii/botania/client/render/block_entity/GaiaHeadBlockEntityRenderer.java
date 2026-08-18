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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.GaiaGuardianRenderer;
import vazkii.botania.common.block.block_entity.GaiaHeadBlockEntity;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.Map;
import java.util.Objects;

public class GaiaHeadBlockEntityRenderer
		implements BlockEntityRenderer<GaiaHeadBlockEntity, SkullBlockRenderState> {
	private static final Identifier SKELETON_TEXTURE = vanilla("textures/entity/skeleton/skeleton.png");
	private static final Identifier WITHER_SKELETON_TEXTURE = vanilla("textures/entity/skeleton/wither_skeleton.png");
	private static final Identifier ZOMBIE_TEXTURE = vanilla("textures/entity/zombie/zombie.png");
	private static final Identifier CREEPER_TEXTURE = vanilla("textures/entity/creeper/creeper.png");
	private static final Identifier DRAGON_TEXTURE = vanilla("textures/entity/enderdragon/dragon.png");

	private final Map<SkullBlock.Type, SkullModelBase> models;

	public GaiaHeadBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		var modelSet = context.entityModelSet();
		models = Map.of(
				SkullBlock.Types.PLAYER, requireModel(modelSet, SkullBlock.Types.PLAYER),
				SkullBlock.Types.SKELETON, requireModel(modelSet, SkullBlock.Types.SKELETON),
				SkullBlock.Types.WITHER_SKELETON, requireModel(modelSet, SkullBlock.Types.WITHER_SKELETON),
				SkullBlock.Types.ZOMBIE, requireModel(modelSet, SkullBlock.Types.ZOMBIE),
				SkullBlock.Types.CREEPER, requireModel(modelSet, SkullBlock.Types.CREEPER),
				SkullBlock.Types.DRAGON, requireModel(modelSet, SkullBlock.Types.DRAGON));
	}

	@Override
	public SkullBlockRenderState createRenderState() {
		return new SkullBlockRenderState();
	}

	@Override
	public void extractRenderState(GaiaHeadBlockEntity blockEntity, SkullBlockRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.animationProgress = blockEntity.getAnimation(partialTicks);
		BlockState blockState = blockEntity.getBlockState();
		var wallFacing = blockState.getBlock() instanceof WallSkullBlock
				? blockState.getValue(WallSkullBlock.FACING) : null;
		int rotation = wallFacing == null ? blockState.getValue(SkullBlock.ROTATION) : 0;
		state.transformation = wallFacing == null
				? SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(rotation)
				: SkullBlockRenderer.TRANSFORMATIONS.wallTransformation(wallFacing);

		Entity view = Minecraft.getInstance().getCameraEntity();
		state.skullType = getViewType(view);
		state.renderType = RenderHelper.getDopplegangerLayer(getViewTexture(view, state.skullType));
	}

	@Override
	public void submit(SkullBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		SkullModelBase model = Objects.requireNonNull(models.get(state.skullType),
				"Missing cached Gaia head view model for " + state.skullType);
		SkullModelBase.State modelState = new SkullModelBase.State();
		modelState.animationPos = state.animationProgress;
		int shaderTint = BotaniaConfig.client().useShaders()
				? 0xFF000000
						| Math.round(GaiaGuardianRenderer.DEFAULT_GRAIN_INTENSITY * 255F) << 16
						| Math.round(GaiaGuardianRenderer.DEFAULT_DISFIGURATION * 255F) << 8
						| 0xFF
				: -1;

		poseStack.pushPose();
		poseStack.mulPose(state.transformation);
		collector.submitModel(model, modelState, poseStack, state.renderType, state.lightCoords,
				OverlayTexture.NO_OVERLAY, shaderTint, null, 0, state.breakProgress);
		poseStack.popPose();
	}

	private static SkullBlock.Type getViewType(@Nullable Entity view) {
		if (view instanceof WitherSkeleton || view instanceof WitherBoss) {
			return SkullBlock.Types.WITHER_SKELETON;
		} else if (view instanceof Skeleton) {
			return SkullBlock.Types.SKELETON;
		} else if (view instanceof Zombie) {
			return SkullBlock.Types.ZOMBIE;
		} else if (view instanceof Creeper) {
			return SkullBlock.Types.CREEPER;
		} else if (view instanceof EnderDragon) {
			return SkullBlock.Types.DRAGON;
		}
		return SkullBlock.Types.PLAYER;
	}

	private static Identifier getViewTexture(@Nullable Entity view, SkullBlock.Type type) {
		if (view instanceof AbstractClientPlayer player) {
			return player.getSkin().body().texturePath();
		}
		if (type == SkullBlock.Types.SKELETON) {
			return SKELETON_TEXTURE;
		} else if (type == SkullBlock.Types.WITHER_SKELETON) {
			return WITHER_SKELETON_TEXTURE;
		} else if (type == SkullBlock.Types.ZOMBIE) {
			return ZOMBIE_TEXTURE;
		} else if (type == SkullBlock.Types.CREEPER) {
			return CREEPER_TEXTURE;
		} else if (type == SkullBlock.Types.DRAGON) {
			return DRAGON_TEXTURE;
		}
		return DefaultPlayerSkin.getDefaultSkin().body().texturePath();
	}

	private static SkullModelBase requireModel(EntityModelSet modelSet,
			SkullBlock.Type type) {
		return Objects.requireNonNull(SkullBlockRenderer.createModel(modelSet, type),
				"Missing vanilla skull model for Gaia head view type " + type);
	}

	private static Identifier vanilla(String path) {
		return Identifier.withDefaultNamespace(path);
	}
}
