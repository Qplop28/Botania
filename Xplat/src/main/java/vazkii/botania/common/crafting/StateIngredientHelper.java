/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class StateIngredientHelper {
	public static final Codec<StateIngredient> CODEC =
			Codec.PASSTHROUGH.comapFlatMap(
					StateIngredientHelper::decode,
					ingredient -> new Dynamic<>(
							JsonOps.INSTANCE,
							ingredient.serialize()
					)
			);

	public static final StreamCodec<
			RegistryFriendlyByteBuf,
			StateIngredient
	> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	private StateIngredientHelper() {}

	public static StateIngredient of(Block block) {
		return new BlockStateIngredient(block);
	}

	public static StateIngredient of(BlockState state) {
		return new BlockStateStateIngredient(state);
	}

	public static StateIngredient of(TagKey<Block> tag) {
		return of(tag.location());
	}

	public static StateIngredient of(Identifier id) {
		return new TagStateIngredient(id);
	}

	public static StateIngredient of(Collection<Block> blocks) {
		return new BlocksStateIngredient(blocks);
	}

	// Cannot be named "of" because of type erasure.
	public static StateIngredient compound(
			Collection<StateIngredient> ingredients
	) {
		return new CompoundStateIngredient(ingredients);
	}

	public static StateIngredient combine(
			StateIngredient firstIngredient,
			StateIngredient secondIngredient
	) {
		List<StateIngredient> ingredients = new ArrayList<>();

		if (firstIngredient
				instanceof CompoundStateIngredient compound) {
			ingredients.addAll(compound.getIngredients());
		} else {
			ingredients.add(firstIngredient);
		}

		if (secondIngredient
				instanceof CompoundStateIngredient compound) {
			ingredients.addAll(compound.getIngredients());
		} else {
			ingredients.add(secondIngredient);
		}

		return new CompoundStateIngredient(ingredients);
	}

	public static StateIngredient tagExcluding(
			TagKey<Block> tag,
			StateIngredient... excluded
	) {
		return new TagExcludingStateIngredient(
				tag.location(),
				List.of(excluded)
		);
	}

	public static StateIngredient deserialize(JsonObject object) {
		String type = GsonHelper.getAsString(object, "type");

		switch (type) {
			case "tag":
				return new TagStateIngredient(
						new Identifier(
								GsonHelper.getAsString(object, "tag")
						)
				);

			case "block":
				return new BlockStateIngredient(
						getBlock(
								new Identifier(
										GsonHelper.getAsString(
												object,
												"block"
										)
								)
						)
				);

			case "state":
				return new BlockStateStateIngredient(
						readBlockState(object)
				);

			case "blocks":
				List<Block> blocks = new ArrayList<>();

				for (JsonElement element :
						GsonHelper.getAsJsonArray(
								object,
								"blocks"
						)) {
					blocks.add(
							getBlock(
									new Identifier(
											element.getAsString()
									)
							)
					);
				}

				return new BlocksStateIngredient(blocks);

			case "tag_excluding":
				Identifier tag = new Identifier(
						GsonHelper.getAsString(object, "tag")
				);

				List<StateIngredient> excluded =
						new ArrayList<>();

				for (JsonElement element :
						GsonHelper.getAsJsonArray(
								object,
								"exclude"
						)) {
					excluded.add(
							deserialize(
									GsonHelper.convertToJsonObject(
											element,
											"exclude entry"
									)
							)
					);
				}

				return new TagExcludingStateIngredient(
						tag,
						excluded
				);

			case "compound":
				List<StateIngredient> ingredients =
						new ArrayList<>();

				for (JsonElement element :
						GsonHelper.getAsJsonArray(
								object,
								"ingredients"
						)) {
					if (!element.isJsonObject()) {
						throw new JsonParseException(
								"Unknown ingredient in compound "
										+ "state ingredient: "
										+ element
						);
					}

					ingredients.add(
							deserialize(element.getAsJsonObject())
					);
				}

				return new CompoundStateIngredient(ingredients);

			default:
				throw new JsonParseException(
						"Unknown state ingredient type: " + type
				);
		}
	}

	/**
	 * Deserializes a state ingredient, removes air from its data, and returns
	 * {@code null} if the ingredient matched only air.
	 */
	@Nullable
	public static StateIngredient tryDeserialize(JsonObject object) {
		return clearTheAir(deserialize(object));
	}

	@Nullable
	public static StateIngredient clearTheAir(
			@Nullable StateIngredient ingredient
	) {
		if (ingredient == null) {
			return null;
		}

		if (ingredient instanceof BlockStateIngredient
				|| ingredient
						instanceof BlockStateStateIngredient) {
			if (ingredient.test(Blocks.AIR.defaultBlockState())) {
				return null;
			}
		} else if (ingredient
				instanceof BlocksStateIngredient blocksIngredient) {
			List<Block> blocks =
					new ArrayList<>(blocksIngredient.blocks);

			if (blocks.removeIf(block -> block == Blocks.AIR)) {
				if (blocks.isEmpty()) {
					return null;
				}

				return of(blocks);
			}
		} else if (ingredient
				instanceof CompoundStateIngredient compound) {
			List<StateIngredient> ingredients =
					compound.getIngredients()
							.stream()
							.map(StateIngredientHelper::clearTheAir)
							.filter(Objects::nonNull)
							.toList();

			if (ingredients.isEmpty()) {
				return null;
			}

			return compound(ingredients);
		}

		return ingredient;
	}

	/**
	 * Transitional reader for callers using the legacy custom packet format.
	 */
	public static StateIngredient read(FriendlyByteBuf buffer) {
		return switch (buffer.readVarInt()) {
			case 0 -> {
				int count = buffer.readVarInt();
				Set<Block> blocks = new HashSet<>();

				for (int index = 0; index < count; index++) {
					blocks.add(
							BuiltInRegistries.BLOCK.byId(
									buffer.readVarInt()
							)
					);
				}

				yield new BlocksStateIngredient(blocks);
			}

			case 1 -> new BlockStateIngredient(
					BuiltInRegistries.BLOCK.byId(
							buffer.readVarInt()
					)
			);

			case 2 -> new BlockStateStateIngredient(
					Block.stateById(buffer.readVarInt())
			);

			case 3 -> {
				int ingredientCount = buffer.readVarInt();
				Set<StateIngredient> ingredients =
						new HashSet<>();

				for (int index = 0;
						index < ingredientCount;
						index++) {
					ingredients.add(read(buffer));
				}

				yield new CompoundStateIngredient(ingredients);
			}

			default -> throw new IllegalArgumentException(
					"Unknown state ingredient discriminator"
			);
		};
	}

	/**
	 * Writes a block state using Botania's existing lowercase JSON field
	 * names.
	 */
	public static JsonObject serializeBlockState(
			BlockState state
	) {
		JsonElement encoded = BlockState.CODEC
				.encodeStart(JsonOps.INSTANCE, state)
				.getOrThrow(JsonParseException::new);

		JsonObject object = GsonHelper.convertToJsonObject(
				encoded,
				"block state"
		);

		renameJsonKey(object, "Name", "name");
		renameJsonKey(object, "Properties", "properties");

		return object;
	}

	/**
	 * Reads Botania's lowercase block-state JSON through the vanilla
	 * block-state codec.
	 */
	public static BlockState readBlockState(JsonObject object) {
		JsonObject normalized = object.deepCopy();

		renameJsonKey(normalized, "name", "Name");
		renameJsonKey(
				normalized,
				"properties",
				"Properties"
		);

		return BlockState.CODEC
				.parse(JsonOps.INSTANCE, normalized)
				.getOrThrow(JsonParseException::new);
	}

	private static DataResult<StateIngredient> decode(
			Dynamic<?> dynamic
	) {
		try {
			JsonElement element =
					dynamic.convert(JsonOps.INSTANCE).getValue();

			if (!element.isJsonObject()) {
				return DataResult.error(
						() -> "State ingredient must be an object"
				);
			}

			return DataResult.success(
					deserialize(element.getAsJsonObject())
			);
		} catch (RuntimeException exception) {
			return DataResult.error(
					() -> "Invalid state ingredient: "
							+ exception.getMessage()
			);
		}
	}

	private static Block getBlock(Identifier id) {
		Block block = BuiltInRegistries.BLOCK.getValue(id);

		if (block == null) {
			throw new JsonParseException(
					"Unknown block id: " + id
			);
		}

		return block;
	}

	private static void renameJsonKey(
			JsonObject object,
			String oldName,
			String newName
	) {
		JsonElement value = object.remove(oldName);

		if (value != null) {
			object.add(newName, value);
		}
	}
}