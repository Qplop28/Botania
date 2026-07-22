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
			String vertexShader, VertexFormat format, VertexFormat.Mode mode) {
		Identifier id = prefix(name);
		return RenderPipelines.register(RenderPipeline.builder(snippet)
				.withLocation(id)
				.withVertexShader(vertexShader)
				.withFragmentShader(id.toString())
				.withVertexFormat(format, mode)
				.build());
	}

	public static final RenderPipeline STARFIELD = register("starfield", RenderPipelines.END_PORTAL_SNIPPET,
			"core/rendertype_end_portal", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS);
	public static final RenderPipeline DOPPLEGANGER = register("doppleganger", RenderPipelines.ENTITY_SNIPPET,
			"botania:doppleganger", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
	public static final RenderPipeline MANA_POOL = register("mana_pool", RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP_SNIPPET,
			"core/position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline TERRA_PLATE_RUNE = register("terra_plate_rune", RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP_SNIPPET,
			"core/position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline ENCHANTER_RUNE = register("enchanter_rune", RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP_SNIPPET,
			"core/position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS);
	public static final RenderPipeline PYLON = register("pylon", RenderPipelines.ENTITY_SNIPPET,
			"core/rendertype_entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
	public static final RenderPipeline HALO = register("halo", RenderPipelines.POSITION_COLOR_TEX_SNIPPET,
			"core/position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS);
	public static final RenderPipeline FILM_GRAIN_PARTICLE = register("film_grain_particle", RenderPipelines.PARTICLE_SNIPPET,
			"core/particle", DefaultVertexFormat.PARTICLE, VertexFormat.Mode.QUADS);
	public static final RenderPipeline DOPPLEGANGER_BAR = register("doppleganger_bar", RenderPipelines.GUI_TEXTURED_SNIPPET,
			"botania:doppleganger_bar", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);

	public static RenderPipeline doppleganger() {
		return BotaniaConfig.client().useShaders() ? DOPPLEGANGER : RenderPipelines.ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline manaPool() {
		return BotaniaConfig.client().useShaders() ? MANA_POOL : RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline terraPlate() {
		return BotaniaConfig.client().useShaders() ? TERRA_PLATE_RUNE : RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline enchanter() {
		return BotaniaConfig.client().useShaders() ? ENCHANTER_RUNE : RenderPipelines.POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline pylon() {
		return BotaniaConfig.client().useShaders() ? PYLON : RenderPipelines.ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline halo() {
		return BotaniaConfig.client().useShaders() ? HALO : RenderPipelines.POSITION_COLOR_TEX;
	}

	public static RenderPipeline filmGrainParticle() {
		return BotaniaConfig.client().useShaders() ? FILM_GRAIN_PARTICLE : RenderPipelines.PARTICLE;
	}

	public static RenderPipeline dopplegangerBar() {
		return BotaniaConfig.client().useShaders() ? DOPPLEGANGER_BAR : RenderPipelines.GUI_TEXTURED;
	}
}
