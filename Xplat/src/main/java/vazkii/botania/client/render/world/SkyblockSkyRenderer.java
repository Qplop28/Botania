/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.render.world;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderSetup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.helper.VecHelper;

import java.util.Optional;
import java.util.Random;

/** Garden of Glass sky submissions for the renderer's modern buffered path. */
public final class SkyblockSkyRenderer {
	private static final Identifier SKYBOX = new Identifier(ResourcesLib.MISC_SKYBOX);
	private static final Identifier RAINBOW = new Identifier(ResourcesLib.MISC_RAINBOW);
	private static final Identifier[] PLANETS = {
			new Identifier(ResourcesLib.MISC_PLANET + "0.png"), new Identifier(ResourcesLib.MISC_PLANET + "1.png"),
			new Identifier(ResourcesLib.MISC_PLANET + "2.png"), new Identifier(ResourcesLib.MISC_PLANET + "3.png"),
			new Identifier(ResourcesLib.MISC_PLANET + "4.png"), new Identifier(ResourcesLib.MISC_PLANET + "5.png")
	};

	private static final RenderPipeline ALPHA_TEXTURE = pipeline("garden_sky_alpha", RenderPipelines.POSITION_TEX_SNIPPET,
			DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, BlendFunction.TRANSLUCENT);
	private static final RenderPipeline ADDITIVE_TEXTURE = pipeline("garden_sky_additive", RenderPipelines.POSITION_TEX_SNIPPET,
			DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, BlendFunction.LIGHTNING);
	private static final RenderPipeline ADDITIVE_COLOR = pipeline("garden_stars", RenderPipelines.POSITION_COLOR_SNIPPET,
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BlendFunction.LIGHTNING);

	private SkyblockSkyRenderer() {}

	private static RenderPipeline pipeline(String name, RenderPipeline.Snippet snippet, VertexFormat format,
			VertexFormat.Mode mode, BlendFunction blend) {
		return RenderPipelines.register(RenderPipeline.builder(snippet)
				.withLocation(new Identifier(ResourcesLib.PREFIX_MOD + name))
				.withVertexFormat(format, mode).withCull(false)
				.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false, 0, 0))
				.withColorTargetState(new ColorTargetState(Optional.of(blend), ColorTargetState.WRITE_RED
						| ColorTargetState.WRITE_GREEN | ColorTargetState.WRITE_BLUE | ColorTargetState.WRITE_ALPHA))
				.build());
	}

	private static RenderType layer(String name, RenderPipeline pipeline, Identifier texture) {
		var setup = RenderSetup.builder(pipeline).bufferSize(131072);
		if (texture != null) {
			setup = setup.withTexture("Sampler0", texture);
		}
		return RenderType.create(name, setup.createRenderSetup());
	}

	public static void renderExtra(PoseStack pose, ClientLevel level, float partialTick, float insideVoid) {
		float rain = 1F - level.getRainLevel(partialTick);
		float dayAngle = level.getTimeOfDay(partialTick);
		float effectiveAngle = dayAngle > .5F ? 1F - dayAngle : dayAngle;
		float lowAlpha = Math.max(0F, effectiveAngle - .3F) * rain;
		var buffers = Minecraft.getInstance().renderBuffers().bufferSource();

		pose.pushPose();
		pose.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(90), .5F, .5F, 0));
		float scale = 20;
		int planetColor = ARGB.colorFromFloat(Math.max(.1F, lowAlpha) * 4 * (1 - insideVoid), 1, 1, 1);
		for (int i = 0; i < PLANETS.length; i++) {
			quad(buffers.getBuffer(layer("botania_planet_" + i, ALPHA_TEXTURE, PLANETS[i])), pose.last().pose(), scale, planetColor);
			buffers.endBatch();
			switch (i) {
				case 0 -> { pose.mulPose(VecHelper.rotateX(70)); scale = 12; }
				case 1 -> { pose.mulPose(VecHelper.rotateZ(120)); scale = 15; }
				case 2 -> { pose.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(80), 1, 0, 1)); scale = 25; }
				case 3 -> { pose.mulPose(VecHelper.rotateZ(100)); scale = 10; }
				case 4 -> { pose.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(-60), 1, 0, .5F)); scale = 40; }
			}
		}
		pose.popPose();

		pose.pushPose();
		pose.translate(0, -1, 0);
		pose.mulPose(VecHelper.rotateX(220));
		float fuzz = (float) Math.PI * 10 / 90;
		float speed = 1;
		int[] colors = { ARGB.colorFromFloat(lowAlpha, 1, 1, 1), ARGB.colorFromFloat(lowAlpha, 1, .4F, .4F),
				ARGB.colorFromFloat(lowAlpha, .4F, 1, .7F) };
		for (int i = 0; i < 3; i++) {
			float now = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
			pose.mulPose(VecHelper.rotateY(now * .1F * speed));
			ring(buffers.getBuffer(layer("botania_garden_rays", ADDITIVE_TEXTURE, SKYBOX)), pose.last().pose(),
					20, fuzz, speed * .4F * now, colors[i]);
			buffers.endBatch();
			if (i == 0) { pose.mulPose(VecHelper.rotateX(20)); fuzz = (float) Math.PI * 14 / 90; speed = .2F; }
			if (i == 1) { pose.mulPose(VecHelper.rotateX(50)); fuzz = (float) Math.PI * 6 / 90; speed = 2; }
		}
		pose.popPose();

		float rainbowAlpha = dayAngle > .25F ? 1F - dayAngle : dayAngle;
		rainbowAlpha = .25F - Math.min(.25F, rainbowAlpha);
		Random random = new Random((int) ((level.getDayTime() + 1000) / 24000L) * 0xFFL);
		pose.pushPose();
		pose.mulPose(VecHelper.rotateY(random.nextFloat() * 360));
		pose.mulPose(VecHelper.rotateZ(random.nextFloat() * 360));
		ring(buffers.getBuffer(layer("botania_garden_rainbow", ALPHA_TEXTURE, RAINBOW)), pose.last().pose(),
				10, 0, 0, ARGB.colorFromFloat(rainbowAlpha * (1 - insideVoid), 1, 1, 1));
		buffers.endBatch();
		pose.popPose();
	}

	public static void renderStars(PoseStack pose, ClientLevel level, float partialTick) {
		float angle = level.getTimeOfDay(partialTick);
		float alpha = (1 - level.getRainLevel(partialTick)) * Math.max(.1F, (angle > .5F ? 1 - angle : angle) * 2);
		float time = (ClientTickHandler.ticksInGame + partialTick + 2000) * .005F;
		float[] speeds = { 3, 1, 2, 3, 1, 2 };
		float[][] rgb = { { 1, 1, 1 }, { .5F, 1, 1 }, { 1, .75F, .75F }, { 1, 1, 1 }, { .5F, 1, 1 }, { 1, .75F, .75F } };
		var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		Random random = new Random(10842L);
		for (int pass = 0; pass < 6; pass++) {
			pose.pushPose();
			pose.mulPose(pass < 3 ? VecHelper.rotateY(time * speeds[pass]) : VecHelper.rotateZ(time * speeds[pass]));
			VertexConsumer out = buffers.getBuffer(layer("botania_garden_stars", ADDITIVE_COLOR, null));
			int color = ARGB.colorFromFloat(alpha * (pass < 3 ? 1 : .25F), rgb[pass][0], rgb[pass][1], rgb[pass][2]);
			stars(out, pose.last().pose(), random, color);
			buffers.endBatch();
			pose.popPose();
		}
	}

	private static void quad(VertexConsumer out, Matrix4f matrix, float scale, int color) {
		out.addVertex(matrix, -scale, 100, -scale).setUv(0, 0).setColor(color);
		out.addVertex(matrix, scale, 100, -scale).setUv(1, 0).setColor(color);
		out.addVertex(matrix, scale, 100, scale).setUv(1, 1).setColor(color);
		out.addVertex(matrix, -scale, 100, scale).setUv(0, 1).setColor(color);
	}

	private static void ring(VertexConsumer out, Matrix4f matrix, float radius, float fuzz, float offset, int color) {
		for (int i = 0; i < 90; i += 2) {
			for (int k = 0; k < 4; k++) {
				int j = i + (k == 0 || k == 3 ? -1 : 1);
				float degrees = j * 4 + offset;
				float x = (float) Math.cos(Math.toRadians(degrees)) * radius;
				float z = (float) Math.sin(Math.toRadians(degrees)) * radius;
				float y = (float) Math.sin(fuzz * j) + ((k == 0 || k == 1) ? 2 : 0);
				out.addVertex(matrix, x, y, z).setUv(degrees / 360F, (k == 0 || k == 1) ? 1 : 0).setColor(color);
			}
		}
	}

	private static void stars(VertexConsumer out, Matrix4f matrix, Random random, int color) {
		for (int i = 0; i < 1500; i++) {
			float x = random.nextFloat() * 2 - 1, y = random.nextFloat() * 2 - 1, z = random.nextFloat() * 2 - 1;
			float length = x * x + y * y + z * z;
			if (length >= 1 || length <= .01F) { continue; }
			float inverse = 100 / (float) Math.sqrt(length); x *= inverse; y *= inverse; z *= inverse;
			float size = .15F + random.nextFloat() * .1F;
			out.addVertex(matrix, x - size, y, z - size).setColor(color);
			out.addVertex(matrix, x + size, y, z - size).setColor(color);
			out.addVertex(matrix, x + size, y, z + size).setColor(color);
			out.addVertex(matrix, x - size, y, z + size).setColor(color);
		}
	}
}
