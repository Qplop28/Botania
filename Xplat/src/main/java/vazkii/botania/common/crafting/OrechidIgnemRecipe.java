/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;

import java.util.Optional;

public class OrechidIgnemRecipe extends OrechidRecipe {
	private static final MapCodec<OrechidIgnemRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					StateIngredientHelper.CODEC.fieldOf("input")
							.forGetter(OrechidIgnemRecipe::getInput),
					StateIngredientHelper.CODEC.fieldOf("output")
							.forGetter(OrechidIgnemRecipe::getOutput),
					Codec.INT.fieldOf("weight")
							.forGetter(OrechidIgnemRecipe::getWeight),
					Identifier.CODEC.optionalFieldOf("success_function")
							.forGetter(recipe -> Optional.ofNullable(recipe.getSuccessFunction())
									.map(CacheableFunction::getId))
			).apply(instance, (input, output, weight, function) ->
					new OrechidIgnemRecipe(input, output, weight,
							function.map(CacheableFunction::new).orElse(null))));

	private static final StreamCodec<RegistryFriendlyByteBuf, OrechidIgnemRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<OrechidIgnemRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public OrechidIgnemRecipe(
			StateIngredient input,
			StateIngredient output,
			int weight,
			@Nullable CacheableFunction successFunction
	) {
		super(input, output, weight, successFunction);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public OrechidIgnemRecipe(
			Identifier ignoredId,
			StateIngredient input,
			StateIngredient output,
			int weight,
			@Nullable CacheableFunction successFunction
	) {
		this(input, output, weight, successFunction);
	}

	@Override
	public RecipeType<? extends OrechidIgnemRecipe> getType() {
		return BotaniaRecipeTypes.ORECHID_IGNEM_TYPE;
	}

	@Override
	public RecipeSerializer<OrechidIgnemRecipe> getSerializer() {
		return SERIALIZER;
	}
}
