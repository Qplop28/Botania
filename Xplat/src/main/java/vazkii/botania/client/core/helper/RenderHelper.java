/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import static net.minecraft.util.ARGB.color;

import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public final class RenderHelper {
	private static final RenderPipeline STAR_PIPELINE = pipeline("star", null,
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, BlendFunction.LIGHTNING, true,
			CompareOp.LESS_THAN_OR_EQUAL, false, 1);
	private static final RenderPipeline HIGHLIGHT_QUADS_PIPELINE = pipeline("rectangle_highlight", null,
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BlendFunction.TRANSLUCENT, false,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);
	private static final RenderPipeline HIGHLIGHT_TRIANGLES_PIPELINE = pipeline("circle_highlight", null,
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, BlendFunction.TRANSLUCENT, false,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);
	private static final RenderPipeline LINE_PIPELINE = linePipeline("red_string", 1, false);
	private static final RenderPipeline LINE_1_NO_DEPTH_PIPELINE = linePipeline("line_1_no_depth", 1, true);
	private static final RenderPipeline LINE_4_NO_DEPTH_PIPELINE = linePipeline("line_4_no_depth", 4, true);
	private static final RenderPipeline LINE_5_NO_DEPTH_PIPELINE = linePipeline("line_5_no_depth", 5, true);
	private static final RenderPipeline LINE_8_NO_DEPTH_PIPELINE = linePipeline("line_8_no_depth", 8, true);
	private static final RenderPipeline SPARK_PIPELINE = pipeline("spark", null,
			CoreShaders.POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, BlendFunction.TRANSLUCENT, true,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);
	private static final RenderPipeline ICON_OVERLAY_PIPELINE = pipeline("icon_overlay", null,
			CoreShaders.POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, BlendFunction.TRANSLUCENT, true,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);
	private static final RenderPipeline LIGHTNING_PIPELINE = pipeline("lightning", null,
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BlendFunction.LIGHTNING, true,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);
	private static final RenderPipeline ASTROLABE_PIPELINE = pipeline("astrolabe", null,
			DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS, BlendFunction.TRANSLUCENT, true,
			CompareOp.LESS_THAN_OR_EQUAL, true, 1);

	private static final RenderType STAR = layer("star", STAR_PIPELINE, 256, false, false, null);
	public static RenderType starRenderType() {
		return STAR;
	}

	public static final RenderType RECTANGLE = layer("rectangle_highlight", HIGHLIGHT_QUADS_PIPELINE, 256, false, true, null);
	public static final RenderType CIRCLE = layer("circle_highlight", HIGHLIGHT_TRIANGLES_PIPELINE, 256, false, false, null);
	public static final RenderType RED_STRING = lineLayer("red_string", LINE_PIPELINE, 128, false);
	public static final RenderType LINE_1_NO_DEPTH = lineLayer("line_1_no_depth", LINE_1_NO_DEPTH_PIPELINE, 128, true);
	public static final RenderType LINE_4_NO_DEPTH = lineLayer("line_4_no_depth", LINE_4_NO_DEPTH_PIPELINE, 128, true);
	public static final RenderType LINE_5_NO_DEPTH = lineLayer("line_5_no_depth", LINE_5_NO_DEPTH_PIPELINE, 64, true);
	public static final RenderType LINE_8_NO_DEPTH = lineLayer("line_8_no_depth", LINE_8_NO_DEPTH_PIPELINE, 64, true);
	public static final RenderType SPARK = layer("spark", SPARK_PIPELINE, 256, false, false, TextureAtlas.LOCATION_BLOCKS, true, false, true);
	public static final RenderType LIGHT_RELAY = layer("light_relay", CoreShaders.halo(), 64, false, false, TextureAtlas.LOCATION_BLOCKS);
	public static final RenderType ICON_OVERLAY = layer("icon_overlay", ICON_OVERLAY_PIPELINE, 128, false, false, TextureAtlas.LOCATION_BLOCKS, true, false, true);
	public static final RenderType BABYLON_ICON = layer("babylon", CoreShaders.halo(), 64, false, false, Identifier.parse(ResourcesLib.MISC_BABYLON));
	public static final RenderType MANA_POOL_WATER = layer("mana_pool_water", CoreShaders.manaPool(), 128, false, false, TextureAtlas.LOCATION_BLOCKS, true, false, false);
	public static final RenderType TERRA_PLATE = layer("terra_plate_rune", CoreShaders.terraPlate(), 128, false, false, TextureAtlas.LOCATION_BLOCKS, true, false, false);
	public static final RenderType ENCHANTER = layer("enchanter_rune", CoreShaders.enchanter(), 128, false, false, TextureAtlas.LOCATION_BLOCKS, true, false, false);
	public static final RenderType HALO = layer("halo", CoreShaders.halo(), 64, false, false, FlugelTiaraItem.textureHalo);
	public static final RenderType MANA_PYLON_GLOW = getPylonGlow("mana_pylon_glow", PylonBlockEntityRenderer.MANA_TEXTURE);
	public static final RenderType NATURA_PYLON_GLOW = getPylonGlow("natura_pylon_glow", PylonBlockEntityRenderer.NATURA_TEXTURE);
	public static final RenderType GAIA_PYLON_GLOW = getPylonGlow("gaia_pylon_glow", PylonBlockEntityRenderer.GAIA_TEXTURE);
	public static final RenderType MANA_PYLON_GLOW_DIRECT = getPylonGlowDirect("mana_pylon_glow_direct", PylonBlockEntityRenderer.MANA_TEXTURE);
	public static final RenderType NATURA_PYLON_GLOW_DIRECT = getPylonGlowDirect("natura_pylon_glow_direct", PylonBlockEntityRenderer.NATURA_TEXTURE);
	public static final RenderType GAIA_PYLON_GLOW_DIRECT = getPylonGlowDirect("gaia_pylon_glow_direct", PylonBlockEntityRenderer.GAIA_TEXTURE);
	public static final RenderType ASTROLABE_PREVIEW = layer("astrolabe", ASTROLABE_PIPELINE, 256, true, true, TextureAtlas.LOCATION_BLOCKS);
	public static final RenderType STARFIELD = starfield();
	public static final RenderType LIGHTNING = layer("lightning", LIGHTNING_PIPELINE, 256, false, true, null);
	public static final RenderType TRANSLUCENT = RenderTypes.entityTranslucentCullItemTarget(TextureAtlas.LOCATION_BLOCKS);

	private static final int ITEM_AND_PADDING_WIDTH = 20;
	private static final double INITIAL_OFFSET = 0.005;
	private static final double OFFSET_INCREMENT = 0.001;
	private static double offY = INITIAL_OFFSET;

	private static RenderPipeline pipeline(String name, RenderPipeline.Snippet snippet, VertexFormat format,
			VertexFormat.Mode mode, BlendFunction blend, boolean cull, CompareOp depthCompare,
			boolean depthWrite, int lineWidth) {
		var builder = snippet == null
				? RenderPipeline.builder()
						.withVertexShader(format == DefaultVertexFormat.ENTITY
								? "core/rendertype_entity_translucent" : format == CoreShaders.POSITION_TEX_COLOR_LIGHTMAP
										? "core/position_tex_color_lightmap" : "core/position_color")
						.withFragmentShader(format == DefaultVertexFormat.ENTITY
								? "core/rendertype_entity_translucent" : format == CoreShaders.POSITION_TEX_COLOR_LIGHTMAP
										? "core/position_tex_color_lightmap" : "core/position_color")
				: RenderPipeline.builder(snippet);
		builder = builder.withLocation(Identifier.parse(ResourcesLib.PREFIX_MOD + name))
				.withVertexFormat(format, mode)
				.withCull(cull)
				.withDepthStencilState(new DepthStencilState(depthCompare, depthWrite, 0, 0))
				.withColorTargetState(new ColorTargetState(Optional.of(blend),
						ColorTargetState.WRITE_RED | ColorTargetState.WRITE_GREEN | ColorTargetState.WRITE_BLUE | ColorTargetState.WRITE_ALPHA));
		if (lineWidth != 1) {
			builder = builder.withShaderDefine("LINE_WIDTH", lineWidth);
		}
		return RenderPipelines.register(builder.build());
	}

	private static RenderPipeline linePipeline(String name, int width, boolean noDepth) {
		return pipeline(name, RenderPipelines.LINES_SNIPPET, DefaultVertexFormat.POSITION_COLOR_NORMAL,
				VertexFormat.Mode.LINES, BlendFunction.TRANSLUCENT, false,
				noDepth ? CompareOp.ALWAYS_PASS : CompareOp.LESS_THAN_OR_EQUAL, !noDepth, width);
	}

	private static RenderType layer(String name, RenderPipeline pipeline, int bufferSize,
			boolean crumbling, boolean sorted, Identifier texture) {
		return layer(name, pipeline, bufferSize, crumbling, sorted, texture, false, false, false);
	}

	private static RenderType layer(String name, RenderPipeline pipeline, int bufferSize,
			boolean crumbling, boolean sorted, Identifier texture, boolean lightmap, boolean overlay, boolean itemTarget) {
		var builder = RenderSetup.builder(pipeline).bufferSize(bufferSize);
		if (texture != null) {
			builder = builder.withTexture("Sampler0", texture);
		}
		if (lightmap) {
			builder = builder.useLightmap();
		}
		if (overlay) {
			builder = builder.useOverlay();
		}
		if (crumbling) {
			builder = builder.affectsCrumbling();
		}
		if (sorted) {
			builder = builder.sortOnUpload();
		}
		if (itemTarget) {
			builder = builder.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET);
		}
		return RenderType.create(ResourcesLib.PREFIX_MOD + name, builder.createRenderSetup());
	}

	private static RenderType lineLayer(String name, RenderPipeline pipeline, int size, boolean direct) {
		var builder = RenderSetup.builder(pipeline).bufferSize(size).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING);
		if (!direct) {
			builder = builder.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET);
		}
		return RenderType.create(ResourcesLib.PREFIX_MOD + name, builder.createRenderSetup());
	}

	private static RenderType starfield() {
		var setup = RenderSetup.builder(CoreShaders.STARFIELD)
				.withTexture("Sampler0", TheEndPortalRenderer.END_SKY_LOCATION)
				.withTexture("Sampler1", TheEndPortalRenderer.END_PORTAL_LOCATION)
				.bufferSize(256)
				.createRenderSetup();
		return RenderType.create(ResourcesLib.PREFIX_MOD + "starfield", setup);
	}

	public static double getOffY() {
		return offY;
	}

	public static void incrementOffY() {
		offY += OFFSET_INCREMENT;
	}

	public static void onWorldRenderLast() {
		offY = INITIAL_OFFSET;
	}

	private static RenderType getPylonGlowDirect(String name, Identifier texture) {
		return getPylonGlow(name, texture, true);
	}

	private static RenderType getPylonGlow(String name, Identifier texture) {
		return getPylonGlow(name, texture, false);
	}

	private static RenderType getPylonGlow(String name, Identifier texture, boolean direct) {
		return layer(name, CoreShaders.pylon(), 128, false, false, texture, true, true, !direct);
	}

	public static RenderType getHaloLayer(Identifier texture) {
		return layer("crafting_halo", CoreShaders.halo(), 64, false, true, texture);
	}

	private static final Function<Identifier, RenderType> DOPPLEGANGER = Util.memoize(texture ->
			layer("doppleganger", CoreShaders.doppleganger(), 256, true, true, texture, true, true, false));

	public static RenderType getDopplegangerLayer(Identifier texture) {
		return DOPPLEGANGER.apply(texture);
	}
	public static void drawTexturedModalRect(GuiGraphicsExtractor gui, Identifier textureId, int x, int y, int u, int v, int width, int height) {
		gui.blit(textureId, x, y, u, v, width, height, 256, 256);
	}

	public static void renderStar(PoseStack ms, MultiBufferSource buffers, int color, float xScale, float yScale, float zScale, long seed) {
		renderStar(ms, buffers.getBuffer(STAR), color, xScale, yScale, zScale, seed);
	}

	public static void submitStar(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int color,
			float xScale, float yScale, float zScale, long seed, float animationTicks) {
		submitNodeCollector.submitCustomGeometry(poseStack, STAR, (pose, consumer) -> {
			PoseStack localPoseStack = new PoseStack();
			localPoseStack.last().set(pose);
			renderStar(localPoseStack, consumer, color, xScale, yScale, zScale, seed, animationTicks);
		});
	}

	public static void renderStar(PoseStack ms, VertexConsumer buffer, int color, float xScale, float yScale, float zScale, long seed) {
		renderStar(ms, buffer, color, xScale, yScale, zScale, seed, ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks);
	}

	public static void renderStar(PoseStack ms, VertexConsumer buffer, int color, float xScale, float yScale, float zScale, long seed, float ticks) {
		float semiPeriodTicks = 200;
		float f1 = Mth.abs(Mth.sin((float) Math.PI / semiPeriodTicks * ticks))
				* 0.9F + 0.1F; // shift to [0.1, 1.0]

		float f2 = f1 > 0.F ? (f1 - 0.7F) / 0.2F : 0;
		Random random = new Random(seed);

		ms.pushPose();
		ms.scale(xScale, yScale, zScale);

		for (int i = 0; i < (f1 + f1 * f1) / 2F * 90F + 30F; i++) {
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F + f1 * 90F));
			float f3 = random.nextFloat() * 20F + 5F + f2 * 10F;
			float f4 = random.nextFloat() * 2F + 1F + f2 * 2F;
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = (color & 0xFF) / 255F;
			Matrix4f mat = ms.last().pose();
			Runnable center = () -> buffer.addVertex(mat, 0, 0, 0).setColor(r, g, b, f1);
			Runnable[] vertices = {
					() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, 0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, 0, f3, 1F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0)
			};
			triangleFan(center, vertices);
		}

		ms.popPose();
	}

	public static void triangleFan(Runnable center, Runnable... vertices) {
		triangleFan(center, Arrays.asList(vertices));
	}

	/**
	 * With a buffer in GL_TRIANGLES mode, emulates GL_TRIANGLE_FAN on the CPU.
	 * This is because batching of GL_TRIANGLE_FAN makes no sense (the vertices would bleed into one massive fan)
	 */
	public static void triangleFan(Runnable center, List<Runnable> vertices) {
		for (int i = 0; i < vertices.size() - 1; i++) {
			center.run();
			vertices.get(i).run();
			vertices.get(i + 1).run();
		}
	}

	public static void flatRectangle(VertexConsumer buffer, Matrix4f mat,
			float xMin, float xMax, float y, float zMin, float zMax,
			int r, int g, int b, int a) {
		buffer.addVertex(mat, xMax, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMax).setColor(r, g, b, a);
		buffer.addVertex(mat, xMax, y, zMax).setColor(r, g, b, a);
	}

	public static void renderProgressPie(GuiGraphicsExtractor gui, int x, int y, float progress, ItemStack stack) {
		gui.item(stack, x, y);

		// GUI rendering is extracted into immutable render-state nodes in 26.1.2. Represent the
		// progress without mutating global depth/stencil state, which is no longer safe here.
		int covered = Mth.clamp(Mth.ceil(16F * progress), 0, 16);
		if (covered > 0) {
			float pulse = 0.5F + 0.2F * ((float) Math.cos(
					(ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) / 10.0) * 0.5F + 0.5F);
			int alpha = Mth.clamp((int) (pulse * 255F), 0, 255);
			gui.fill(x, y + 16 - covered, x + 16, y + 16, ARGB.color(alpha, 0, 255, 128));
		}
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 *
	 * @param startX   Start x position in blocks
	 * @param startY   Start position in blocks
	 * @param endX     End x position in blocks
	 * @param endY     End y position in blocks
	 *
	 * @param uvStartX UV start x position in "pixels" (1/16th sprite size)
	 * @param uvStartY UV start position in "pixels" (1/16th sprite size)
	 * @param uvEndX   UV end x position in "pixels" (1/16th sprite size)
	 * @param uvEndY   UV end y position in "pixels" (1/16th sprite size)
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			float startX, float startY, float endX, float endY,
			int uvStartX, int uvStartY, int uvEndX, int uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		Matrix4f mat = ms.last().pose();
		float red = ((color >> 16) & 0xFF) / 255F;
		float green = ((color >> 8) & 0xFF) / 255F;
		float blue = (color & 0xFF) / 255F;

		buffer.addVertex(mat, startX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvEndY)).setLight(light);
		buffer.addVertex(mat, endX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvEndY)).setLight(light);
		buffer.addVertex(mat, endX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvStartY)).setLight(light);
		buffer.addVertex(mat, startX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvStartY)).setLight(light);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 *
	 * @param uvStartX UV start x position in "pixels" (1/16th sprite size)
	 * @param uvStartY UV start position in "pixels" (1/16th sprite size)
	 * @param uvEndX   UV end x position in "pixels" (1/16th sprite size)
	 * @param uvEndY   UV end y position in "pixels" (1/16th sprite size)
	 */
	public static void renderIconCropped(
			PoseStack ms, VertexConsumer buffer,
			int uvStartX, int uvStartY, int uvEndX, int uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconFullBright(
				ms, buffer,
				uvStartX / 16F, uvStartY / 16F, uvEndX / 16F, uvEndY / 16F,
				uvStartX, uvStartY, uvEndX, uvEndY,
				icon, color, alpha, light
		);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconCropped(
				ms, buffer,
				0, 0, 16, 16,
				icon, color, alpha, light
		);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon in fullbright, at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha) {
		int fullbright = 0xF000F0;
		renderIconFullBright(ms, buffer, icon, color, alpha, fullbright);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon in fullbright, with no color modification, at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, float alpha) {
		renderIconFullBright(ms, buffer, icon, 0xFFFFFF, alpha);
	}

	private static MultiBufferSource wrapBuffer(MultiBufferSource buffer, int alpha, boolean forceTranslucent) {
		return renderType -> new GhostVertexConsumer(buffer.getBuffer(forceTranslucent ? TRANSLUCENT : renderType), alpha);
	}

	/*
	* Renders a transparent black box with a soft border. The parameters describe the inner box, there will also be drawn
	* another box that is 2px bigger in each direction
	*/
	public static void renderHUDBox(GuiGraphicsExtractor gui, int startX, int startY, int endX, int endY) {
		gui.fill(startX, startY, endX, endY, 0x40000000);
		gui.fill(startX - 2, startY - 2, endX + 2, endY + 2, 0x40000000);
	}

	/*
	* Renders an item and its name, vertically centered next to it. Renders nothing if the stack is empty
	* Note: The item renderer does not respect the PoseStack
	*/
	public static void renderItemWithName(GuiGraphicsExtractor gui, Minecraft mc, ItemStack itemStack, int startX, int startY, int color) {
		if (!itemStack.isEmpty()) {
			gui.text(mc.font, itemStack.getHoverName(), startX + ITEM_AND_PADDING_WIDTH, startY + 4, color);
			gui.item(itemStack, startX, startY);
		}
	}

	public static void renderItemWithNameCentered(GuiGraphicsExtractor gui, Minecraft mc, ItemStack itemStack, int startY, int color) {
		int centerX = mc.getWindow().getGuiScaledWidth() / 2;
		int startX = centerX - (ITEM_AND_PADDING_WIDTH + mc.font.width(itemStack.getHoverName())) / 2;
		renderItemWithName(gui, mc, itemStack, startX, startY, color);
	}

	/*
	* Returns the width of an item and its text, as rendered by renderItemWithName
	*/
	public static int itemWithNameWidth(Minecraft mc, ItemStack itemStack) {
		return ITEM_AND_PADDING_WIDTH + mc.font.width(itemStack.getHoverName());
	}

	// Borrowed with permission from https://github.com/XFactHD/FramedBlocks/blob/14f468810fc416b39447512810f0aa86e1012335/src/main/java/xfacthd/framedblocks/client/util/GhostVertexConsumer.java
	public record GhostVertexConsumer(VertexConsumer wrapped, int alpha) implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			wrapped.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			wrapped.setColor(red, green, blue, alpha * this.alpha / 0xFF);
			return this;
		}

		@Override
		public VertexConsumer setColor(int color) {
			int adjustedAlpha = ARGB.alpha(color) * this.alpha / 0xFF;
			wrapped.setColor(color(adjustedAlpha, color));
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			wrapped.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			wrapped.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			wrapped.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			wrapped.setNormal(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			wrapped.setLineWidth(width);
			return this;
		}
	}
}
