/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

public final class NbtOutputRecipe {
	private static final String WRAPPER_TYPE = "botania:nbt_output_wrapper";
	private static final String SHAPELESS_TYPE = "minecraft:crafting_shapeless";
	private static final String PETAL_APOTHECARY_TYPE = "botania:petal_apothecary";

	private static final MapCodec<Recipe<?>> CODEC = new MapCodec<>() {
		@Override
		public <T> DataResult<Recipe<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
			T nestedRecipe = input.get("recipe");
			if (nestedRecipe == null) {
				return DataResult.error(() -> "NBT output wrapper is missing its nested recipe");
			}

			T legacyNbt = input.get("nbt");
			DataResult<Optional<CompoundTag>> outerMetadata = legacyNbt == null
					? DataResult.success(Optional.empty())
					: CustomData.COMPOUND_TAG_CODEC.parse(ops, legacyNbt)
							.map(Optional::of)
							.mapError(message -> "Invalid NBT output metadata: " + message);

			return outerMetadata.flatMap(metadata -> decodeNestedRecipe(
					ops, nestedRecipe, metadata
			));
		}

		@Override
		public <T> RecordBuilder<T> encode(
				Recipe<?> recipe, DynamicOps<T> ops, RecordBuilder<T> prefix
		) {
			return prefix.add("recipe", Recipe.CODEC.encodeStart(ops, recipe));
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("recipe"), ops.createString("nbt"));
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC =
			Recipe.STREAM_CODEC;

	public static final RecipeSerializer<Recipe<?>> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private NbtOutputRecipe() {}

	private static <T> DataResult<Recipe<?>> decodeNestedRecipe(
			DynamicOps<T> ops,
			T nestedRecipe,
			Optional<CompoundTag> outerMetadata
	) {
		Dynamic<T> recipe = new Dynamic<>(ops, nestedRecipe);
		return recipe.get("type").asString()
				.mapError(message -> "NBT output wrapper has no valid nested recipe type: " + message)
				.flatMap(type -> normalizeRecipe(ops, recipe, type, outerMetadata))
				.flatMap(normalized -> Recipe.CODEC.parse(ops, normalized))
				.mapError(message -> "Invalid nested NBT output recipe: " + message);
	}

	private static <T> DataResult<T> normalizeRecipe(
			DynamicOps<T> ops,
			Dynamic<T> recipe,
			String type,
			Optional<CompoundTag> outerMetadata
	) {
		if (WRAPPER_TYPE.equals(type)) {
			return DataResult.error(() -> "Nested NBT output wrappers are not supported");
		}

		String outputField;
		if (SHAPELESS_TYPE.equals(type)) {
			outputField = "result";
		} else if (PETAL_APOTHECARY_TYPE.equals(type)) {
			outputField = "output";
		} else if (outerMetadata.isPresent()) {
			return DataResult.error(() -> "NBT output metadata is not supported for nested recipe type " + type);
		} else {
			return DataResult.success(recipe.getValue());
		}

		DataResult<T> normalizedIngredients = normalizeIngredientField(
				ops, recipe.getValue(), "ingredients"
		);
		if (PETAL_APOTHECARY_TYPE.equals(type)) {
			normalizedIngredients = normalizedIngredients.flatMap(value ->
					normalizeSingleIngredientField(ops, value, "reagent")
			);
		}

		return normalizedIngredients.flatMap(value -> normalizeOutput(
				ops, new Dynamic<>(ops, value), type, outputField, outerMetadata
		));
	}

	private static <T> DataResult<T> normalizeOutput(
			DynamicOps<T> ops,
			Dynamic<T> recipe,
			String type,
			String outputField,
			Optional<CompoundTag> outerMetadata
	) {
		return recipe.getElement(outputField)
				.mapError(message -> "Nested " + type + " recipe has no valid " + outputField + ": " + message)
				.flatMap(output -> readLegacyOutputMetadata(ops, output)
						.flatMap(innerMetadata -> selectMetadata(type, outerMetadata, innerMetadata))
						.flatMap(metadata -> normalizeItemStackTemplate(ops, output, metadata))
						.map(normalizedOutput -> recipe.set(
								outputField, new Dynamic<>(ops, normalizedOutput)
						).getValue()));
	}

	private static <T> DataResult<Optional<CompoundTag>> readLegacyOutputMetadata(
			DynamicOps<T> ops, T output
	) {
		Optional<MapLike<T>> outputMap = ops.getMap(output).result();
		if (outputMap.isEmpty()) {
			return DataResult.success(Optional.empty());
		}

		MapLike<T> map = outputMap.get();
		T legacyNbt = map.get("nbt");
		if (legacyNbt == null) {
			return DataResult.success(Optional.empty());
		}
		return CustomData.COMPOUND_TAG_CODEC.parse(ops, legacyNbt)
				.map(Optional::of)
				.mapError(message -> "Invalid legacy output nbt: " + message);
	}

	private static DataResult<OutputMetadata> selectMetadata(
			String type,
			Optional<CompoundTag> outerMetadata,
			Optional<CompoundTag> innerMetadata
	) {
		if (outerMetadata.isPresent() && innerMetadata.isPresent()
				&& !outerMetadata.get().equals(innerMetadata.get())) {
			return DataResult.error(() -> "Conflicting outer and nested NBT output metadata for " + type);
		}

		Optional<CompoundTag> metadata = outerMetadata.isPresent()
				? outerMetadata
				: innerMetadata;
		if (metadata.isEmpty()) {
			return DataResult.success(OutputMetadata.EMPTY);
		}
		return decodeMetadata(type, metadata.get());
	}

	private static DataResult<OutputMetadata> decodeMetadata(String type, CompoundTag tag) {
		if (SHAPELESS_TYPE.equals(type)) {
			if (!tag.keySet().equals(Set.of("variant"))) {
				return unexpectedMetadata(type, tag);
			}
			return tag.getInt("variant")
					.<DataResult<OutputMetadata>>map(variant -> DataResult.success(
							new OutputMetadata(OptionalInt.of(variant), Optional.empty())
					))
					.orElseGet(() -> DataResult.error(() ->
							"NBT output metadata variant must be an integer"
					));
		}

		if (PETAL_APOTHECARY_TYPE.equals(type)) {
			if (!tag.keySet().equals(Set.of("SkullOwner"))) {
				return unexpectedMetadata(type, tag);
			}
			return tag.getString("SkullOwner")
					.filter(owner -> !owner.isBlank())
					.<DataResult<OutputMetadata>>map(owner -> DataResult.success(
							new OutputMetadata(OptionalInt.empty(), Optional.of(owner))
					))
					.orElseGet(() -> DataResult.error(() ->
							"NBT output metadata SkullOwner must be a non-empty string"
					));
		}

		return DataResult.error(() -> "NBT output metadata is not supported for nested recipe type " + type);
	}

	private static DataResult<OutputMetadata> unexpectedMetadata(String type, CompoundTag tag) {
		return DataResult.error(() ->
				"Unsupported NBT output metadata keys for " + type + ": " + tag.keySet()
		);
	}

	private static <T> DataResult<T> normalizeItemStackTemplate(
			DynamicOps<T> ops, T output, OutputMetadata metadata
	) {
		return renameField(ops, new Dynamic<>(ops, output).remove("nbt").getValue(), "item", "id")
				.flatMap(normalized -> ItemStackTemplate.CODEC.parse(ops, normalized))
				.mapError(message -> "Invalid nested recipe output: " + message)
				.flatMap(template -> applyMetadata(template, metadata))
				.flatMap(template -> ItemStackTemplate.CODEC.encodeStart(ops, template));
	}

	private static DataResult<ItemStackTemplate> applyMetadata(
			ItemStackTemplate template, OutputMetadata metadata
	) {
		if (metadata.equals(OutputMetadata.EMPTY)) {
			return DataResult.success(template);
		}

		DataComponentPatch.SplitResult components = template.components().split();
		DataComponentPatch.Builder patch = DataComponentPatch.builder()
				.set(components.added());
		components.removed().forEach(patch::remove);

		if (metadata.variant().isPresent()) {
			CustomData existingData = components.added().get(DataComponents.CUSTOM_DATA);
			CompoundTag customData = existingData == null
					? new CompoundTag()
					: existingData.copyTag();
			int variant = metadata.variant().getAsInt();
			Optional<Integer> existingVariant = customData.getInt("variant");
			if (customData.contains("variant") && existingVariant.isEmpty()) {
				return DataResult.error(() ->
						"Existing output custom data variant must be an integer"
				);
			}
			if (existingVariant.isPresent() && existingVariant.get() != variant) {
				return DataResult.error(() ->
						"Conflicting variant in existing output custom data"
				);
			}
			customData.putInt("variant", variant);
			patch.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
		}

		if (metadata.profile().isPresent()) {
			ResolvableProfile profile = ResolvableProfile.createUnresolved(
					metadata.profile().get()
			);
			ResolvableProfile existingProfile = components.added().get(DataComponents.PROFILE);
			if (existingProfile != null && !existingProfile.equals(profile)) {
				return DataResult.error(() ->
						"Legacy SkullOwner conflicts with an existing output profile component"
				);
			}
			patch.set(DataComponents.PROFILE, profile);
		}

		return DataResult.success(new ItemStackTemplate(
				template.item(), template.count(), patch.build()
		));
	}

	private static <T> DataResult<T> normalizeIngredientField(
			DynamicOps<T> ops, T recipe, String field
	) {
		Dynamic<T> dynamic = new Dynamic<>(ops, recipe);
		return dynamic.getElement(field)
				.flatMap(ops::getStream)
				.flatMap(stream -> normalizeIngredients(ops, stream.toList()))
				.map(values -> dynamic.set(
						field, new Dynamic<>(ops, ops.createList(values.stream()))
				).getValue())
				.mapError(message -> "Invalid nested recipe " + field + ": " + message);
	}

	private static <T> DataResult<List<T>> normalizeIngredients(
			DynamicOps<T> ops, List<T> inputs
	) {
		List<T> normalized = new ArrayList<>(inputs.size());
		for (int index = 0; index < inputs.size(); index++) {
			DataResult<T> ingredient = normalizeIngredient(ops, inputs.get(index));
			Optional<T> value = ingredient.result();
			if (value.isEmpty()) {
				int failedIndex = index;
				return ingredient
						.mapError(message -> "ingredient " + failedIndex + ": " + message)
						.map(ignored -> normalized);
			}
			normalized.add(value.get());
		}
		return DataResult.success(normalized);
	}

	private static <T> DataResult<T> normalizeSingleIngredientField(
			DynamicOps<T> ops, T recipe, String field
	) {
		Dynamic<T> dynamic = new Dynamic<>(ops, recipe);
		return dynamic.getElement(field)
				.flatMap(value -> normalizeIngredient(ops, value))
				.map(value -> dynamic.set(field, new Dynamic<>(ops, value)).getValue())
				.mapError(message -> "Invalid nested recipe " + field + ": " + message);
	}

	private static <T> DataResult<T> normalizeIngredient(DynamicOps<T> ops, T input) {
		if (ops.getStringValue(input).result().isPresent()) {
			return DataResult.success(input);
		}

		Optional<MapLike<T>> mapResult = ops.getMap(input).result();
		if (mapResult.isEmpty()) {
			return DataResult.success(input);
		}

		MapLike<T> map = mapResult.get();
		T item = map.get("item");
		T tag = map.get("tag");
		if (item == null && tag == null) {
			return DataResult.success(input);
		}
		if (item != null && tag != null) {
			return DataResult.error(() -> "legacy ingredient contains both item and tag");
		}
		if (map.entries().count() != 1) {
			return DataResult.error(() -> "legacy ingredient contains unsupported extra fields");
		}

		T value = item != null ? item : tag;
		return ops.getStringValue(value)
				.map(identifier -> ops.createString(item != null ? identifier : "#" + identifier));
	}

	private static <T> DataResult<T> renameField(
			DynamicOps<T> ops, T input, String from, String to
	) {
		Optional<MapLike<T>> mapResult = ops.getMap(input).result();
		if (mapResult.isEmpty()) {
			return DataResult.success(input);
		}

		MapLike<T> map = mapResult.get();
		if (map.get(to) != null && map.get(from) != null) {
			return DataResult.error(() ->
					"Nested recipe output contains both " + from + " and " + to
			);
		}
		if (map.get(to) != null || map.get(from) == null) {
			return DataResult.success(input);
		}

		RecordBuilder<T> builder = ops.mapBuilder();
		map.entries().forEach(entry -> builder.add(
				renameKey(ops, entry, from, to), entry.getSecond()
		));
		return builder.build(ops.empty());
	}

	private static <T> T renameKey(
			DynamicOps<T> ops, Pair<T, T> entry, String from, String to
	) {
		return ops.getStringValue(entry.getFirst()).result()
				.filter(from::equals)
				.map(ignored -> ops.createString(to))
				.orElse(entry.getFirst());
	}

	private record OutputMetadata(OptionalInt variant, Optional<String> profile) {
		private static final OutputMetadata EMPTY =
				new OutputMetadata(OptionalInt.empty(), Optional.empty());
	}
}
