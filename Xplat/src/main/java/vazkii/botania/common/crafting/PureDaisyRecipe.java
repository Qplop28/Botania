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
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.api.recipe.StateIngredient;

import java.util.Optional;

public class PureDaisyRecipe
		implements vazkii.botania.api.recipe.PureDaisyRecipe {
	public static final int DEFAULT_TIME = 150;

	private static final Codec<BlockState> BLOCK_STATE_CODEC =
			Codec.PASSTHROUGH.comapFlatMap(
					PureDaisyRecipe::decodeBlockState,
					state -> new Dynamic<>(
							JsonOps.INSTANCE,
							StateIngredientHelper
									.serializeBlockState(state)
					)
			);

	private static final MapCodec<PureDaisyRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					StateIngredientHelper.CODEC
							.fieldOf("input")
							.forGetter(recipe -> recipe.input),
					BLOCK_STATE_CODEC
							.fieldOf("output")
							.forGetter(recipe -> recipe.outputState),
					Codec.INT
							.optionalFieldOf(
									"time",
									DEFAULT_TIME
							)
							.forGetter(recipe -> recipe.time),
					Identifier.CODEC
							.optionalFieldOf("success_function")
							.forGetter(recipe ->
									Optional.ofNullable(
											recipe.function
									).map(CacheableFunction::getId)
							)
			).apply(
					instance,
					(input, output, time, functionId) ->
							new PureDaisyRecipe(
									input,
									output,
									time,
									functionId
											.map(CacheableFunction::new)
											.orElse(null)
							)
			));

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			PureDaisyRecipe
	> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<PureDaisyRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	protected final StateIngredient input;
	protected final BlockState outputState;

	private final int time;

	@Nullable
	private final CacheableFunction function;

	/**
	 * Creates a Pure Daisy recipe.
	 *
	 * @param input    The block or block state input.
	 * @param state    The block state placed when conversion completes.
	 * @param time     The number of Pure Daisy processing ticks.
	 * @param function An optional function run at the converted block.
	 */
	public PureDaisyRecipe(
			StateIngredient input,
			BlockState state,
			int time,
			@Nullable CacheableFunction function
	) {
		Preconditions.checkArgument(
				time >= 0,
				"Time must be nonnegative"
		);

		this.input = input;
		this.outputState = state;
		this.time = time;
		this.function = function;
	}

	/**
	 * Transitional constructor for existing data generators and addons.
	 *
	 * <p>Recipe identifiers are now stored by the recipe holder, so the
	 * supplied identifier is intentionally not retained.</p>
	 */
	@Deprecated
	public PureDaisyRecipe(
			Identifier ignoredId,
			StateIngredient input,
			BlockState state,
			int time,
			@Nullable CacheableFunction function
	) {
		this(input, state, time, function);
	}

	@Override
	public boolean matches(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy,
			BlockState state
	) {
		return this.input.test(state)
				&& !this.outputState.equals(state);
	}

	@Override
	public boolean set(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy
	) {
		if (level.isClientSide()) {
			return true;
		}

		boolean success =
				level.setBlockAndUpdate(pos, this.outputState);

		if (!success || this.function == null) {
			return success;
		}

		ServerLevel serverLevel = (ServerLevel) level;
		var server = serverLevel.getServer();

		this.function.get(server.getFunctions())
				.ifPresent(command -> {
					var context = server.getFunctions()
							.getGameLoopSender()
							.withLevel(serverLevel)
							.withPosition(
									Vec3.atBottomCenterOf(pos)
							);

					server.getFunctions()
							.execute(command, context);
				});

		return true;
	}

	@Override
	public StateIngredient getInput() {
		return this.input;
	}

	@Override
	public BlockState getOutputState() {
		return this.outputState;
	}

	@Nullable
	@Override
	public CacheableFunction getSuccessFunction() {
		return this.function;
	}

	@Override
	public int getTime() {
		return this.time;
	}

	@Override
	public RecipeSerializer<? extends PureDaisyRecipe> getSerializer() {
		return SERIALIZER;
	}

	private static DataResult<BlockState> decodeBlockState(
			Dynamic<?> dynamic
	) {
		try {
			JsonElement element =
					dynamic.convert(JsonOps.INSTANCE).getValue();

			if (!element.isJsonObject()) {
				return DataResult.error(
						() -> "Pure Daisy output must be "
								+ "a block-state object"
				);
			}

			return DataResult.success(
					StateIngredientHelper.readBlockState(
							element.getAsJsonObject()
					)
			);
		} catch (RuntimeException exception) {
			return DataResult.error(
					() -> "Invalid Pure Daisy output state: "
							+ exception.getMessage()
			);
		}
	}
}