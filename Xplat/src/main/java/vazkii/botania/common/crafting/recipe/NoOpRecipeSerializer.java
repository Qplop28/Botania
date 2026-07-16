package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

// Serializer for dynamic recipes that have no JSON or network representation.
// The registered recipe ID fully identifies the recipe.
public final class NoOpRecipeSerializer {
	private NoOpRecipeSerializer() {}

	public static <T extends Recipe<?>> RecipeSerializer<T> create(
			Identifier recipeId,
			Function<Identifier, T> constructor
	) {
		T recipe = constructor.apply(recipeId);

		return new RecipeSerializer<>(
				MapCodec.unit(recipe),
				StreamCodec.<RegistryFriendlyByteBuf, T>unit(recipe)
		);
	}
}