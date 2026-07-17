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
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import vazkii.botania.api.BotaniaAPI;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface ElvenTradeRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(
			BotaniaAPI.MODID,
			"elven_trade"
	);

	/**
	 * Attempts to match the recipe
	 *
	 * @param stacks Entire contents of the portal's buffer
	 * @return {@link Optional#empty()} if recipe doesn't match, Optional with a set of items used by recipe
	 *         otherwise
	 */
	Optional<List<ItemStack>> match(List<ItemStack> stacks);

	/**
	 * If the recipe does not contain the item, it will be destroyed upon entering the portal.
	 */
	boolean containsItem(ItemStack stack);

	/**
	 * @return Preview of the inputs
	 */
	@Deprecated
	NonNullList<Ingredient> getIngredients();

	/**
	 * @return Preview of the outputs
	 */
	List<ItemStack> getOutputs();

	/**
	 * Actually evaluate the recipe
	 */
	List<ItemStack> getOutputs(List<ItemStack> inputs);

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<ElvenTradeRecipe> getType() {
		return (RecipeType<ElvenTradeRecipe>) Objects.requireNonNull(
				BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	// Ignored IRecipe boilerplate

	@Override
	default boolean matches(RecipeInput input, Level level) {
		return false;
	}

	@Override
	default ItemStack assemble(RecipeInput input) {
		return ItemStack.EMPTY;
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

	@Deprecated
	default ItemStack getResultItem(RegistryAccess registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}

	/**
	 * Checks if this recipe is a "return" recipe, meaning that it returns the item that was thrown into it.
	 *
	 * @return {@code true} if recipe is a return recipe, {@code false} otherwise.
	 */
	default boolean isReturnRecipe() {
		return this.getOutputs().size() == 1
				&& this.getIngredients().size() == 1
				&& this.containsItem(this.getOutputs().get(0));
	}
}
