/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;

import java.util.List;
import java.util.Objects;

public interface ManaInfusionRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(
			BotaniaAPI.MODID,
			"mana_infusion"
	);

	/**
	 * Attempts to match the recipe.
	 *
	 * @param stack The whole stack that is in the Mana Pool (when actually crafting)
	 *              or in the player's hand (for the HUD).
	 * @return Whether this recipe matches the given stack.
	 */
	boolean matches(ItemStack stack);

	/**
	 * Get the recipe output, used for display (in JEI or the HUD).
	 * If {@link #getRecipeOutput} isn't overridden, this is also the actual result of the crafting recipe.
	 *
	 * @return The output stack of the recipe.
	 */
	ItemStack getResultItem(RegistryAccess registries);

	/**
	 * Get the actual recipe output, not just for display. Defaults to a copy of {@link #getResultItem}.
	 *
	 * @param input The whole stack that is in the Mana Pool, not a copy.
	 * @return The output stack of the recipe for the specific input.
	 */
	default ItemStack getRecipeOutput(RegistryAccess registries, ItemStack input) {
		return getResultItem(registries).copy();
	}

	/**
	 * Get the catalyst that must be under the Mana Pool for this recipe, or {@code null} if it can be anything.
	 *
	 * @return The catalyst ingredient.
	 */
	@Nullable
	StateIngredient getRecipeCatalyst();

	/**
	 * @return How much mana this recipe consumes from the pool.
	 */
	int getManaToConsume();

	/**
	 * Transitional ingredient accessor for integrations using the pre-1.21
	 * recipe API.
	 */
	@Deprecated
	NonNullList<Ingredient> getIngredients();

	/** Transitional group accessor for older integrations. */
	@Deprecated
	default String getGroup() {
		return group();
	}

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<ManaInfusionRecipe> getType() {
		return (RecipeType<ManaInfusionRecipe>) Objects.requireNonNull(
				BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	// Ignored IRecipe stuff

	@Override
	default ItemStack assemble(RecipeInput input) {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean matches(RecipeInput input, Level level) {
		return false;
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.create(List.<Ingredient>of());
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
