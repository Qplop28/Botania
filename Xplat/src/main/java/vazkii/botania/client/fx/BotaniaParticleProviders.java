/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;

import java.util.function.Function;

/** Client-only registration for Botania's sprite-backed particle providers. */
public final class BotaniaParticleProviders {
	private BotaniaParticleProviders() {}

	public interface Consumer {
		<T extends ParticleOptions> void register(ParticleType<T> type,
				Function<SpriteSet, ParticleProvider<T>> constructor);
	}

	public static void registerFactories(Consumer consumer) {
		BotaniaParticleRenderTypes.init();
		consumer.register(BotaniaParticles.WISP, WispParticleType.Factory::new);
		consumer.register(BotaniaParticles.SPARKLE, SparkleParticleType.Factory::new);
	}

	public static class SparkleFactory implements ParticleProvider<SparkleParticleData> {
		private final SpriteSet sprites;

		public SparkleFactory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SparkleParticleData data, ClientLevel world, double x, double y, double z,
				double mx, double my, double mz, RandomSource random) {
			return new FXSparkle(world, x, y, z, data.size, data.r, data.g, data.b, data.m,
					data.fake, data.noClip, data.corrupt, sprites);
		}
	}

	public static class WispFactory implements ParticleProvider<WispParticleData> {
		private final SpriteSet sprites;

		public WispFactory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(WispParticleData data, ClientLevel world, double x, double y, double z,
				double mx, double my, double mz, RandomSource random) {
			return new FXWisp(world, x, y, z, mx, my, mz, data.size, data.r, data.g, data.b,
					data.depthTest, data.maxAgeMul, data.noClip, data.gravity, sprites.get(random));
		}
	}
}
