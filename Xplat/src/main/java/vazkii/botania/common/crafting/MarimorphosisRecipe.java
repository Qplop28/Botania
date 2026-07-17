/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;

import java.util.Optional;

public class MarimorphosisRecipe extends OrechidRecipe {
	private static final Codec<Integer> WEIGHT_CODEC = Codec.INT.validate(weight -> weight > 0
			? DataResult.success(weight)
			: DataResult.error(() -> "Weight must be positive"));
	private static final MapCodec<MarimorphosisRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					StateIngredientHelper.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
					StateIngredientHelper.CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
					WEIGHT_CODEC.fieldOf("weight").forGetter(recipe -> recipe.weight),
					Identifier.CODEC.optionalFieldOf("success_function").forGetter(recipe ->
							Optional.ofNullable(recipe.successFunction).map(CacheableFunction::getId)),
					Codec.INT.optionalFieldOf("biome_bonus", 0).forGetter(recipe -> recipe.weightBonus),
					TagKey.codec(Registries.BIOME).fieldOf("biome_bonus_tag").forGetter(recipe -> recipe.biomes)
			).apply(instance, (input, output, weight, function, bonus, biomes) ->
					new MarimorphosisRecipe(input, output, weight,
							function.map(CacheableFunction::new).orElse(null), bonus, biomes)));
	private static final StreamCodec<RegistryFriendlyByteBuf, MarimorphosisRecipe> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
	public static final RecipeSerializer<MarimorphosisRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final int weightBonus;
	private final TagKey<Biome> biomes;

	public MarimorphosisRecipe(StateIngredient input, StateIngredient output, int weight,
			@Nullable CacheableFunction successFunction, int weightBonus, TagKey<Biome> biomes) {
		super(input, output, weight, successFunction);
		Preconditions.checkArgument(weight + weightBonus > 0,
				"Weight combined with bonus must be positive");
		this.weightBonus = weightBonus;
		this.biomes = biomes;
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public MarimorphosisRecipe(Identifier ignoredId, StateIngredient input, StateIngredient output, int weight,
			@Nullable CacheableFunction successFunction, int weightBonus, TagKey<Biome> biomes) {
		this(input, output, weight, successFunction, weightBonus, biomes);
	}

	@Override
	public int getWeight(@NotNull Level level, @NotNull BlockPos pos) {
		return level.getBiome(pos).is(this.biomes) ? getWeight() + weightBonus : getWeight();
	}

	@Override public RecipeType<MarimorphosisRecipe> getType() { return BotaniaRecipeTypes.MARIMORPHOSIS_TYPE; }
	@Override public RecipeSerializer<MarimorphosisRecipe> getSerializer() { return SERIALIZER; }
	public int getWeightBonus() { return weightBonus; }
	public TagKey<Biome> getBiomes() { return biomes; }
}
