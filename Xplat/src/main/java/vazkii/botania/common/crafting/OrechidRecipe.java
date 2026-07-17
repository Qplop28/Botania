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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;

import java.util.Optional;

public class OrechidRecipe implements vazkii.botania.api.recipe.OrechidRecipe {
	protected static final MapCodec<OrechidRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					StateIngredientHelper.CODEC.fieldOf("input")
							.forGetter(recipe -> recipe.input),
					StateIngredientHelper.CODEC.fieldOf("output")
							.forGetter(recipe -> recipe.output),
					Codec.INT.fieldOf("weight")
							.forGetter(recipe -> recipe.weight),
					Identifier.CODEC.optionalFieldOf("success_function")
							.forGetter(recipe -> Optional.ofNullable(recipe.successFunction)
									.map(CacheableFunction::getId))
			).apply(instance, (input, output, weight, function) ->
					new OrechidRecipe(input, output, weight,
							function.map(CacheableFunction::new).orElse(null))));

	private static final StreamCodec<RegistryFriendlyByteBuf, OrechidRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<OrechidRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	protected final StateIngredient input;
	protected final StateIngredient output;
	private final int weight;
	@Nullable
	private final CacheableFunction successFunction;

	public OrechidRecipe(StateIngredient input, StateIngredient output, int weight,
			@Nullable CacheableFunction successFunction) {
		this.input = input;
		this.output = output;
		this.weight = weight;
		this.successFunction = successFunction;
	}

	@Deprecated
	public OrechidRecipe(Identifier ignoredId, StateIngredient input, StateIngredient output, int weight,
			@Nullable CacheableFunction successFunction) {
		this(input, output, weight, successFunction);
	}

	@Override
	public StateIngredient getInput() {
		return this.input;
	}

	@Override
	public StateIngredient getOutput() {
		return this.output;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Nullable
	@Override
	public CacheableFunction getSuccessFunction() {
		return this.successFunction;
	}

	@Override
	public RecipeType<? extends OrechidRecipe> getType() {
		return BotaniaRecipeTypes.ORECHID_TYPE;
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return SERIALIZER;
	}
}
