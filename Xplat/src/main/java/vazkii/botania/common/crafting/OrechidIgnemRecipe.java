/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonObject;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.StateIngredient;

public class OrechidIgnemRecipe extends OrechidRecipe {
	public OrechidIgnemRecipe(Identifier id, StateIngredient input, StateIngredient output, int weight, CacheableFunction successFunction) {
		super(id, input, output, weight, successFunction);
	}

	private OrechidIgnemRecipe(OrechidRecipe recipe) {
		this(recipe.getId(), recipe.getInput(), recipe.getOutput(), recipe.getWeight(), recipe.getSuccessFunction());
	}

	@NotNull
	@Override
	public RecipeType<? extends OrechidIgnemRecipe> getType() {
		return BotaniaRecipeTypes.ORECHID_IGNEM_TYPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return BotaniaRecipeTypes.ORECHID_IGNEM_SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<OrechidIgnemRecipe> {
		@Override
		public OrechidIgnemRecipe fromJson(@NotNull Identifier recipeId, @NotNull JsonObject json) {
			return new OrechidIgnemRecipe(BotaniaRecipeTypes.ORECHID_SERIALIZER.fromJson(recipeId, json));
		}

		@Override
		public OrechidIgnemRecipe fromNetwork(@NotNull Identifier recipeId, @NotNull FriendlyByteBuf buffer) {
			return new OrechidIgnemRecipe(BotaniaRecipeTypes.ORECHID_SERIALIZER.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull OrechidIgnemRecipe recipe) {
			BotaniaRecipeTypes.ORECHID_SERIALIZER.toNetwork(buffer, recipe);
		}
	}
}
