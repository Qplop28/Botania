/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import vazkii.botania.xplat.BotaniaConfig;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/** Immutable render state for Botania's additive particle layers. */
public final class BotaniaParticleRenderTypes {
	private BotaniaParticleRenderTypes() {}

	private static final ColorTargetState ADDITIVE_COLOR = new ColorTargetState(
			new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE));
	private static final Optional<DepthStencilState> DEPTH_TEST_NO_WRITE =
			Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false));
	private static final Identifier LINEAR_PARTICLE_ATLAS = prefix("textures/atlas/particles_linear.png");
	private static LinearParticleAtlasTexture linearParticleAtlasTexture;

	private static final RenderPipeline ADDITIVE_DEPTH =
			register("pipeline/particle/additive_depth_tested", DEPTH_TEST_NO_WRITE, null);
	private static final RenderPipeline ADDITIVE_NO_DEPTH =
			register("pipeline/particle/additive_no_depth_test", Optional.empty(), null);
	private static final RenderPipeline CORRUPT_SPARKLE = register(
			"pipeline/particle/corrupt_additive_depth_tested", DEPTH_TEST_NO_WRITE,
			prefix("core/film_grain_particle"));

	private static final SingleQuadParticle.Layer ADDITIVE_DEPTH_LAYER = layer(ADDITIVE_DEPTH);
	private static final SingleQuadParticle.Layer ADDITIVE_NO_DEPTH_LAYER = layer(ADDITIVE_NO_DEPTH);
	private static final SingleQuadParticle.Layer CORRUPT_SPARKLE_LAYER = layer(CORRUPT_SPARKLE);

	private static RenderPipeline register(String name, Optional<DepthStencilState> depthStencilState,
			Identifier fragmentShader) {
		var builder = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
				.withLocation(prefix(name))
				.withColorTargetState(ADDITIVE_COLOR)
				.withDepthStencilState(depthStencilState);
		if (fragmentShader != null) {
			builder = builder.withFragmentShader(fragmentShader);
		}
		return RenderPipelines.register(builder.build());
	}

	private static SingleQuadParticle.Layer layer(RenderPipeline pipeline) {
		return new SingleQuadParticle.Layer(true, LINEAR_PARTICLE_ATLAS, pipeline);
	}

	/** Forces pipeline registration during client initialization. */
	public static void init() {}

	/** Registers a local linear-sampler view of the live particle atlas. */
	public static void init(TextureManager textureManager) {
		if (linearParticleAtlasTexture == null || linearParticleAtlasTexture.textureManager != textureManager) {
			linearParticleAtlasTexture = new LinearParticleAtlasTexture(textureManager);
		}
		textureManager.register(LINEAR_PARTICLE_ATLAS, linearParticleAtlasTexture);
	}

	public static SingleQuadParticle.Layer sparkle(boolean corrupt) {
		return corrupt && BotaniaConfig.client().useShaders() ? CORRUPT_SPARKLE_LAYER : ADDITIVE_DEPTH_LAYER;
	}

	public static SingleQuadParticle.Layer wisp(boolean depthTest) {
		return depthTest ? ADDITIVE_DEPTH_LAYER : ADDITIVE_NO_DEPTH_LAYER;
	}

	private static final class LinearParticleAtlasTexture extends AbstractTexture {
		private final TextureManager textureManager;

		private LinearParticleAtlasTexture(TextureManager textureManager) {
			this.textureManager = textureManager;
		}

		private AbstractTexture particleAtlas() {
			return textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES);
		}

		@Override
		public GpuTexture getTexture() {
			return particleAtlas().getTexture();
		}

		@Override
		public GpuTextureView getTextureView() {
			return particleAtlas().getTextureView();
		}

		@Override
		public GpuSampler getSampler() {
			return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		}

		@Override
		public void close() {}
	}
}
