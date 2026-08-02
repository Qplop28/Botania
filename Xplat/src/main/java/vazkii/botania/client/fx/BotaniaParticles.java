/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BotaniaParticles {
	public static final ParticleType<WispParticleData> WISP = new WispParticleType();
	public static final ParticleType<SparkleParticleData> SPARKLE = new SparkleParticleType();

	public static void registerParticles(BiConsumer<ParticleType<?>, Identifier> r) {
		r.accept(WISP, prefix("wisp"));
		r.accept(SPARKLE, prefix("sparkle"));
	}

	/** @deprecated Client registrations belong in {@link BotaniaParticleProviders}. */
	@Deprecated(forRemoval = false)
	public static class FactoryHandler {
		public interface Consumer extends BotaniaParticleProviders.Consumer {
		}

		public static void registerFactories(Consumer consumer) {
			BotaniaParticleProviders.registerFactories(consumer);
		}
	}
}
