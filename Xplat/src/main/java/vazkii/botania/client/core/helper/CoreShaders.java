/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import vazkii.botania.xplat.BotaniaConfig;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/** Botania shader programs expressed as immutable render pipelines. */
public final class CoreShaders {
	private CoreShaders() {}

	private static RenderPipeline register(String name, RenderPipeline.Snippet snippet,
			String vertexShader, String fragmentShader, VertexFormat format, VertexFormat.Mode mode) {
		Identifier id = prefix(name);
		var builder = snippet == null ? RenderPipeline.builder() : RenderPipeline.builder(snippet);
		return RenderPipelines.register(builder
				.withLocation(id)
				.withVertexShader(vertexShader)
				.withFragmentShader(fragmentShader)
				.withVertexFormat(format, mode)
				.build());
	}

	private static RenderPipeline register(String name, RenderPipeline.Snippet snippet,
			String vertexShader, VertexFormat format, VertexFormat.Mode mode) {
		return register(name, snippet, vertexShader, prefix(name).toString(), format, mode);
	}

	private static final VertexFormat POSITION_TEX_COLOR_LIGHTMAP = DefaultVertexFormat.POSITION_TEX_COLOR_LIGHTMAP;
	private static final RenderPipeline POSITION_TEX_COLOR = register("position_tex_color_fallback", null,
			"core/position_tex_color", "core/position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);
	private static final RenderPipeline POSITION_TEX_COLOR_LIGHTMAP_PIPELINE = register("position_tex_color_lightmap_fallback", null,
			"core/position_tex_color_lightmap", "core/position_tex_color_lightmap", POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS);
	private static final RenderPipeline PARTICLE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
					.withLocation(prefix("particle_fallback"))
					.build());

	public static final RenderPipeline STARFIELD = register("starfield", null,
			"core/rendertype_end_portal", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS);
	public static final RenderPipeline DOPPLEGANGER = register("doppleganger", null,
			"botania:doppleganger", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
	public static final RenderPipeline MANA_POOL = register("mana_pool", null,
			"core/position_tex_color_lightmap", POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline TERRA_PLATE_RUNE = register("terra_plate_rune", null,
			"core/position_tex_color_lightmap", POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline ENCHANTER_RUNE = register("enchanter_rune", null,
			"core/position_tex_color_lightmap", POSITION_TEX_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline PYLON = register("pylon", null,
			"core/rendertype_entity_translucent", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
	public static final RenderPipeline HALO = register("halo", null,
			"core/position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);
	public static final RenderPipeline FILM_GRAIN_PARTICLE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
					.withLocation(prefix("film_grain_particle"))
					.withFragmentShader(prefix("film_grain_particle"))
					.build());
	public static final RenderPipeline DOPPLEGANGER_BAR = register("doppleganger_bar", null,
			"botania:doppleganger_bar", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);

	public static RenderPipeline doppleganger() {
		return BotaniaConfig.client().useShaders() ? DOPPLEGANGER : RenderPipelines.ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline manaPool() {
		return BotaniaConfig.client().useShaders() ? MANA_POOL : POSITION_TEX_COLOR_LIGHTMAP_PIPELINE;
	}

	public static RenderPipeline terraPlate() {
		return BotaniaConfig.client().useShaders() ? TERRA_PLATE_RUNE : POSITION_TEX_COLOR_LIGHTMAP_PIPELINE;
	}

	public static RenderPipeline enchanter() {
		return BotaniaConfig.client().useShaders() ? ENCHANTER_RUNE : POSITION_TEX_COLOR_LIGHTMAP_PIPELINE;
	}

	public static RenderPipeline pylon() {
		return BotaniaConfig.client().useShaders() ? PYLON : RenderPipelines.ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline halo() {
		return BotaniaConfig.client().useShaders() ? HALO : POSITION_TEX_COLOR;
	}

	public static RenderPipeline filmGrainParticle() {
		return BotaniaConfig.client().useShaders() ? FILM_GRAIN_PARTICLE : PARTICLE;
	}

	public static RenderPipeline dopplegangerBar() {
		return BotaniaConfig.client().useShaders() ? DOPPLEGANGER_BAR : RenderPipelines.GUI_TEXTURED;
	}
}
