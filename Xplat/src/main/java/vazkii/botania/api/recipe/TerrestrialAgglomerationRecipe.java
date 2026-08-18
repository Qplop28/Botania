/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.BotaniaAPI;

import java.util.Objects;

public interface TerrestrialAgglomerationRecipe
		extends Recipe<RecipeInput> {
	Identifier TERRA_PLATE_ID =
			Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "terra_plate");
	Identifier TYPE_ID = TERRA_PLATE_ID;

	int getMana();

	/** Transitional accessors for recipe-viewer integrations. */
	@Deprecated
	NonNullList<Ingredient> getIngredients();

	@Deprecated
	ItemStack getResultItem(RegistryAccess registries);

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<TerrestrialAgglomerationRecipe> getType() {
		return (RecipeType<TerrestrialAgglomerationRecipe>)
				Objects.requireNonNull(
						BuiltInRegistries.RECIPE_TYPE
								.getValue(TYPE_ID)
				);
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}
}
