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
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.state.SparkRenderState;
import vazkii.botania.common.entity.SparkBaseEntity;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Objects;
import java.util.Random;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public abstract class BaseSparkRenderer<T extends SparkBaseEntity> extends EntityRenderer<T, SparkRenderState> {
	private final TextureAtlasSprite starSprite;
	private final TextureAtlasSprite worldSprite;

	public BaseSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
		this.starSprite = Objects.requireNonNull(atlas.apply(prefix("item/corporea_spark_star")));
		this.worldSprite = Objects.requireNonNull(atlas.apply(prefix("item/spark")));
	}

	@Override
	public SparkRenderState createRenderState() {
		return new SparkRenderState();
	}

	@Override
	public void extractRenderState(T entity, SparkRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.baseSprite = getBaseIcon(entity);
		state.spinningSprite = getSpinningIcon(entity);
		state.animationTime = entity.level().getGameTime() % 24000 + partialTicks
				+ new Random(entity.getId()).nextInt(200);
		state.networkColor = ColorHelper.getColorValue(entity.getNetwork());
	}

	@Override
	public void submit(SparkRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState camera) {
		ClientXplatAbstractions.instance().markSpriteActive(state.baseSprite);
		ClientXplatAbstractions.instance().markSpriteActive(this.starSprite);
		if (state.spinningSprite != null) {
			ClientXplatAbstractions.instance().markSpriteActive(state.spinningSprite);
		}

		double time = state.animationTime;
		float visibility = 0.1F + (state.isInvisible ? 0 : 1) * 0.8F;
		int alpha = (int) ((0.7 + 0.3 * (Math.sin(time / 5.0) + 0.5) * 2) * visibility * 255.0);
		int iconColor = 0xFFFFFF | alpha << 24;
		float scale = 0.75F + 0.1F * (float) Math.sin(time / 10);

		poseStack.pushPose();
		poseStack.scale(scale, scale, scale);
		poseStack.mulPose(camera.orientation);
		poseStack.mulPose(VecHelper.rotateY(180));
		submitNodeCollector.submitCustomGeometry(poseStack, RenderHelper.SPARK, (pose, consumer) -> {
			renderIcon(pose.pose(), consumer, state.baseSprite, iconColor);
			Matrix4f orbit = new Matrix4f(pose.pose())
					.translate((float) (-0.02 + Math.sin(time / 20) * 0.2),
							(float) (0.24 + Math.cos(time / 20) * 0.2), 0.005F)
					.scale(0.2F);
			int starColor = state.networkColor | (int) (visibility * 255.0F) << 24;
			renderIcon(orbit, consumer, this.starSprite, starColor);
			if (state.spinningSprite != null) {
				Matrix4f spinning = new Matrix4f(pose.pose())
						.translate((float) (-0.02 + Math.sin(time / 20) * -0.2),
								(float) (0.24 + Math.cos(time / 20) * -0.2), 0.005F)
						.scale(0.2F);
				renderIcon(spinning, consumer, state.spinningSprite, iconColor);
			}
		});
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	protected TextureAtlasSprite getBaseIcon(T entity) { return this.worldSprite; }
	@Nullable
	protected TextureAtlasSprite getSpinningIcon(T entity) { return null; }

	private static void renderIcon(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite icon, int color) {
		int a = color >>> 24;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		buffer.addVertex(matrix, -0.5F, -0.25F, 0).setColor(r, g, b, a).setUv(icon.getU0(), icon.getV1()).setLight(0xF000F0);
		buffer.addVertex(matrix, 0.5F, -0.25F, 0).setColor(r, g, b, a).setUv(icon.getU1(), icon.getV1()).setLight(0xF000F0);
		buffer.addVertex(matrix, 0.5F, 0.75F, 0).setColor(r, g, b, a).setUv(icon.getU1(), icon.getV0()).setLight(0xF000F0);
		buffer.addVertex(matrix, -0.5F, 0.75F, 0).setColor(r, g, b, a).setUv(icon.getU0(), icon.getV0()).setLight(0xF000F0);
	}
}
