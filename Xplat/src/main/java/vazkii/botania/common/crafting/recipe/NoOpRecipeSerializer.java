package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;
import java.util.function.Supplier;

// Serializer for dynamic recipes that have no JSON or network representation.
public final class NoOpRecipeSerializer {
	private NoOpRecipeSerializer() {}

	/**
	 * Modern custom recipes no longer contain their registered recipe ID.
	 */
	public static <T extends Recipe<?>> RecipeSerializer<T> create(
			Supplier<T> constructor
	) {
		return create(constructor.get());
	}

	/**
	 * Transitional overload for older Botania recipe implementations that still
	 * store an Identifier inside the recipe object.
	 */
	public static <T extends Recipe<?>> RecipeSerializer<T> create(
			Identifier recipeId,
			Function<Identifier, T> constructor
	) {
		return create(constructor.apply(recipeId));
	}

	private static <T extends Recipe<?>> RecipeSerializer<T> create(T recipe) {
		return new RecipeSerializer<>(
				MapCodec.unit(recipe),
				StreamCodec.<RegistryFriendlyByteBuf, T>unit(recipe)
		);
	}
}