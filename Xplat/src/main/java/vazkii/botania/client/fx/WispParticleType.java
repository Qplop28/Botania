/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import org.jetbrains.annotations.NotNull;

public class WispParticleType extends ParticleType<WispParticleData> {
	public WispParticleType() {
		super(false);
	}

	@NotNull
	@Override
	public MapCodec<WispParticleData> codec() {
		return WispParticleData.CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, WispParticleData> streamCodec() {
		return WispParticleData.STREAM_CODEC;
	}

	/** @deprecated Use {@link BotaniaParticleProviders} for client registration. */
	@Deprecated(forRemoval = false)
	public static class Factory extends BotaniaParticleProviders.WispFactory {
		public Factory(SpriteSet sprites) {
			super(sprites);
		}
	}
}
