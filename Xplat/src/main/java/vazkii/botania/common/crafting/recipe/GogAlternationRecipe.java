/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.xplat.XplatAbstractions;

public final class GogAlternationRecipe {
	private static final MapCodec<Recipe<?>> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Recipe.CODEC.fieldOf("gog")
							.forGetter((Recipe<?> recipe) -> recipe),
					Recipe.CODEC.fieldOf("base")
							.forGetter((Recipe<?> recipe) -> recipe)
			).apply(instance, GogAlternationRecipe::selectRecipe));

	private static final StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC =
			new StreamCodec<>() {
				@Override
				public Recipe<?> decode(RegistryFriendlyByteBuf buffer) {
					throw new IllegalStateException(
							"GogAlternationRecipe should not be sent over network"
					);
				}

				@Override
				public void encode(
						RegistryFriendlyByteBuf buffer,
						Recipe<?> recipe
				) {
					throw new IllegalStateException(
							"GogAlternationRecipe should not be sent over network"
					);
				}
			};

	public static final RecipeSerializer<Recipe<?>> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private GogAlternationRecipe() {}

	private static Recipe<?> selectRecipe(
			Recipe<?> gog,
			Recipe<?> base
	) {
		if (gog.getType() != base.getType()) {
			throw new IllegalArgumentException(
					"Subrecipes must have matching types"
			);
		}

		return XplatAbstractions.INSTANCE.gogLoaded()
				? gog
				: base;
	}
}