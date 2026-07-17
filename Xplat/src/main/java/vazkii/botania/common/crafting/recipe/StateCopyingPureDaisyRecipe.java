/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.PureDaisyRecipe;
import vazkii.botania.common.crafting.StateIngredientHelper;

/**
 * Pure Daisy recipe that copies compatible input block-state properties to
 * the converted block.
 */
public class StateCopyingPureDaisyRecipe
		extends PureDaisyRecipe {
	private static final Codec<Block> BLOCK_CODEC =
			Identifier.CODEC.comapFlatMap(
					StateCopyingPureDaisyRecipe::decodeBlock,
					BuiltInRegistries.BLOCK::getKey
			);

	private static final MapCodec<
			StateCopyingPureDaisyRecipe
	> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					StateIngredientHelper.CODEC
							.fieldOf("input")
							.forGetter(recipe ->
									recipe.getInput()
							),
					BLOCK_CODEC
							.fieldOf("output")
							.forGetter(recipe ->
									recipe.getOutputState()
											.getBlock()
							),
					Codec.INT
							.optionalFieldOf(
									"time",
									DEFAULT_TIME
							)
							.forGetter(recipe ->
									recipe.getTime()
							)
			).apply(
					instance,
					StateCopyingPureDaisyRecipe::new
			));

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			StateCopyingPureDaisyRecipe
	> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<
			StateCopyingPureDaisyRecipe
	> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public StateCopyingPureDaisyRecipe(
			StateIngredient input,
			Block block,
			int time
	) {
		super(
				input,
				block.defaultBlockState(),
				time,
				null
		);
	}

	/**
	 * Transitional constructor for existing data generators and addons.
	 *
	 * <p>Recipe identifiers are now stored by the recipe holder, so the
	 * supplied identifier is intentionally not retained.</p>
	 */
	@Deprecated
	public StateCopyingPureDaisyRecipe(
			Identifier ignoredId,
			StateIngredient input,
			Block block,
			int time
	) {
		super(
				ignoredId,
				input,
				block.defaultBlockState(),
				time,
				null
		);
	}

	@Override
	public boolean matches(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy,
			BlockState state
	) {
		BlockState output =
				this.outputState.getBlock()
						.withPropertiesOf(state);

		return this.input.test(state)
				&& !output.equals(state);
	}

	@Override
	public boolean set(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy
	) {
		if (!level.isClientSide) {
			Block outputBlock =
					getOutputState().getBlock();

			level.setBlockAndUpdate(
					pos,
					outputBlock.withPropertiesOf(
							level.getBlockState(pos)
					)
			);
		}

		return true;
	}

	@Override
	public RecipeSerializer<
			StateCopyingPureDaisyRecipe
	> getSerializer() {
		return SERIALIZER;
	}

	private static DataResult<Block> decodeBlock(
			Identifier id
	) {
		Block block = BuiltInRegistries.BLOCK.getValue(id);

		if (block == null) {
			return DataResult.error(
					() -> "Unknown block id: " + id
			);
		}

		return DataResult.success(block);
	}
}